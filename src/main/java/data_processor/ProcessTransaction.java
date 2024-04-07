package data_processor;

import java.math.BigDecimal;
import java.util.*;

public class ProcessTransaction {


    public static List<Event> processTransactions(final List<User> users, final List<Transaction> transactions, final List<BinMapping> binMappings) {

        List<Event> events = new ArrayList<>();
        List<String> newCreditCardAccount = new ArrayList<>();
        Set<String> transactionIds = new HashSet<>();
        Set<String> hasDeposited = new HashSet<>();
        Map<String, String> accountsInUseCheck = new HashMap<>();


        for (Transaction transaction : transactions) {
            String transactionId = transaction.getTransaction_ID();
            String transactionUserId = transaction.getUserID();
            String transactionType = transaction.getType();
            BigDecimal transactionAmount = transaction.getAmount();
            String transactionMethod = transaction.getMethod();
            String transactionUserAccount = transaction.getAccount_number();

            accountsInUseCheck.putIfAbsent(transaction.getAccount_number(), transaction.getUserID());

            if (!transactionIds.add(transactionId)) {
                String message = "Transaction " + transactionId + " already processed (ID isn't unique).";
                events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                continue;
            }

            User validUser = findValidUser(transactionUserId, users);
            if (validUser != null) {
                if (validUser.getIsFrozen() == 1) {
                    String message = "User account with an ID " + validUser.getUser_ID() + " is frozen";
                    events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                    continue;
                }

                if (!Objects.equals(transactionType, "DEPOSIT") && !transactionType.equals("WITHDRAW")) {
                    String message = "Invalid transaction type";
                    events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                    continue;
                }

                if (Objects.equals(transactionMethod, "CARD")) {
                    if (!Objects.equals(validUser.getCountry(), matchingCountries(validUser, transaction, binMappings))) {

                       Map<String,String> countryCodeMap = getCountryCodeMap();
                       String userCountryTag = null;
                       String countryTag = null;

                        for (Map.Entry<String, String> userCode : countryCodeMap.entrySet()) {
                            if (userCode.getValue().equals(validUser.getCountry())) {
                                userCountryTag = userCode.getKey();
                            }
                            if(userCode.getValue().equals(matchingCountries(validUser,transaction,binMappings))){
                                countryTag = userCode.getKey();
                            }
                        }

                        String message = "Invalid country: " + countryTag
                                + ", expected: " + validUser.getCountry() + "(" + userCountryTag + ")";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;


                    } else if(!Objects.equals(findCardType(transaction, binMappings), "DC") &&
                            !transactionType.equals("WITHDRAW")) {


                        newCreditCardAccount.add(transactionUserAccount);
                        String message = "User tried to use CC. Only DC allowed";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;

                    }
                }

                if(transactionMethod.equals("TRANSFER")) {
                    if (validIBAN(transactionUserAccount)) {

                        String message = "IBAN " + transactionUserAccount + " is not valid!";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;

                    } else if (!Objects.equals(validUser.getCountry(), transactionUserAccount.trim().substring(0, 2))) {
                        String countryTag = transactionUserAccount.trim().substring(0, 2);

                        String message = "Account country: " + countryTag +
                                " and user country: " + validUser.getCountry() + " do not match.";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;
                    }
                }




                if (Objects.equals(transactionType, "DEPOSIT")) {
                    if (validUser.getDeposit_min().compareTo(transactionAmount) > 0) {
                        hasDeposited.remove(transactionUserAccount);
                        String message = "Minimum deposit: " + validUser.getDeposit_min() +
                                ". User tried to deposit: " + transactionAmount + ".";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;

                    } else if (validUser.getDeposit_max().compareTo(transactionAmount) < 0) {
                        hasDeposited.remove(transactionUserAccount);
                        String message = "Maximum deposit: " + validUser.getDeposit_max() +
                                " user tried to deposit: " + transactionAmount + ".";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;

                    }else if (!accountsInUseCheck.get(transactionUserAccount).contains(transactionUserId)) {
                        String message = "Account number: " + transactionUserAccount + " is in use by another user";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
                        continue;


                    } else {
                        hasDeposited.add(transactionUserAccount);
                        validUser.deposit(transactionAmount);
                        String message = "OK";
                        events.add(new Event(transactionId, Event.STATUS_APPROVED, message));
                    }
                }



                if (Objects.equals(transactionType, "WITHDRAW")) {


                    if(validUser.getWithdraw_min().compareTo(transactionAmount) > 0) {
                        String message = "User tried to withdraw " + transactionAmount
                                + " minimum is " + validUser.getWithdraw_min() + ".";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));

                    }else if (validUser.getWithdraw_max().compareTo(transactionAmount) < 0){
                        String message = "User tried to withdraw " + transactionAmount
                                + " maximum is " + validUser.getWithdraw_max() + ".";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));

                    }else if (validUser.getBalance().compareTo(transactionAmount) < 0) {
                    String message = "Cannot withdraw " + transaction.getAmount() +
                            " from balance: " + validUser.getBalance() + ".";
                    events.add(new Event(transactionId, Event.STATUS_DECLINED, message));

                    }else if(!hasDeposited.contains(transactionUserAccount)) {
                        String message = "New user with an account: " + transactionUserAccount + ", has not deposited!";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));

                    }else if (!accountsInUseCheck.get(transactionUserAccount).contains(transactionUserId)) {
                        String message = "Account number: " + transactionUserAccount + " is in use by another user";
                        events.add(new Event(transactionId, Event.STATUS_DECLINED, message));

                    }else{
                        if(hasDeposited.contains(transactionUserAccount)) {
                            validUser.withdraw(transactionAmount);
                            String message = "OK";
                            events.add(new Event(transactionId, Event.STATUS_APPROVED, message));
                        }
                    }
                }

            } else {
                String message = "User ID " + transactionUserId + " not found!";
                events.add(new Event(transactionId, Event.STATUS_DECLINED, message));
            }
        }

        return events;
    }



    public static String matchingCountries(User user, Transaction transaction, List<BinMapping> binMappings){


        Map<String, String> countryCodeMap = getCountryCodeMap();

        BinMapping countryMapping = null;
        BinMapping noMatchCountry = null;
        for(BinMapping binMapping : binMappings){
            String countryIso3 = binMapping.getCountry();
            if(!countryCodeMap.containsKey(countryIso3)){
                continue;
            }

            String country = countryCodeMap.get(countryIso3);
            String rangeFrom = binMapping.getRange_from();
            String rangeTo = binMapping.getRange_to();

            if(country.equals(user.getCountry()) &&
                    transaction.getAccount_number().substring(0,10).compareTo(rangeFrom) >= 0 &&
                    transaction.getAccount_number().substring(0,10).compareTo(rangeTo) <= 0){
                countryMapping = binMapping;
                break;

            }else if(!country.equals(user.getCountry()) &&
                    transaction.getAccount_number().substring(0,10).compareTo(rangeFrom) >= 0 &&
                    transaction.getAccount_number().substring(0,10).compareTo(rangeTo) <= 0){
                noMatchCountry = binMapping;
            }
        }

        if(countryMapping != null){
            return countryCodeMap.get(countryMapping.getCountry());
        }else{
            return countryCodeMap.get(noMatchCountry.getCountry());
        }
    }

    private static Map<String, String> getCountryCodeMap() {
        Map<String, String> countryCodeMap = new HashMap<>();
        Locale[] locales = Locale.getAvailableLocales();

        for(Locale locale : locales) {
            try {
                String iso3Country = locale.getISO3Country();
                if (!iso3Country.isEmpty()) {
                    countryCodeMap.putIfAbsent(iso3Country, locale.getCountry());
                }
            } catch (MissingResourceException e) {
            }
        }
        return countryCodeMap;
    }

    public static String findCardType(Transaction transaction, List<BinMapping> binMappings){

        BinMapping cardMapping = null;
        for(BinMapping binMapping :binMappings) {
            String rangeFrom = binMapping.getRange_from();
            String rangeTo = binMapping.getRange_to();

            if (transaction.getAccount_number().substring(0,10).compareTo(rangeFrom) >= 0 &&
                    transaction.getAccount_number().substring(0,10).compareTo(rangeTo) <= 0 ){
                cardMapping = binMapping;
                break;
            }
        }

        if (cardMapping != null) {
            return cardMapping.getType();
        }else{
            return null;
        }
    }

    public static User findValidUser(String userID, List<User> users) {
        for (User user : users) {
            if (Objects.equals(userID, user.getUser_ID())) {
                return user;
            }
        }
        return null;
    }

    public static boolean validIBAN(String iban){
        int minSize = 15;
        int maxSize = 34;
        long maxIban = 999999999;
        long modulusIban = 97;

        String trimmed = iban.trim();
        if(trimmed.length() < minSize || trimmed.length() > maxSize){
            return false;
        }
        String format = trimmed.substring(4) + trimmed.substring(0,4);
        long total = 0;
        for (int i = 0; i < format.length(); i++) {
            int charValue = Character.getNumericValue(format.charAt(i));

            if(charValue < 0 || charValue > 35){
                return false;
            }

            total = (charValue > 9 ? total * 100 : total * 10) + charValue;

            if(total>maxIban){
                total = (total % modulusIban);
            }
        }
        return (total % modulusIban) != 1;
    }
}

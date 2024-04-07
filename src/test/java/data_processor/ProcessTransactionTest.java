package data_processor;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProcessTransactionTest {

    @Test
    void processTransactions() {
        List<User> users = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        List<BinMapping> binMappings = new ArrayList<>();
        List<Event> events;

        users.add(new User("1", "User", new BigDecimal(1000), "EE", 0, new BigDecimal(5), new BigDecimal(2000), new BigDecimal(5), new BigDecimal(1500)));
        users.add(new User("2", "User2", new BigDecimal(1000), "LT", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));
        users.add(new User("3", "User3", new BigDecimal(1000), "LV", 1, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));
        users.add(new User("4", "User4", new BigDecimal(1000), "LV", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));
        users.add(new User("5", "User5", new BigDecimal(1000), "EE", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));
        users.add(new User("6", "User6", new BigDecimal(1000), "LT", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));
        users.add(new User("7", "User7", new BigDecimal(1000), "LT", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500)));

        transactions.add(new Transaction("1","1", "DEPOSIT", new BigDecimal(100), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("1","2", "DEPOSIT", new BigDecimal(10), "TRANSFER", "LT754323658986932423"));
        transactions.add(new Transaction("3","3", "DEPOSIT", new BigDecimal(100), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("4","2", "???????", new BigDecimal(100), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("5","4", "DEPOSIT", new BigDecimal(100), "CARD", "1111111111111111"));
        transactions.add(new Transaction("6","5", "DEPOSIT", new BigDecimal(100), "CARD", "3333323332323232332323"));
        transactions.add(new Transaction("7","5", "DEPOSIT", new BigDecimal(100), "TRANSFER", "EE3333323332323232332323"));
        transactions.add(new Transaction("8","1", "DEPOSIT", new BigDecimal(100), "TRANSFER", "LT754323658986932423"));
        transactions.add(new Transaction("9","1", "DEPOSIT", new BigDecimal(1), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("10","1", "DEPOSIT", new BigDecimal(10000), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("11","6", "DEPOSIT", new BigDecimal(100), "TRANSFER", "LT754323658986932423"));
        transactions.add(new Transaction("12","1", "WITHDRAW", new BigDecimal(1), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("13","1", "WITHDRAW", new BigDecimal(10000), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("14","1", "WITHDRAW", new BigDecimal(1500), "TRANSFER", "EE181286519889332750"));
        transactions.add(new Transaction("15","7", "WITHDRAW", new BigDecimal(500), "TRANSFER", "LT817763842614249324"));
        transactions.add(new Transaction("16","8", "DEPOSIT", new BigDecimal(500), "TRANSFER", "LT817763842614249324"));


        binMappings.add(new BinMapping("Swedbank", "1111111111", "2222222222", "DC", "EST"));
        binMappings.add( new BinMapping("LHV", "3111111111", "4000000000", "CC", "EST"));

       events = ProcessTransaction.processTransactions(users, transactions, binMappings);


        assertEquals("APPROVED", events.get(0).status);  // OK
        assertEquals("DECLINED", events.get(1).status);  // ID is not unique
        assertEquals("DECLINED", events.get(2).status);  // Frozen account
        assertEquals("DECLINED", events.get(3).status);  // Invalid transaction type
        assertEquals("DECLINED", events.get(4).status ); //Invalid country code
        assertEquals("DECLINED", events.get(5).status);  // CC used
        assertEquals("DECLINED", events.get(6).status);  // Invalid IBAN
        assertEquals("DECLINED", events.get(7).status);  // Country mismatch(TRANSFER)
        assertEquals("DECLINED", events.get(8).status);  // Deposit minimum
        assertEquals("DECLINED", events.get(9).status);  // Maximum deposit
        assertEquals("DECLINED", events.get(10).status); // Account in use
        assertEquals("DECLINED", events.get(11).status); // Minimum withdraw
        assertEquals("DECLINED", events.get(12).status); // Maximum withdraw
        assertEquals("DECLINED", events.get(13).status); // Balance lower than withdraw
        assertEquals("DECLINED", events.get(14).status); // Has not deposited
        assertEquals("DECLINED", events.get(15).status); // No userID found



    }

    @Test
    void matchingCountries() {

        User user = new User("1", "User", new BigDecimal(2000), "EE", 0, new BigDecimal(5), new BigDecimal(2000), new BigDecimal(5), new BigDecimal(1500));
        User user2 = new User("2", "User2", new BigDecimal(1000), "LT", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500));

        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", new BigDecimal(200), "CARD", "222222222222222" );

        BinMapping binmapping = new BinMapping("Swedbank", "1111111111", "2222222222", "DC", "EST");
        BinMapping binMapping2 = new BinMapping("LHV", "3111111111", "4000000000", "CC", "EST");
        List<BinMapping> binMappingList = new ArrayList<>();
        binMappingList.add(binmapping);
        binMappingList.add(binMapping2);

        assertEquals("EE", ProcessTransaction.matchingCountries(user,transaction1,binMappingList));
        assertNotEquals("LT", ProcessTransaction.matchingCountries(user2,transaction1, binMappingList));





    }

    @Test
    void findCardType() {

        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", new BigDecimal(200), "CARD", "222222222222222" );
        Transaction transaction2 = new Transaction("2", "3", "WITHDRAW", new BigDecimal(150), "CARD", "333333333333333" );


        BinMapping binmapping = new BinMapping("Swedbank", "1111111111", "2222222222", "DC", "EST");
        BinMapping binMapping2 = new BinMapping("LHV", "3111111111", "4000000000", "CC", "EST");
        List<BinMapping> binMappingList = new ArrayList<>();
        binMappingList.add(binmapping);
        binMappingList.add(binMapping2);

        assertEquals( "DC", ProcessTransaction.findCardType(transaction1, binMappingList) );
        assertEquals("CC", ProcessTransaction.findCardType(transaction2, binMappingList));

    }

    @Test
    void findValidUser() {
        List<Transaction> transactions = new ArrayList<>();
        Transaction transaction1 = new Transaction("1", "1", "DEPOSIT", new BigDecimal(200), "TRANSFER", "EE961238396123124646" );
        Transaction transaction2 = new Transaction("2", "3", "WITHDRAW", new BigDecimal(150), "CARD", "4975223717801323" );
        transactions.add(transaction1);
        transactions.add(transaction2);

        List<User> users = new ArrayList<>();
        User user = new User("1", "User", new BigDecimal(2000), "EE", 0, new BigDecimal(5), new BigDecimal(2000), new BigDecimal(5), new BigDecimal(1500));
        User user2 = new User("2", "User2", new BigDecimal(1000), "LT", 0, new BigDecimal(10), new BigDecimal(1500), new BigDecimal(10), new BigDecimal(1500));
        users.add(user);
        users.add(user2);


            assertEquals(transaction1.getUserID(), ProcessTransaction.findValidUser(transaction1.getUserID(), users).getUser_ID());
            assertNull(ProcessTransaction.findValidUser(transaction2.getUserID(), users), transaction2.getUserID());



    }

    @Test
    void validIBAN() {
        String Iban1 ="LT754323658986932423";
        String Iban2 ="EETT12345442141464563f54";

        assertFalse(ProcessTransaction.validIBAN(Iban1));
        assertTrue(ProcessTransaction.validIBAN(Iban2));

    }
}
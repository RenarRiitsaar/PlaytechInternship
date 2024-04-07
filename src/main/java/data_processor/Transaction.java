package data_processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private final String transaction_ID;
    private final String userID;
    private final String type;
    private final BigDecimal amount;
    private final String method;
    private final String account_number;


    Transaction(String transaction_ID, String userID, String type, BigDecimal amount, String method, String account_number){
        this.transaction_ID = transaction_ID;
        this.userID = userID;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.account_number = account_number;
    }

    public static List<Transaction> readTransactions(Path filePath) {

        List<Transaction> readTransactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            reader.readLine();

            String transaction;
            while ((transaction = reader.readLine()) != null) {

                String[] transactions = transaction.split(",");

                String transactionId = transactions[0];
                String userId = transactions[1];
                String type = transactions[2];
                String amount = transactions[3];
                String method = transactions[4];
                String account_number = transactions[5];

                BigDecimal amountValue = new BigDecimal(amount);


                readTransactions.add(new Transaction(transactionId, userId, type, amountValue, method, account_number));
            }
        } catch (Exception e) {
            System.out.println("Couldn't get transactions!");
            e.printStackTrace();
        }
        return readTransactions;
    }

    public String getTransaction_ID() {
        return transaction_ID;
    }


    public String getUserID() {
        return userID;
    }


    public String getType() {
        return type;
    }


    public BigDecimal getAmount() {
        return amount;
    }


    public String getMethod() {
        return method;
    }



    public String getAccount_number() {
        return account_number;
    }


}
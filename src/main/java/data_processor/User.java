package data_processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class User {
    private final String user_ID;
    private final String userName;
    private  BigDecimal balance;
    private final String country;
    private final int isFrozen;

    private final BigDecimal deposit_min;
    private final BigDecimal deposit_max;
    private final BigDecimal withdraw_min;
    private final BigDecimal withdraw_max;


    User(String id, String userName, BigDecimal balance, String country, int isFrozen, BigDecimal deposit_min, BigDecimal deposit_max, BigDecimal withdraw_min, BigDecimal withdraw_max) {

        this.user_ID = id;
        this.userName = userName;
        this.balance = balance;
        this.country = country;
        this.isFrozen = isFrozen;
        this.deposit_min = deposit_min;
        this.deposit_max = deposit_max;
        this.withdraw_min = withdraw_min;
        this.withdraw_max = withdraw_max;


    }

    public static List<User> readUsers(Path filePath) {

        List<User> readUsers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            reader.readLine();

            String user;
            while ((user = reader.readLine()) != null) {

                String[] users = user.split(",");

                String id = users[0];
                String username = users[1];
                String balance = users[2];
                String country = users[3];
                String isFrozen = users[4];
                String deposit_min = users[5];
                String deposit_max = users[6];
                String withdraw_min = users[7];
                String withdraw_max = users[8];


                int intValueOfIsFrozen = Integer.parseInt(isFrozen);
                BigDecimal balanceValue = new BigDecimal(balance);
                BigDecimal deposit_min_value = new BigDecimal(deposit_min);
                BigDecimal deposit_max_value = new BigDecimal(deposit_max);
                BigDecimal withdraw_min_value = new BigDecimal(withdraw_min);
                BigDecimal withdraw_max_value = new BigDecimal(withdraw_max);

                readUsers.add(new User(id, username, balanceValue, country, intValueOfIsFrozen, deposit_min_value, deposit_max_value, withdraw_min_value, withdraw_max_value));
            }
        } catch (Exception e) {
            System.out.println("Couldn't get users!");
            e.printStackTrace();
        }
        return readUsers;
    }


    public void deposit(BigDecimal amount){
      balance = balance.add(amount);

    }
    public void withdraw(BigDecimal withdraw){
        balance = balance.subtract(withdraw);
    }

    public String getUser_ID() {
        return user_ID;
    }


    public BigDecimal getBalance() {
        return balance;
    }


    public String getCountry() {
        return country;
    }


    public int getIsFrozen() {
        return isFrozen;
    }


    public BigDecimal getDeposit_min() {
        return deposit_min;
    }

    public BigDecimal getDeposit_max() {
        return deposit_max;
    }


    public BigDecimal getWithdraw_min() {
        return withdraw_min;
    }

    public BigDecimal getWithdraw_max() {
        return withdraw_max;
    }


}

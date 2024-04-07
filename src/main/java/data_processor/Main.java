package data_processor;

import static data_processor.BinMapping.readBinMappings;
import static data_processor.ProcessTransaction.processTransactions;
import static data_processor.Transaction.readTransactions;
import static data_processor.User.readUsers;
import static data_processor.WriteToFile.writeBalances;
import static data_processor.WriteToFile.writeEvents;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


public class Main {


    public static void main(String[] args) {

        List<User> users = null;
        List<Transaction> transactions = null;
        List<BinMapping> binMappings = null;


        String writeBalancePath = String.join(" ", args[3]);
        Path writeBalance = Paths.get(writeBalancePath);

        String writeEventPath = String.join(" ", args[4]);
        Path writeEvent = Paths.get(writeEventPath);


        try {
            String usersPath = String.join(" ", args[0]);
            Path userFile = Paths.get(usersPath);
            users = readUsers(userFile);


            String transactionsPath = String.join(" ", args[1]);
            Path transactionFile = Paths.get(transactionsPath);
            transactions = readTransactions(transactionFile);


            String binMappingPath = String.join(" ", args[2]);
            Path binMappingFile = Paths.get(binMappingPath);
            binMappings = readBinMappings(binMappingFile);


        } catch (Exception e) {
            System.out.println("Something wrong with reading data!");
            e.printStackTrace();
        }

        List<Event> events = processTransactions(users, transactions, binMappings);

        try {
            writeEvents(writeEvent, events);
        }catch(Exception e){
            System.out.println("Something went wrong writing events file");
            e.printStackTrace();
        }

        try{
            writeBalances(writeBalance,users);
        }catch(Exception e){
            System.out.println("Something went wrong with writing balances");
            e.printStackTrace();
        }
    }
}
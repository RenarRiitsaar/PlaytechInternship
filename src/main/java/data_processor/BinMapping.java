package data_processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class BinMapping {
    private final String name;
    private final String range_from;
    private final String range_to;
    private final String type;
    private final String country;


    BinMapping(String name, String range_from, String range_to, String type, String country){
        this.name = name;
        this.range_from = range_from;
        this.range_to = range_to;
        this.type = type;
        this.country = country;
    }

    public static List<BinMapping> readBinMappings(final Path filePath) {

        List<BinMapping> binMappings = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
            reader.readLine();

            String binMapping;
            while ((binMapping = reader.readLine()) != null) {

                String[] mapping= binMapping.split(",");

                String name = mapping[0];
                String rangeFrom = mapping[1];
                String rangeTo = mapping[2];
                String type = mapping[3];
                String country = mapping[4];

                binMappings.add(new BinMapping(name, rangeFrom, rangeTo, type, country));
            }
        } catch (Exception e) {
            System.out.println("Couldn't get bins!");
            e.printStackTrace();
        }
        return binMappings;
    }



    public String getRange_from() {
        return range_from;
    }


    public String getRange_to() {
        return range_to;
    }

    public String getType() {
        return type;
    }


    public String getCountry() {
        return country;
    }

}
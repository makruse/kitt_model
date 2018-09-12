package zmt.kitt;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static Map<String, Long> ids = new HashMap<>();
    private static long counter = 0;
    //using strings because string replace seems more reliable than char replace
    private static String NEW_COLUMN_SEPARATOR = ";";
    private static String OLD_COLUMN_SEPARATOR = "\t";

    public static void main(String[] args) {
        try{
        long startTime = System.currentTimeMillis();

        String filePath = args[0];
            String out;
        try {
           out = filePath.substring(0, filePath.lastIndexOf('.')) + "WithoutUnits.csv";
        } catch (IndexOutOfBoundsException e){
            System.out.println("Missing the specific filename?");
            e.printStackTrace();
            return;
        }
        PrintWriter writer = new PrintWriter(out, "UTF-8");


        File inputF = new File(filePath);

        InputStream inputFS = new FileInputStream(inputF);

        BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

        // skip the header of the csv and replace tabs with comma, as well spaces with
            String header = br.readLine()
                    .replace("steps","days")
                    .replace(OLD_COLUMN_SEPARATOR,NEW_COLUMN_SEPARATOR)
                    .replace(" ","")
                    .replace("AGE","AGE(years)")
                    .replace("Length","Length(cm)")
                    .replace("Biomass","Biomass(g)")
                    .replace("Ingested_Energy", "Ingested_Energy(kJ)")
                    .replace("Gut", "Gut(kJ)")
                    .replace("Fat","Fat(kJ)")
                    .replace("Protein","Protein(kJ)")
                    .replace("Excess","Excess(kJ)")
                    .replace("Shortterm","Shortterm(kJ)")
                    .replace("Netenergy","Netenergy(kJ)")
                    .replace("Consumed_Energy","Consumed_Energy(kJ)")
                    .replace("Food_Value","Food_Value(g/m^2)")
                    .replace("Repro_Storage", "Repro_Storage(kJ)")
                    .replace("Â","")
                    .replace("mÂ²","m^2");
            header = header.substring(0,header.length()-1);
        writer.println(header);
        System.out.println("Header");

        String line = "";
        while ((line = br.readLine()) != null) {
            writeFilteredLine(writer, line
                                        .replace("(","")
                                        .replace("kJ","")
                                        .replace("g/m","")
                                        .replace("year","")
                                        .replace(" ",""));
        }


        br.close();
        writer.close();
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println(endTime/1000 + "Seconds needed");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException i){
            i.printStackTrace();
        }
    }

    public static void writeFilteredLine(PrintWriter writer, String line){
        boolean kill = false;
        String[] parts = line.split(OLD_COLUMN_SEPARATOR);
        parts[0] = Integer.toString(Integer.parseInt(parts[0]) / 86400);

        if(ids.containsKey(parts[parts.length-1]))
            parts[parts.length-1] = Long.toString(ids.get(parts[parts.length-1]));
        else {
            ids.put(parts[parts.length-1],counter);
            parts[parts.length-1] = Long.toString(counter);
            counter++;
        }
        char[] chars = String.join(NEW_COLUMN_SEPARATOR,parts).toCharArray();
        for(int i=0, n=chars.length; i<n; i++){
            if(chars[i] == 'Â') //get rid of stupid science unit
                kill = true;
            else if(chars[i] == NEW_COLUMN_SEPARATOR.charAt(0))
                kill = false;

            if(!kill)
                writer.append(chars[i]);

        }

        writer.append("\n");
    }
}

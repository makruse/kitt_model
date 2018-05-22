package zmt.kitt;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static Map<String, Long> ids = new HashMap<>();
    private static long counter = 0;

    public static void main(String[] args) {
        try{
        long startTime = System.currentTimeMillis();
        long lineNr = 0;
        String filePath = args[0];
        String out = filePath.substring(0,filePath.lastIndexOf('.'))+ "WithoutUnits.csv";
        PrintWriter writer = new PrintWriter(out, "UTF-8");


        File inputF = new File(filePath);

        InputStream inputFS = new FileInputStream(inputF);

        BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

        // skip the header of the csv and replace tabs with comma, as well spaces with
        writer.println(br.readLine().replace("steps","days").replace("\t",",").replace(" ",""));
        System.out.println("Header");

        String line = "";
        while ((line = br.readLine()) != null) {
            writeFilteredLine(writer, line.replace("(","").replace("kJ",""));
            System.out.println("Line: " +lineNr);
            lineNr++;
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
        String[] parts = line.split("\t");
        parts[0] = Integer.toString(Integer.parseInt(parts[0]) / 86400);

        if(ids.containsKey(parts[parts.length-1]))
            parts[parts.length-1] = Long.toString(ids.get(parts[parts.length-1]));
        else {
            ids.put(parts[parts.length-1],counter);
            parts[parts.length-1] = Long.toString(counter);
            counter++;
        }
        char[] chars = String.join(",",parts).toCharArray();
        for(int i=0, n=chars.length; i<n; i++){
            if(chars[i] == 'Â') //get rid of stupid science unit
                kill = true;
            else if(chars[i] == ',')
                kill = false;

            if(!kill)
                writer.append(chars[i]);

        }

        writer.append("\n");
    }
}

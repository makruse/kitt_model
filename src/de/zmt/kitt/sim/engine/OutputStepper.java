package de.zmt.kitt.sim.engine;

import java.io.*;
import java.text.DecimalFormat;

import de.zmt.kitt.sim.Sim;
import de.zmt.sim_base.io.CsvWriter;

public class OutputStepper {
    CsvWriter csv;
    DecimalFormat fmt = new DecimalFormat("#.##");
    boolean fileIsOpen = false;

    public synchronized void prepareFile(String outPath) throws IOException {
	String strCount = "";
	int count = 1;
	// if outputfile with current name exist, add count to the path
	while (new File(outPath + "_total" + strCount + ".csv").exists()) {
	    strCount = "." + String.format("%02d", count);
	    count++;
	}

	// create output file
	csv = new CsvWriter(outPath + "_total" + strCount + ".csv");

	// create header of outputfile
	csv.append("steps:");
	csv.append("total:");
	csv.append("SpeciesA:");
	csv.append("SpeciesB:");
	csv.newLine();
    }

    public synchronized void closeFile() throws IOException {
	csv.close();
    }

    public void writeData(long steps, Sim sim) throws IOException {
	csv.append(String.valueOf(steps));
	csv.newLine();
    }
}

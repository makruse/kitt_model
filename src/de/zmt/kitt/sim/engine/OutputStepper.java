package de.zmt.kitt.sim.engine;

import java.io.*;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import de.zmt.kitt.sim.Sim;
import de.zmt.sim_base.io.CsvWriter;

public class OutputStepper {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(OutputStepper.class
	    .getName());

    private static final String PREFIX_WRITER = "results_";

    CsvWriter csv;
    DecimalFormat fmt = new DecimalFormat("#.##");
    boolean fileIsOpen = false;

    public synchronized void prepareFile(String outputPath) throws IOException {
	// create output directory
	new File(outputPath).mkdir();
	
	String writerPath = "";
	int count = 1;
	// if outputfile with current name exist, add count to the path
	do {
	    writerPath = outputPath + PREFIX_WRITER
		    + String.format("%04d", count) + CsvWriter.FILENAME_SUFFIX;
	    count++;
	} while (new File(writerPath).exists());

	// create output file
	logger.info("Creating output at: " + writerPath);
	csv = new CsvWriter(writerPath);

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

    // TODO write info, not just step numbers
    public void writeData(long steps, Sim sim) throws IOException {
	csv.append(String.valueOf(steps));
	csv.newLine();
    }
}

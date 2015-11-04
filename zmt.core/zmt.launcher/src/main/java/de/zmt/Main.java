package de.zmt;

import java.io.*;
import java.util.logging.*;

import org.kohsuke.args4j.*;

import de.zmt.launcher.*;
import de.zmt.launcher.strategies.LauncherStrategyContext;

/**
 * Runs simulations with passed arguments.
 * 
 * @author mey
 *
 */
public final class Main {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final int ERROR_CODE_INVALID_ARGS = 2;

    public static final String LOGGING_PROPERTIES_FILENAME = "logging.properties";

    public static void main(String[] args) throws CmdLineException {
	LauncherArgs data = new LauncherArgs();
	CmdLineParser parser = new FixedCmdLineParser(data);

	try {
	    parser.parseArgument(args);
	} catch (CmdLineException e) {
	    System.err.println(e.getMessage());
	    System.err.println("Try '--help' for more information.");
	    System.exit(ERROR_CODE_INVALID_ARGS);
	}

	if (data.isHelp()) {
	    data.restoreDefaults();
	    printHelp(parser);
	    return;
	}

	setupLogger();
	new Launcher(LauncherStrategyContext.createDefault()).run(data);
    }

    /**
     * Loads logging.properties file from working directory for setting up the
     * logger.
     */
    private static void setupLogger() {
	LogManager logManager = LogManager.getLogManager();
	try (InputStream stream = new FileInputStream(Main.LOGGING_PROPERTIES_FILENAME)) {
	    logManager.readConfiguration(stream);
	} catch (FileNotFoundException e) {
	    Logger.getAnonymousLogger().log(Level.INFO, Main.LOGGING_PROPERTIES_FILENAME
		    + " file not present in working directory. " + "Using default configuration.");
	} catch (IOException e) {
	    Logger.getAnonymousLogger().log(Level.WARNING,
		    Main.LOGGING_PROPERTIES_FILENAME + " could not be read. Using default configuration.", e);
	}
    }

    private static void printHelp(CmdLineParser parser) {
	System.err.println("Usage:" + parser.printExample(OptionHandlerFilter.REQUIRED) + " [options]");
	System.err.println();
	parser.printUsage(new OutputStreamWriter(System.err), null, OptionHandlerFilter.ALL);
    }
}

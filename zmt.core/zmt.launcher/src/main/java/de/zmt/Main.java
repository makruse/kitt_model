package de.zmt;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.FixedCmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;

import de.zmt.launcher.Launcher;
import de.zmt.launcher.LauncherArgs;
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
	LauncherArgs launcherArgs = new LauncherArgs();
	CmdLineParser parser = new FixedCmdLineParser(launcherArgs);

	try {
	    parser.parseArgument(args);
	} catch (CmdLineException e) {
	    System.err.println(e.getMessage());
	    exitInvalidArgs();
	}
	// additional check is needed for export that needs a sim name
	if ((launcherArgs.getExportAutoParamsPath() != null || launcherArgs.getExportSimParamsPath() != null)
		&& launcherArgs.getSimName() == null) {
	    System.err.println("Argument \"<NAME>\" is required when exporting parameters.");
	    exitInvalidArgs();
	}

	if (launcherArgs.isHelp()) {
	    launcherArgs.restoreDefaults();
	    printHelp(parser);
	    return;
	}

	setupLogger();
	new Launcher(LauncherStrategyContext.createDefault()).run(launcherArgs);
    }

    private static void exitInvalidArgs() {
	System.err.println("Try '--help' for more information.");
	System.exit(ERROR_CODE_INVALID_ARGS);
    }

    private static void printHelp(CmdLineParser parser) {
	System.err.println("Usage:" + parser.printExample(OptionHandlerFilter.REQUIRED) + " [options]");
	System.err.println();
	parser.printUsage(new OutputStreamWriter(System.err), null, OptionHandlerFilter.ALL);
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
}

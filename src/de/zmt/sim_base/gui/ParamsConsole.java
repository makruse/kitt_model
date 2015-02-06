package de.zmt.sim_base.gui;

import java.awt.FileDialog;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import sim.display.*;
import sim.display.Console;
import sim.portrayal.Inspector;
import sim.util.gui.Utilities;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.sim_base.engine.ParamsSim;
import de.zmt.sim_base.gui.portrayal.inspector.ParamsInspector;

/** Adds saving / loading of xml parameters to standard UI */
public class ParamsConsole extends Console {
    private static final long serialVersionUID = 1L;

    private static final String PARAMETERS_MENU_TITLE = "Parameters";
    private static final String OPEN_PARAMETERS_ITEM_TEXT = "Open";
    private static final String SAVE_PARAMETERS_ITEM_TEXT = "Save";
    private static final String XML_FILENAME_SUFFIX = ".xml";
    private static final String SAVE_CONFIGURATION_FILE_DIALOG_TITLE = "Save Configuration File...";
    private static final String LOAD_CONFIGURATION_FILE_DIALOG_TITLE = "Load Configuration File...";

    private final String currentDir = KittSim.DEFAULT_INPUT_DIR;

    public ParamsConsole(GUIState gui) {
	super(gui);

	// add Parameters menu item
	JMenu paramsMenu = new JMenu(PARAMETERS_MENU_TITLE);
	getJMenuBar().add(paramsMenu);

	// add menu items for params saving / loading
	JMenuItem openParams = new JMenuItem(OPEN_PARAMETERS_ITEM_TEXT);
	if (SimApplet.isApplet())
	    openParams.setEnabled(false);
	openParams.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doParamsOpen();
	    }
	});
	paramsMenu.add(openParams);

	JMenuItem saveParams = new JMenuItem(SAVE_PARAMETERS_ITEM_TEXT);
	if (SimApplet.isApplet())
	    saveParams.setEnabled(false);
	saveParams.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doParamsSaveAs();
	    }
	});
	paramsMenu.add(saveParams);
    }

    /** Lets the user save the current modelparams under a specific filename. */
    public void doParamsSaveAs() {

	FileDialog fd = new FileDialog(this,
		SAVE_CONFIGURATION_FILE_DIALOG_TITLE, FileDialog.SAVE);
	fd.setFilenameFilter(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return Utilities.ensureFileEndsWith(name, XML_FILENAME_SUFFIX)
			.equals(name);
	    }
	});

	fd.setDirectory(currentDir);

	fd.setVisible(true);
	if (fd.getFile() != null) {
	    try {
		String path = fd.getDirectory() + fd.getFile();
		((ParamsSim) getSimulation().state).getParams()
			.writeToXml(path);

	    } catch (Exception e) {
		Utilities.informOfError(e,
			"Failed to save parameters to file: " + fd.getFile(),
			null);
	    }
	}
    }

    /**
     * Reverts the current configuration to the configuration stored under
     * filename.
     */
    public void doParamsOpen() {
	FileDialog fd = new FileDialog(this,
		LOAD_CONFIGURATION_FILE_DIALOG_TITLE, FileDialog.LOAD);
	fd.setFilenameFilter(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return Utilities.ensureFileEndsWith(name, XML_FILENAME_SUFFIX)
			.equals(name);
	    }
	});

	File file = new File(currentDir);
	fd.setDirectory(file.getPath());

	boolean pauseSet = false;
	if (getPlayState() == PS_PLAYING) {
	    // need to put into paused mode
	    pressPause();
	    pauseSet = true;
	}

	fd.setVisible(true);

	if (fd.getFile() != null) {
	    KittParams params;
	    try {
		params = KittParams.readFromXml(fd.getDirectory()
			+ fd.getFile());
	    } catch (Exception e) {
		Utilities.informOfError(e,
			"Failed to load parameters from file: " + fd.getFile(),
			null);
		return;
	    } finally {
		// continue again if pause was set
		if (pauseSet) {
		    pressPause();
		}
	    }
	    ((ParamsSim) getSimulation().state).setParams(params);

	    // if params inspector is used we will also set params there
	    Inspector modelInspector = getModelInspector();
	    if (modelInspector instanceof ParamsInspector) {
		((ParamsInspector) modelInspector).setParams(params);
	    }
	}

    }
}

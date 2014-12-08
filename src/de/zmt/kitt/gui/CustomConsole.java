package de.zmt.kitt.gui;

import java.awt.FileDialog;
import java.awt.event.*;
import java.io.*;

import javax.swing.JMenuItem;

import sim.display.*;
import sim.display.Console;
import sim.util.gui.Utilities;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.params.ModelParams;

/** Adds saving / loading of xml parameters to standard UI */
public class CustomConsole extends Console {
    private static final long serialVersionUID = 1L;

    private final String currentDir = Sim.DEFAULT_INPUT_DIR;

    public CustomConsole(Gui gui) {
	super(gui);

	// add menu items for params saving / loading
	JMenuItem openParams = new JMenuItem("Open parameters");
	if (SimApplet.isApplet())
	    openParams.setEnabled(false);
	openParams.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doParamsOpen();
	    }
	});
	getJMenuBar().getMenu(0).add(openParams);

	JMenuItem saveParams = new JMenuItem("Save parameters");
	if (SimApplet.isApplet())
	    saveParams.setEnabled(false);
	saveParams.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doParamsOpen();
	    }
	});
	getJMenuBar().getMenu(0).add(saveParams);
    }

    /** Lets the user save the current modelparams under a specific filename. */
    public void doParamsSaveAs() {

	FileDialog fd = new FileDialog(this, "Save Configuration File...",
		FileDialog.SAVE);
	fd.setFilenameFilter(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return Utilities.ensureFileEndsWith(name, ".xml").equals(name);
	    }
	});

	fd.setDirectory(currentDir);

	fd.setVisible(true);
	if (fd.getFile() != null) {
	    try {
		String path = fd.getDirectory() + fd.getFile();
		((Sim) getSimulation().state).getParams().writeToXml(path);

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
	FileDialog fd = new FileDialog(this, "Load Configuration File...",
		FileDialog.LOAD);
	fd.setFilenameFilter(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return Utilities.ensureFileEndsWith(name, ".xml").equals(name);
	    }
	});

	File file = new File(currentDir);
	fd.setDirectory(file.getPath());

	boolean failed = true;
	int originalPlayState = getPlayState();
	if (originalPlayState == PS_PLAYING) {
	    // need to put into paused mode
	    pressPause();
	}

	fd.setVisible(true);

	if (fd.getFile() != null) {
	    try {
		ModelParams params = ModelParams.readFromXml(fd.getDirectory()
			+ fd.getFile());
		((Sim) getSimulation().state).setParams(params);

		failed = false;
	    } catch (Exception e) {
		Utilities.informOfError(e,
			"Failed to load parameters from file: " + fd.getFile(),
			null);
		return;
	    }

	    // if we failed, reset play state. If we were stopped, do nothing
	    // (we're still stopped).
	    if (failed && originalPlayState == PS_PLAYING)
		pressPause();
	}

	// update inspectors
	getModelInspector().updateInspector();
    }
}

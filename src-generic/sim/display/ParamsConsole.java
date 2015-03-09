package sim.display;

import java.awt.FileDialog;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import sim.display.portrayal.inspector.ParamsInspector;
import sim.engine.ParamsSim;
import sim.engine.params.AbstractParams;
import sim.portrayal.Inspector;
import sim.util.gui.Utilities;

/** Adds saving / loading of xml parameters to standard UI */
public abstract class ParamsConsole extends Console {

    private static final long serialVersionUID = 1L;

    private static final String PARAMETERS_MENU_TITLE = "Parameters";
    private static final String NEW_PARAMETERS_ITEM_TEXT = "New";
    private static final String OPEN_PARAMETERS_ITEM_TEXT = "Open";
    private static final String SAVE_PARAMETERS_ITEM_TEXT = "Save";
    private static final String XML_FILENAME_SUFFIX = ".xml";
    private static final String SAVE_CONFIGURATION_FILE_DIALOG_TITLE = "Save Configuration File...";
    private static final String LOAD_CONFIGURATION_FILE_DIALOG_TITLE = "Load Configuration File...";

    private String currentDir = System.getProperty("user.dir");

    public ParamsConsole(GUIState gui) {
	super(gui);
	// add Parameters menu item
	JMenu paramsMenu = new JMenu(PARAMETERS_MENU_TITLE);
	getJMenuBar().add(paramsMenu);

	// add menu items for new params + saving / loading
	JMenuItem newParams = new JMenuItem(NEW_PARAMETERS_ITEM_TEXT);
	newParams.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		doParamsNew();
	    }
	});
	paramsMenu.add(newParams);

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
    private void doParamsSaveAs() {

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
	    String path = fd.getDirectory() + fd.getFile();
	    try {
		((ParamsSim) getSimulation().state).getParams()
			.writeToXml(path);
		currentDir = fd.getDirectory();

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
    private void doParamsOpen() {
	FileDialog fd = new FileDialog(this,
		LOAD_CONFIGURATION_FILE_DIALOG_TITLE, FileDialog.LOAD);
	fd.setFilenameFilter(new FilenameFilter() {
	    @Override
	    public boolean accept(File dir, String name) {
		return Utilities.ensureFileEndsWith(name, XML_FILENAME_SUFFIX)
			.equals(name);
	    }
	});

	fd.setDirectory(new File(currentDir).getPath());

	boolean pauseSet = false;
	if (getPlayState() == PS_PLAYING) {
	    // need to put into paused mode
	    pressPause();
	    pauseSet = true;
	}

	fd.setVisible(true);

	if (fd.getFile() != null) {
	    AbstractParams params;
	    String path = fd.getDirectory() + fd.getFile();
	    try {
		params = AbstractParams.readFromXml(path, getParamsClass());
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
	    currentDir = fd.getDirectory();
	    setParams(params);
	}
    }

    private void setParams(AbstractParams params) {
	((ParamsSim) getSimulation().state).setParams(params);
	// if params inspector is used we will also set params there
	Inspector modelInspector = getModelInspector();
	if (modelInspector instanceof ParamsInspector) {
	    ((ParamsInspector) modelInspector).setParams(params);
	}
    }

    private void doParamsNew() {
	AbstractParams defaultParams;
	try {
	    defaultParams = getParamsClass().newInstance();
	} catch (ReflectiveOperationException e) {
	    Utilities.informOfError(e,
		    "Unable to instantiate new Parameter object.", null);
	    return;
	}

	setParams(defaultParams);
    }

    protected void setCurrentDir(String currentDir) {
	this.currentDir = currentDir;
    }

    /**
     * Implement to provide {@link ParamsBase} child class. Needed for the
     * unmarshalling process triggered when loading parameters from an XML file.
     * 
     * @return {@link ParamsBase} child class to be used in XML loading.
     */
    protected abstract Class<? extends AbstractParams> getParamsClass();
}

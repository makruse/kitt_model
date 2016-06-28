package sim.display;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.thoughtworks.xstream.XStreamException;

import de.zmt.params.SimParams;
import de.zmt.util.ParamsUtil;
import sim.engine.ZmtSimState;
import sim.util.gui.Utilities;

/**
 * Parameters menu for {@link ZmtConsole}.
 * 
 * @author mey
 *
 */
class ParamsMenu extends JMenu {
    private static final long serialVersionUID = 1L;

    private static final String PARAMETERS_MENU_TITLE = "Parameters";
    private static final String NEW_PARAMETERS_ITEM_TEXT = "New";
    private static final String OPEN_PARAMETERS_ITEM_TEXT = "Open";
    private static final String SAVE_PARAMETERS_ITEM_TEXT = "Save";
    private static final String XML_FILENAME_SUFFIX = ".xml";
    private static final String SAVE_CONFIGURATION_FILE_DIALOG_TITLE = "Save Configuration File...";
    private static final String LOAD_CONFIGURATION_FILE_DIALOG_TITLE = "Load Configuration File...";

    private final Console console;
    private String currentDir = ZmtSimState.DEFAULT_INPUT_DIR.toString();

    public ParamsMenu(Console console) {
        super(PARAMETERS_MENU_TITLE);
        this.console = console;

        addMenuItems();
    }

    /** Adds menu items for new params + saving / loading. */
    private void addMenuItems() {
        JMenuItem newParams = new JMenuItem(NEW_PARAMETERS_ITEM_TEXT);
        newParams.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doParamsNew();
            }
        });
        add(newParams);

        JMenuItem openParams = new JMenuItem(OPEN_PARAMETERS_ITEM_TEXT);
        if (SimApplet.isApplet()) {
            openParams.setEnabled(false);
        }
        openParams.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doParamsOpen();
            }
        });
        add(openParams);

        JMenuItem saveParams = new JMenuItem(SAVE_PARAMETERS_ITEM_TEXT);
        if (SimApplet.isApplet()) {
            saveParams.setEnabled(false);
        }
        saveParams.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doParamsSaveAs();
            }
        });
        add(saveParams);
    }

    /** Lets the user save the current parameters under a specific filename. */
    private void doParamsSaveAs() {

        FileDialog fd = new FileDialog(console, SAVE_CONFIGURATION_FILE_DIALOG_TITLE, FileDialog.SAVE);
        fd.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Utilities.ensureFileEndsWith(name, XML_FILENAME_SUFFIX).equals(name);
            }
        });

        fd.setDirectory(currentDir);

        fd.setVisible(true);
        if (fd.getFile() != null) {
            Path path = Paths.get(fd.getDirectory() + fd.getFile());
            try {
                ParamsUtil.writeToXml(((ZmtSimState) console.getSimulation().state).getParams(), path);
                currentDir = fd.getDirectory();

            } catch (IOException | XStreamException e) {
                Utilities.informOfError(e, "Failed to save parameters to file: " + fd.getFile(), null);
            }
        }
    }

    /**
     * Reverts the current configuration to the configuration stored under
     * filename.
     */
    private void doParamsOpen() {
        FileDialog fd = new FileDialog(console, LOAD_CONFIGURATION_FILE_DIALOG_TITLE, FileDialog.LOAD);
        fd.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Utilities.ensureFileEndsWith(name, XML_FILENAME_SUFFIX).equals(name);
            }
        });

        fd.setDirectory(new File(currentDir).getPath());

        boolean pauseSet = false;
        if (console.getPlayState() == Console.PS_PLAYING) {
            // need to put into paused mode
            console.pressPause();
            pauseSet = true;
        }

        fd.setVisible(true);

        if (fd.getFile() != null) {
            SimParams simParams;
            Path path = Paths.get(fd.getDirectory(), fd.getFile());
            try {
                simParams = ParamsUtil.readFromXml(path,
                        ((ZmtSimState) console.getSimulation().state).getParamsClass());
            } catch (IOException | XStreamException e) {
                Utilities.informOfError(e, "Failed to load parameters from file: " + fd.getFile(), null);
                return;
            } finally {
                // continue again if pause was set
                if (pauseSet) {
                    console.pressPause();
                }
            }
            currentDir = fd.getDirectory();
            setParams(simParams);
        }
    }

    private void doParamsNew() {
        SimParams defaultParams;
        try {
            defaultParams = ((ZmtSimState) console.getSimulation().state).getParamsClass().newInstance();
        } catch (ReflectiveOperationException e) {
            Utilities.informOfError(e, "Unable to instantiate new Parameter object.", null);
            return;
        }

        setParams(defaultParams);
    }

    private void setParams(SimParams simParams) {
        ((ZmtSimState) console.getSimulation().state).setParams(simParams);
        console.buildModelInspector();
    }
}

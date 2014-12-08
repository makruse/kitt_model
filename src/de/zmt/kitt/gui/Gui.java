package de.zmt.kitt.gui;

import java.awt.Color;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import sim.display.*;
import sim.display.Console;
import sim.engine.SimState;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.gui.SimpleColorMap;
import de.zmt.kitt.gui.portrayal.*;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.*;

/**
 * The UI for Simulation.<br />
 * contains the controls and views of mason.<br />
 * and extends the panels for editing the model parameters.<br />
 * Shows the field with the moving agents and obstacles.<br />
 * 
 * @author oth
 * 
 */
public class Gui extends GUIState {
    /** shows the view with the field and the agents */
    private Display2D display;
    /** display frame */
    private JFrame displayFrame;
    /** display frame */
    private CustomConsole console;
    /** the simulation */
    private Sim sim;
    /** responsible to display field */
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();
    HabitatMapPortrayal habitatMapPortrayal;
    FastValueGridPortrayal2D foodGridPortrayal;
    MemoryCellsView memoryGridView;

    FishViewSimple fishViewSimple = null;

    JFrame pFrame = new JFrame();

    public Gui(String path) {
	super(new Sim(path));
	this.sim = (Sim) state;
    }

    public Gui(SimState state) {
	super(state);
    }

    public static String getName() {
	return "Kitty";
    }

    /**
     * called when start was pressed on the control panel. reinitializes the
     * displays and calls the sim.start() method
     */
    @Override
    public void start() {
	super.start();

	setupPortrayals();
	setupFrames();

	scheduleRepeatingImmediatelyAfter(new RateAdjuster(30));
    }

    /** assign the potrayals and scaling */
    private void setupPortrayals() {
	// kittyView = new FishView(Color.green,6.0,
	// sim.cfg.speciesList.size());
	fishViewSimple = new FishViewSimple(sim);
	// set Portrayals to display the agents
	// fieldPortrayal.setPortrayalForClass(Fish.class, kittyView);
	fieldPortrayal.setPortrayalForClass(Fish.class, fishViewSimple);
	sim = (Sim) state;
	fieldPortrayal.setField(sim.getEnvironment().getFishField());

	foodGridPortrayal = new FastValueGridPortrayal2D("food");
	foodGridPortrayal.setField(sim.getEnvironment().getFoodField());
	foodGridPortrayal.setMap(new SimpleColorMap(0.0, 14.0, new Color(0, 0,
		0), new Color(0, 255, 0)));
	display.attach(foodGridPortrayal, "food");

	habitatMapPortrayal = new HabitatMapPortrayal();
	display.attach(habitatMapPortrayal, "habitatMap");

	memoryGridView = new MemoryCellsView(sim, display.getWidth(),
		display.getHeight());
	display.attach(memoryGridView, "memory of selected fish", true);

	display.attach(fieldPortrayal, "field");

	TimeView timeView = new TimeView(this.sim);
	display.attach(timeView, "timeview");

	// reschedule the displayer
	display.reset();
	display.repaint();
    }

    private void setupFrames() {
	// display time series diagram with populations
	GraphView tsChart = new GraphView(sim);
	JFrame chartFrame = tsChart.create(this, "Fish Properties", "Time",
		"Val");
	chartFrame.setVisible(false);
	chartFrame.pack();
	this.console.registerFrame(chartFrame);
	tsChart.start();
	sim.schedule.scheduleRepeating(tsChart, 1); // sim.p.env.drawinterval
    }

    @Override
    public void init(Controller c) {
	super.init(c);
	console = (CustomConsole) c;

	double w = sim.getParams().environmentDefinition.fieldWidth;
	double h = sim.getParams().environmentDefinition.fieldHeight;

	display = new Display2D(w + 1, h + 1, this); // y contains the field
						     // height plus timeview
						     // height
	display.setBackdrop(new Color(0, 0, 0));
	display.setBackground(new Color(0, 0, 0));
	displayFrame = display.createFrame();
	displayFrame.setTitle("field Display");

	// register the frame so it appears in the "Display" list
	console.registerFrame(displayFrame);

	displayFrame.setVisible(true);

	JMenuItem menuItemOpen = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(1);
	menuItemOpen.setText("open configuration..");
	JMenuItem menuItemSaveAs = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(3);
	menuItemSaveAs.setText("save configuration as..");

	JMenuItem menuItemSave = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(2);
	JMenuItem menuItemNew = (JMenuItem) console.getJMenuBar().getMenu(0)
		.getMenuComponent(0);
	console.getJMenuBar().getMenu(0).remove(menuItemSave);
	console.getJMenuBar().getMenu(0).remove(menuItemNew);
    }

    // TODO not supported in this version
    @Override
    public void load(SimState state) {
	super.load(state);

	setupPortrayals();
	setupFrames();

	scheduleRepeatingImmediatelyAfter(new RateAdjuster(30));
    }

    @Override
    public void quit() {
	super.quit();
	if (displayFrame != null)
	    displayFrame.dispose();
	displayFrame = null;
	display = null;
    }

    @Override
    public Inspector getInspector() {
	TabbedInspector tabbedInspector = new TabbedInspector(false);

	Inspector simInspector = new SimpleInspector(sim, this);
	tabbedInspector.addInspector(simInspector, "Simulation");

	// add environment tab
	EnvironmentDefinition envDefs = sim.getParams().environmentDefinition;
	Inspector envInspector = new SimpleInspector(envDefs, this);
	tabbedInspector.addInspector(envInspector, envDefs.getTitle());

	// add tab for available species
	for (SpeciesDefinition def : sim.getParams().getSpeciesDefs()) {
	    Inspector speciesInspector = new SimpleInspector(def, this);
	    tabbedInspector.addInspector(speciesInspector, def.getTitle());
	}

	return tabbedInspector;
    }

    public static void main(String[] args) {
	// setup logging
	final InputStream inputStream = Gui.class
		.getResourceAsStream("logging.properties");
	try {
	    LogManager.getLogManager().readConfiguration(inputStream);
	} catch (final IOException e) {
	    Logger.getAnonymousLogger().severe(
		    "Could not load default logging.properties file");
	    Logger.getAnonymousLogger().severe(e.getMessage());
	}

	Console c = new CustomConsole(new Gui(Sim.DEFAULT_INPUT_DIR
		+ ModelParams.DEFAULT_FILENAME));
	c.setVisible(true);
    }
}

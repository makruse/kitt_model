package de.zmt.kitt.gui;

import java.awt.Color;
import java.io.*;
import java.util.logging.*;

import javax.swing.JFrame;

import sim.display.*;
import sim.display.Console;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import sim.portrayal.inspector.TabbedInspector;
import sim.util.Double2D;
import sim.util.gui.*;
import de.zmt.kitt.gui.portrayal.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.*;
import de.zmt.kitt.util.gui.HabitatColorMap;
import de.zmt.sim_base.gui.ParamsConsole;

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
    private static double DEFAULT_DISPLAY_WIDTH = 471;
    private static double DEFAULT_DISPLAY_HEIGHT = 708;
    /** Transparency for food color map (0x40 = 64) */
    private static final int FOOD_COLOR_TRANSPARENCY = 0x40FFFFFF;
    /**
     * {@link ColorMap} for {@link #foodGridPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final SimpleColorMap FOOD_COLOR_MAP = new SimpleColorMap(
	    0.0, Habitat.FOOD_MAX_ALL, new Color(0, 0, 0, 0), new Color(
		    FOOD_COLOR_TRANSPARENCY & Color.BLACK.getRGB(), true));
    /** shows the view with the field and the agents */
    private Display2D display;
    /** display frame */
    private JFrame displayFrame;
    /** display frame */
    private ParamsConsole console;
    /** the simulation */
    private Sim sim;
    /** responsible to display field */
    private final ContinuousPortrayal2D fishFieldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatGridPortrayal = new FastValueGridPortrayal2D(
	    true);
    private final FastValueGridPortrayal2D foodGridPortrayal = new FastValueGridPortrayal2D();
    private final MemoryCellsPortrayal memoryCellsPortrayal = new MemoryCellsPortrayal();
    private final ObjectGridPortrayal2D normalGridPortrayal = new ObjectGridPortrayal2D();

    /** memory cell values are displayed for the selected fish */
    private Fish selectedFish = null;

    public Gui(String path) {
	super(new Sim(path));
	this.sim = (Sim) state;
    }

    public Gui(SimState state) {
	super(state);
    }

    @Override
    public void init(Controller c) {
	super.init(c);
	console = (ParamsConsole) c;

	display = new Display2D(DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT,
		this);
	display.setBackdrop(Color.WHITE);
	displayFrame = display.createFrame();
	displayFrame.setTitle("field Display");

	// register the frame so it appears in the "Display" list
	console.registerFrame(displayFrame);

	displayFrame.setVisible(true);
    }

    /**
     * called when start was pressed on the control panel. reinitializes the
     * displays and calls the sim.start() method
     */
    @Override
    public void start() {
	super.start();

	setupPortrayals();
    }

    // TODO not supported in this version
    @Override
    public void load(SimState state) {
	super.load(state);

	setupPortrayals();
    }

    /** assign the potrayals and scaling */
    private void setupPortrayals() {
	Environment environment = sim.getEnvironment();
	display.insideDisplay.width = environment.getWidth();
	display.insideDisplay.height = environment.getHeight();
	display.setScale(1);
	displayFrame.pack();

	foodGridPortrayal.setField(environment.getFoodGrid());
	foodGridPortrayal.setMap(FOOD_COLOR_MAP);

	// set Portrayals to display the agents
	fishFieldPortrayal.setField(environment.getFishField());
	fishFieldPortrayal.setPortrayalForClass(Fish.class, new FishPortrayal(
		this));

	habitatGridPortrayal.setField(environment.getHabitatGrid());
	habitatGridPortrayal.setMap(new HabitatColorMap());

	normalGridPortrayal.setField(environment.getNormalGrid());
	// TODO without portrayal an inspector is displayed - keep that
	normalGridPortrayal.setPortrayalForClass(Double2D.class,
		new DirectionPortrayal());

	// displays need to be attached every time the simulation starts
	// size may change because of different habitat image
	display.attach(habitatGridPortrayal, "Habitats");
	display.attach(normalGridPortrayal, "Boundary normals");
	display.attach(foodGridPortrayal, "Food");
	display.attach(memoryCellsPortrayal, "Memory of Selected Fish", true);
	display.attach(new TimeView(), "Time View");
	display.attach(fishFieldPortrayal, "Fish Field");

	// reschedule the displayer
	display.reset();
	display.repaint();
    }

    @Override
    public void finish() {
	super.finish();
	// displays need to be reattached in start
	display.detatchAll();
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
	TabbedInspector tabbedInspector = new TabbedInspector();
	tabbedInspector.setVolatile(false);

	// add environment tab
	EnvironmentDefinition envDefs = sim.getParams().environmentDefinition;
	Inspector envInspector = Inspector.getInspector(envDefs, this, null);
	tabbedInspector.addInspector(envInspector, envDefs.getTitle());

	// add tab for available species
	for (SpeciesDefinition def : sim.getParams().getSpeciesDefs()) {
	    Inspector speciesInspector = Inspector
		    .getInspector(def, this, null);
	    tabbedInspector.addInspector(speciesInspector, def.getTitle());
	}

	return tabbedInspector;
    }

    public Fish getSelectedFish() {
	return selectedFish;
    }

    public void setSelectedFish(Fish selectedFish) {
	this.selectedFish = selectedFish;
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

	Console c = new ParamsConsole(new Gui(Sim.DEFAULT_INPUT_DIR
		+ Params.DEFAULT_FILENAME));
	c.setVisible(true);
    }
}

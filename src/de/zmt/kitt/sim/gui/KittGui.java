package de.zmt.kitt.sim.gui;

import java.awt.Color;
import java.io.*;
import java.util.logging.*;

import javax.swing.JFrame;

import org.jscience.physics.amount.AmountFormat;

import sim.display.*;
import sim.display.Console;
import sim.display.portrayal.inspector.ParamsInspector;
import sim.engine.SimState;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import sim.util.Double2D;
import sim.util.gui.SimpleColorMap;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.fish.Fish;
import de.zmt.kitt.sim.gui.portrayal.*;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.util.AmountUtil;
import de.zmt.kitt.util.gui.HabitatColorMap;

/**
 * The UI for Simulation.<br />
 * contains the controls and views of mason.<br />
 * and extends the panels for editing the model parameters.<br />
 * Shows the field with the moving agents and obstacles.<br />
 * 
 * @author oth
 * 
 */
public class KittGui extends GUIState {
    private static double DEFAULT_DISPLAY_WIDTH = 471;
    private static double DEFAULT_DISPLAY_HEIGHT = 708;

    /** Transparency for food color map (0x40 = 64) */
    private static final int FOOD_COLOR_TRANSPARENCY = 0x40FFFFFF;
    /**
     * {@link ColorMap} for {@link #foodGridPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final SimpleColorMap FOOD_COLOR_MAP = new SimpleColorMap(
	    0.0, Habitat.FOOD_MAX_GENERAL, new Color(0, 0, 0, 0), new Color(
		    FOOD_COLOR_TRANSPARENCY & Color.BLACK.getRGB(), true));

    /** shows the view with the field and the agents */
    private Display2D display;
    /** display frame */
    private JFrame displayFrame;
    /** display frame */
    private ParamsConsole console;
    /** the simulation */
    private final KittSim sim;
    /** responsible to display field */
    private final ContinuousPortrayal2D fishFieldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatGridPortrayal = new FastValueGridPortrayal2D(
	    true);
    private final FastValueGridPortrayal2D foodGridPortrayal = new FastValueGridPortrayal2D();
    private final MemoryCellsPortrayal memoryCellsPortrayal = new MemoryCellsPortrayal();
    private final ObjectGridPortrayal2D normalGridPortrayal = new ObjectGridPortrayal2D();

    /** memory cell values are displayed for the selected fish */
    private Fish selectedFish = null;

    public KittGui(String path) {
	this(new KittSim(path));
    }

    public KittGui(SimState state) {
	super(state);
	this.sim = (KittSim) state;
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

    @Override
    public Controller createController() {
	Console console = new KittConsole(this);
	console.setVisible(true);
	return console;
    }

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
	normalGridPortrayal.setPortrayalForClass(Double2D.class,
		new DirectionPortrayal());

	// displays need to be attached every time the simulation starts
	// size may change because of different habitat image
	display.attach(habitatGridPortrayal, "Habitats");
	display.attach(normalGridPortrayal, "Boundary normals");
	display.attach(foodGridPortrayal, "Food");
	display.attach(memoryCellsPortrayal, "Memory of Selected Fish", true);
	display.attach(fishFieldPortrayal, "Fish Field");
	display.attach(new TimeView(), "Time View");

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
	return new ParamsInspector(sim.getParams(), this);
    }

    public Fish getSelectedFish() {
	return selectedFish;
    }

    public void setSelectedFish(Fish selectedFish) {
	this.selectedFish = selectedFish;
    }

    public static void main(String[] args) {
	// setup logging
	InputStream inputStream = KittGui.class
		.getResourceAsStream("logging.properties");
	System.out.println(inputStream);
	try {
	    LogManager.getLogManager().readConfiguration(inputStream);
	} catch (IOException e) {
	    Logger.getAnonymousLogger().severe(
		    "Could not load default logging.properties file");
	    Logger.getAnonymousLogger().severe(e.getMessage());
	}

	// only exact digits when formatting amounts
	AmountFormat.setInstance(AmountUtil.FORMAT);

	new KittGui(KittSim.DEFAULT_INPUT_DIR + KittParams.DEFAULT_FILENAME)
		.createController();
    }
}

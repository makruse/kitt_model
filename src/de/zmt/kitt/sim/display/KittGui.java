package de.zmt.kitt.sim.display;

import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.util.logging.*;

import javax.swing.*;

import org.jscience.physics.amount.AmountFormat;

import sim.display.*;
import sim.display.Console;
import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.field.grid.*;
import sim.portrayal.Inspector;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import sim.util.*;
import sim.util.gui.SimpleColorMap;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.agent.fish.Fish;
import de.zmt.kitt.sim.params.KittParams;
import de.zmt.kitt.sim.portrayal.*;
import de.zmt.kitt.util.AmountUtil;
import de.zmt.kitt.util.gui.HabitatColorMap;
import de.zmt.sim.portrayal.inspector.ParamsInspector;
import de.zmt.sim.portrayal.portrayable.*;

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
    private static final String DISPLAY_TITLE = "Field Display";
    private static double DEFAULT_DISPLAY_WIDTH = 471;
    private static double DEFAULT_DISPLAY_HEIGHT = 708;

    /** Transparency for food color map (0x40 = 64) */
    private static final int FOOD_COLOR_TRANSPARENCY = 0x40FFFFFF;
    /**
     * {@link ColorMap} for {@link #foodGridPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final SimpleColorMap FOOD_COLOR_MAP = new SimpleColorMap(
	    0.0, Habitat.FOOD_RANGE_MAX, new Color(0, 0, 0, 0), new Color(
		    FOOD_COLOR_TRANSPARENCY & Color.BLACK.getRGB(), true));

    private static final String OUTPUT_INSPECTOR_NAME = "Output Inspector";

    /** shows the view with the field and the agents */
    private Display2D display;
    private JFrame displayFrame;

    /** Model inspector displaying defintions from Parameter object */
    private final ParamsInspector inspector;
    private final JMenuItem outputInspectorMenuItem = new JMenuItem("Show "
	    + OUTPUT_INSPECTOR_NAME);
    private final OutputInspectorListener outputInspectorListener = new OutputInspectorListener();

    // PORTRAYALS
    private final ContinuousPortrayal2D agentFieldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatGridPortrayal = new FastValueGridPortrayal2D(
	    true);
    private final FastValueGridPortrayal2D foodGridPortrayal = new FastValueGridPortrayal2D();
    private final MemoryPortrayal memoryPortrayal = new MemoryPortrayal();
    private final ObjectGridPortrayal2D normalGridPortrayal = new ObjectGridPortrayal2D();

    public KittGui(String path) {
	this(new KittSim(path));
    }

    private KittGui(KittSim state) {
	super(state);
	this.inspector = new ParamsInspector(state.getParams(), this);
    }

    @Override
    public void init(Controller c) {
	super.init(c);

	display = new Display2D(DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT,
		this);
	display.setBackdrop(Color.WHITE);
	displayFrame = display.createFrame();
	displayFrame.setTitle(DISPLAY_TITLE);

	// register the frame so it appears in the "Display" list
	c.registerFrame(displayFrame);

	displayFrame.setVisible(true);

	display.attach(habitatGridPortrayal, "Habitats");
	display.attach(normalGridPortrayal, "Boundary normals");
	display.attach(foodGridPortrayal, "Food");
	display.attach(memoryPortrayal, "Memory of Selected Fish");
	display.attach(agentFieldPortrayal, "Fish Field");

	outputInspectorMenuItem.setEnabled(false);
	outputInspectorMenuItem.addActionListener(outputInspectorListener);
	display.popup.add(outputInspectorMenuItem);
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

	setupPortrayals((GuiPortrayable) ((ProvidesPortrayable<?>) state)
		.providePortrayable());
    }

    @Override
    public void load(SimState state) {
	super.load(state);

	setupPortrayals((GuiPortrayable) ((ProvidesPortrayable<?>) state)
		.providePortrayable());
    }

    /** assign the potrayals and scaling */
    private void setupPortrayals(GuiPortrayable portrayable) {
	Continuous2D agentField = portrayable.getAgentField();
	display.insideDisplay.width = agentField.getWidth();
	display.insideDisplay.height = agentField.getHeight();
	display.setScale(1);
	displayFrame.pack();

	foodGridPortrayal.setField(portrayable.getFoodGrid());
	foodGridPortrayal.setMap(FOOD_COLOR_MAP);

	// set portrayal to display the agents
	agentFieldPortrayal.setField(portrayable.getAgentField());
	agentFieldPortrayal.setPortrayalForClass(Fish.class, new FishPortrayal(
		memoryPortrayal));

	habitatGridPortrayal.setField(portrayable.getHabitatGrid());
	habitatGridPortrayal.setMap(new HabitatColorMap());

	normalGridPortrayal.setField(portrayable.getNormalGrid());
	normalGridPortrayal.setPortrayalForClass(Double2D.class,
		new DirectionPortrayal());

	// register current output inspector on action listener
	Inspector outputInspector = Inspector.getInspector(
		((KittSim) state).getOutput(), this, null);
	outputInspector.setVolatile(true);
	outputInspectorListener.outputInspector = outputInspector;
	outputInspectorMenuItem.setEnabled(true);

	// reschedule the displayer
	display.reset();
	display.repaint();
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
	return inspector;
    }

    public static void main(String[] args) {
	// setup logging
	InputStream inputStream = KittGui.class
		.getResourceAsStream("logging.properties");
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

    private class OutputInspectorListener implements ActionListener {
	private Inspector outputInspector;

	@Override
	public void actionPerformed(ActionEvent e) {
	    outputInspector.updateInspector();
	    Bag inspectors = new Bag();
	    inspectors.add(outputInspector);
	    Bag names = new Bag();
	    names.add(OUTPUT_INSPECTOR_NAME);
	    controller.setInspectors(inspectors, names);
	}
    }

    public static interface GuiPortrayable extends Portrayable {
	Continuous2D getAgentField();

	DoubleGrid2D getFoodGrid();

	IntGrid2D getHabitatGrid();

	ObjectGrid2D getNormalGrid();
    }
}

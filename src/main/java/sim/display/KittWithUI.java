package sim.display;

import java.awt.Color;
import java.awt.event.*;

import javax.swing.*;

import org.jscience.physics.amount.AmountFormat;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.factory.EntityCreationListener;
import de.zmt.util.*;
import de.zmt.util.gui.HabitatColorMap;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import sim.portrayal.inspector.ParamsInspector;
import sim.portrayal.simple.*;
import sim.util.*;
import sim.util.gui.*;

/**
 * The UI for Simulation.<br />
 * contains the controls and views of mason.<br />
 * and extends the panels for editing the model parameters.<br />
 * Shows the field with the moving agents and obstacles.<br />
 * 
 * @author oth
 * @author cmeyer
 * 
 */
public class KittWithUI extends GUIState {
    private static final String DISPLAY_TITLE = "Field Display";
    private static final double DEFAULT_DISPLAY_WIDTH = 471;
    private static final double DEFAULT_DISPLAY_HEIGHT = 708;

    /** Transparency for food color map (0x40 = 64) */
    private static final int FOOD_COLOR_TRANSPARENCY = 0x40FFFFFF;
    /**
     * {@link ColorMap} for {@link #foodGridPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final SimpleColorMap FOOD_COLOR_MAP = new SimpleColorMap(0.0, Habitat.MAX_FOOD_RANGE,
	    new Color(0, 0, 0, 0), new Color(FOOD_COLOR_TRANSPARENCY & Color.BLACK.getRGB(), true));

    private static final String OUTPUT_INSPECTOR_NAME = "Output Inspector";

    private static final double FISH_TRAIL_LENGTH = 15;
    private static final Color FISH_TRAIL_MIN_COLOR = Color.RED;
    /** Transparent red */
    private static final Color FISH_TRAIL_MAX_COLOR = new Color(0x00FFFFFF & FISH_TRAIL_MIN_COLOR.getRGB(), true);

    /** shows the view with the field and the agents */
    private Display2D display;
    private JFrame displayFrame;

    /** Model inspector displaying definitions from Parameter object */
    private final ParamsInspector inspector;
    private final JMenuItem outputInspectorMenuItem = new JMenuItem("Show " + OUTPUT_INSPECTOR_NAME);
    private final OutputInspectorListener outputInspectorListener = new OutputInspectorListener();

    // PORTRAYALS
    private final ContinuousPortrayal2D agentFieldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatGridPortrayal = new FastValueGridPortrayal2D(true);
    private final FastValueGridPortrayal2D foodGridPortrayal = new FastValueGridPortrayal2D();
    private final MemoryPortrayal memoryPortrayal = new MemoryPortrayal();
    private final ObjectGridPortrayal2D normalGridPortrayal = new ObjectGridPortrayal2D();
    private final ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();

    public KittWithUI(Kitt state) {
	super(state);

	// only exact digits when formatting amounts
	AmountFormat.setInstance(AmountUtil.FORMAT);
	this.inspector = new ParamsInspector(state.getParams(), this);
	state.getEntityCreationHandler().addListener(new MyEntityCreationListener());
    }

    @Override
    public void init(Controller c) {
	super.init(c);

	display = new Display2D(DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT, this);
	displayFrame = display.createFrame();
	displayFrame.setTitle(DISPLAY_TITLE);

	// register the frame so it appears in the "Display" list
	c.registerFrame(displayFrame);

	displayFrame.setVisible(true);

	display.attach(habitatGridPortrayal, "Habitats");
	display.attach(normalGridPortrayal, "Boundary normals");
	display.attach(foodGridPortrayal, "Food");
	display.attach(memoryPortrayal, "Memory of Selected Fish");
	display.attach(trailsPortrayal, "Trail of Selected Fish");
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

	setupPortrayals(((Kitt) state).getEnvironment());
    }

    @Override
    public void load(SimState state) {
	super.load(state);

	setupPortrayals(((Kitt) state).getEnvironment());
    }

    private void setupPortrayals(Entity environment) {
	AgentWorld agentWorld = environment.get(AgentWorld.class);
	display.insideDisplay.width = agentWorld.getWidth();
	display.insideDisplay.height = agentWorld.getHeight();
	display.setScale(1);
	displayFrame.pack();

	foodGridPortrayal.setField(environment.get(FoodMap.class).providePortrayable().getField());
	foodGridPortrayal.setMap(FOOD_COLOR_MAP);

	// set portrayal to display the agents
	Object agentField = agentWorld.providePortrayable().getField();
	agentFieldPortrayal.setField(agentField);
	trailsPortrayal.setField(agentField);

	habitatGridPortrayal.setField(environment.get(HabitatMap.class).providePortrayable().getField());
	habitatGridPortrayal.setMap(new HabitatColorMap());

	normalGridPortrayal.setField(environment.get(NormalMap.class).getField());
	normalGridPortrayal.setPortrayalForClass(Double2D.class, new DirectionPortrayal());

	// register current output inspector on action listener
	Inspector outputInspector = Inspector.getInspector(((Kitt) state).getOutput(), this, null);
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
	if (displayFrame != null) {
	    displayFrame.dispose();
	}
	displayFrame = null;
	display = null;
    }

    @Override
    public Inspector getInspector() {
	return inspector;
    }

    private class MyEntityCreationListener implements EntityCreationListener {
	@Override
	public void onCreateEntity(Entity entity) {
	    // add only agents that move
	    if (!entity.has(Moving.class)) {
		return;
	    }
	    // trails portrayal need to be set for every agent individually
	    SimplePortrayal2D portrayal = new TrailedPortrayal2D(KittWithUI.this, new AgentPortrayal(memoryPortrayal),
		    trailsPortrayal, FISH_TRAIL_LENGTH, FISH_TRAIL_MIN_COLOR, FISH_TRAIL_MAX_COLOR);
	    agentFieldPortrayal.setPortrayalForObject(entity, new MovablePortrayal2D(portrayal));
	    trailsPortrayal.setPortrayalForObject(entity, portrayal);
	}

	@Override
	public void onRemoveEntity(Entity entity) {
	    agentFieldPortrayal.setPortrayalForObject(entity, null);
	    trailsPortrayal.setPortrayalForObject(entity, null);
	}
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
}

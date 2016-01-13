package sim.display;

import static javax.measure.unit.NonSI.MINUTE;

import java.awt.Color;

import javax.swing.JFrame;

import org.jscience.physics.amount.*;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.factory.EntityCreationListener;
import de.zmt.util.*;
import de.zmt.util.gui.HabitatColorMap;
import sim.engine.*;
import sim.params.def.*;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.*;
import sim.portrayal.simple.*;
import sim.util.Double2D;
import sim.util.gui.*;

/**
 * The UI for Simulation.<br />
 * contains the controls and views of mason.<br />
 * and extends the panels for editing the model parameters.<br />
 * Shows the field with the moving agents and obstacles.<br />
 * 
 * @author oth
 * @author mey
 * 
 */
public class KittWithUI extends ZmtGUIState {
    private static final String DISPLAY_TITLE = "Field Display";
    private static final double DEFAULT_DISPLAY_WIDTH = 471;
    private static final double DEFAULT_DISPLAY_HEIGHT = 708;

    private static final int FOOD_ALPHA = 0x40;
    private static final int POTENTIALS_ALPHA = 0x80;
    /**
     * {@link ColorMap} for {@link #foodGridPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final ColorMap FOOD_COLOR_MAP = ColorMapFactory.createWithAlpha(0, Habitat.MAX_FOOD_RANGE, 0,
	    FOOD_ALPHA, Color.BLACK);
    private static final ColorMap FOOD_POTENTIALS_COLOR_MAP = ColorMapFactory
	    .createForAttractivePotentials(POTENTIALS_ALPHA);
    private static final ColorMap RISK_POTENTIALS_COLOR_MAP = ColorMapFactory
	    .createForRepulsivePotentials(POTENTIALS_ALPHA);

    private static final int FISH_TRAIL_LENGTH_VALUE_MINUTE = 20;
    /**
     * Length of painted trail in steps for
     * {@value #FISH_TRAIL_LENGTH_VALUE_MINUTE} minutes.
     */
    private static final double FISH_TRAIL_LENGTH = Amount.valueOf(FISH_TRAIL_LENGTH_VALUE_MINUTE, MINUTE)
	    .to(UnitConstants.SIMULATION_TIME).divide(EnvironmentDefinition.STEP_DURATION).getEstimatedValue();
    private static final Color FISH_TRAIL_MIN_COLOR = Color.RED;
    /** Transparent red */
    private static final Color FISH_TRAIL_MAX_COLOR = new Color(0x00FFFFFF & FISH_TRAIL_MIN_COLOR.getRGB(), true);

    private static final String ADD_OPTIONAL_MENU_ITEM_TITLE = "Species";

    /** shows the view with the field and the agents */
    private Display2D display;
    private JFrame displayFrame;

    // PORTRAYALS
    private final ContinuousPortrayal2D agentFieldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatGridPortrayal = new FastValueGridPortrayal2D(true);
    private final FastValueGridPortrayal2D foodGridPortrayal = new FastValueGridPortrayal2D();
    private final MemoryPortrayal memoryPortrayal = new MemoryPortrayal();
    private final ObjectGridPortrayal2D normalGridPortrayal = new ObjectGridPortrayal2D();
    private final ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();

    // FLOW / POTENTIAL MAP PORTRAYALS
    private final ObjectGridPortrayal2D globalFlowPortrayal = new ObjectGridPortrayal2D();
    private final FastValueGridPortrayal2D foodPotentialsPortrayal = new FastValueGridPortrayal2D();
    private final FastValueGridPortrayal2D riskPotentialsPortrayal = new FastValueGridPortrayal2D();

    static {
	// only exact digits when formatting amounts
	AmountFormat.setInstance(AmountUtil.FORMAT);
    }

    public KittWithUI(Kitt state) {
	super(state);
	state.getEntityCreationHandler().addListener(new MyEntityCreationListener());
    }

    @Override
    public void init(Controller controller) {
	super.init(controller);

	display = new Display2D(DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT, this);
	displayFrame = display.createFrame();
	displayFrame.setTitle(DISPLAY_TITLE);

	// register the frame so it appears in the "Display" list
	controller.registerFrame(displayFrame);

	displayFrame.setVisible(true);

	display.attach(habitatGridPortrayal, "Habitats");
	display.attach(normalGridPortrayal, "Boundary normals");
	display.attach(foodGridPortrayal, "Food");
	display.attach(memoryPortrayal, "Memory of Selected Fish");
	display.attach(trailsPortrayal, "Trail of Selected Fish");
	display.attach(agentFieldPortrayal, "Fish Field");
	display.attach(globalFlowPortrayal, "Global Flow", false);
	display.attach(foodPotentialsPortrayal, "Food Potentials", false);
	display.attach(riskPotentialsPortrayal, "Risk Potentials", false);
    }

    @Override
    public Controller createController() {
	ZmtConsole console = new ZmtConsole(this);
	console.addOptionalDefinitionMenuItem(SpeciesDefinition.class, ADD_OPTIONAL_MENU_ITEM_TITLE);
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

	setupPathfindingPortrayals(environment);

	// reschedule the displayer
	display.reset();
	display.repaint();
    }

    private void setupPathfindingPortrayals(Entity environment) {
	final GlobalFlowMap globalFlowMap = environment.get(GlobalFlowMap.class);
	globalFlowPortrayal.setField(globalFlowMap.providePortrayable().getField());
	globalFlowPortrayal.setPortrayalForClass(Double2D.class, new DirectionPortrayal());

	foodPotentialsPortrayal.setField(globalFlowMap.provideFoodPotentialsPortrayable().getField());
	foodPotentialsPortrayal.setMap(FOOD_POTENTIALS_COLOR_MAP);

	riskPotentialsPortrayal.setField(globalFlowMap.provideRiskPotentialsPortrayable().getField());
	riskPotentialsPortrayal.setMap(RISK_POTENTIALS_COLOR_MAP);

	// update global flow map before to draw the most recent version
	scheduleRepeatingImmediatelyBefore(new Steppable() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void step(SimState state) {
		globalFlowMap.updateIfDirtyAll();
	    }
	});
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
}

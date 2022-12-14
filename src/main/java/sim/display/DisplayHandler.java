package sim.display;

import static javax.measure.unit.NonSI.MINUTE;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.measure.quantity.Duration;
import javax.swing.JFrame;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityListener;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.ecs.component.environment.WorldDimension;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import de.zmt.util.gui.HabitatColorMap;
import sim.engine.Kitt;
import sim.engine.ZmtSimState;
import sim.field.continuous.Continuous2D;
import sim.portrayal.AgentPortrayal;
import sim.portrayal.MemoryPortrayal;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
import sim.portrayal.simple.MovablePortrayal2D;
import sim.portrayal.simple.TrailedPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.ColorMapFactory;

/**
 * {@link GuiListener} for creating and handling the display. All portrayals are
 * attached and set up when needed.
 * 
 * @author mey
 *
 */
class DisplayHandler implements GuiListener {
    // DISPLAY
    private static final String DISPLAY_TITLE = "Field Display";
    private static final double DEFAULT_DISPLAY_WIDTH = 500;
    private static final double DEFAULT_DISPLAY_HEIGHT = 750;

    // VALUE NAMES
    private static final String HABITAT_VALUE_NAME = "habitat ordinal";
    private static final String FOOD_DENSITY_VALUE_NAME = "food density";
    private static final String FOOD_POTENTIAL_VALUE_NAME = "food potential";
    private static final String RISK_POTENTIAL_VALUE_NAME = "risk potential";

    // PORTRAYAL NAMES
    private static final String HABITAT_MAP_PORTRAYAL_NAME = "Habitats";
    private static final String FOOD_MAP_PORTRAYAL_NAME = "Food";
    private static final String MEMORY_PORTRAYAL_NAME = "Memory of Selected Fish";
    private static final String TRAIL_PORTRAYAL_NAME = "Trail of Selected Fish";
    private static final String AGENT_WORLD_PORTRAYAL_NAME = "Fish Field";
    private static final String FOOD_POTENTIALS_PORTRAYAL_NAME = "Food Potentials";
    private static final String RISK_POTENTIALS_PORTRAYAL_NAME = "Risk Potentials";

    // COLOR MAPS
    private static final int FOOD_ALPHA = 0x40;
    private static final int POTENTIALS_ALPHA = 0x80;
    /**
     * {@link ColorMap} for {@link #foodMapPortrayal} from fully transparent to
     * slightly transparent black, so that the habitat show through.
     */
    private static final ColorMap FOOD_COLOR_MAP = ColorMapFactory.createWithAlpha(0, Habitat.MAX_FOOD_RANGE, 0,
            FOOD_ALPHA, Color.BLACK);
    private static final ColorMap FOOD_POTENTIALS_COLOR_MAP = ColorMapFactory
            .createForAttractivePotentials(POTENTIALS_ALPHA);
    private static final ColorMap RISK_POTENTIALS_COLOR_MAP = ColorMapFactory
            .createForRepulsivePotentials(POTENTIALS_ALPHA);

    /** Length of simulation time covered by the trail. */
    private static final Amount<Duration> FISH_TRAIL_LENGTH = Amount.valueOf(20, MINUTE)
            .to(UnitConstants.SIMULATION_TIME);
    private static final Color FISH_TRAIL_MIN_COLOR = Color.RED;
    /** Transparent red */
    private static final Color FISH_TRAIL_MAX_COLOR = new Color(0x00FFFFFF & FISH_TRAIL_MIN_COLOR.getRGB(), true);

    /** Shows the view with the field and the agents. */
    private final Display2D display;
    private final JFrame displayFrame;
    private final GUIState guiState;
    private final AgentListener agentListener = new AgentListener();
    private AgentWorld agentWorld;

    // PORTRAYALS
    private final ContinuousPortrayal2D agentWorldPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D habitatMapPortrayal = new FastValueGridPortrayal2D(HABITAT_VALUE_NAME, true);
    private final FastValueGridPortrayal2D foodMapPortrayal = new FastValueGridPortrayal2D(FOOD_DENSITY_VALUE_NAME);
    private final MemoryPortrayal memoryPortrayal = new MemoryPortrayal();
    private final ContinuousPortrayal2D trailsPortrayal = new ContinuousPortrayal2D();
    private final FastValueGridPortrayal2D foodPotentialsPortrayal = new FastValueGridPortrayal2D(
            FOOD_POTENTIAL_VALUE_NAME);
    private final Map<SpeciesDefinition, ValueGridPortrayal2D> riskPortrayals = new HashMap<>();

    public DisplayHandler(GUIState guiState) {
        this.guiState = guiState;
        display = new Display2D(DEFAULT_DISPLAY_WIDTH, DEFAULT_DISPLAY_HEIGHT, guiState);

        displayFrame = display.createFrame();
        displayFrame.setTitle(DISPLAY_TITLE);

        ((Kitt) guiState.state).getEntityCreationHandler().getManager().addListener(agentListener);
        attachPortrayals();
    }

    /**
     * Returns the agent world, creating it if necessary.
     * 
     * @return the agent world
     */
    private AgentWorld obtainAgentWorld() {
        if (agentWorld == null) {
            WorldDimension worldDimension = ((Kitt) guiState.state).getEnvironment().get(WorldDimension.class);
            agentWorld = new AgentWorld(worldDimension.getWidth(), worldDimension.getHeight());
            agentWorld.setStoppable(guiState.scheduleRepeatingImmediatelyAfter(agentWorld));
        }
        return agentWorld;
    }

    @Override
    public void inited(Controller controller) {
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    /** Sets up all portrayals and re-attaches them if needed. {@inheritDoc} */
    @Override
    public void started(ZmtSimState state) {
        setupPortrayals(((Kitt) state).getEnvironment());
    }

    @Override
    public void loaded(ZmtSimState oldState, ZmtSimState loadedState) {
        // remove listener from old state
        ((Kitt) oldState).getEntityCreationHandler().getManager().removeListener(agentListener);

        // add listener to new state
        KittEntityCreationHandler entityCreationHandler = ((Kitt) loadedState).getEntityCreationHandler();
        EntityManager manager = entityCreationHandler.getManager();
        manager.addListener(agentListener);
        setupPortrayals(((Kitt) loadedState).getEnvironment());

        // make loaded entities portray in display
        manager.getAllEntitiesPossessingComponent(Moving.class).stream()
                .forEach(uuid -> agentListener.add(uuid));
    }

    @Override
    public void finished(ZmtSimState state) {
        agentWorld.stop();
        agentWorld = null;
    }

    /**
     * Sets up all portrayals and re-attaches them if needed.
     * 
     * @param environment
     */
    private void setupPortrayals(Entity environment) {
        FoodMap foodMap = environment.get(FoodMap.class);
        int width = foodMap.getWidth();
        int height = foodMap.getHeight();

        setupFieldPortrayals(environment);
        setupPathfindingPortrayals(environment);

        /*
         * If map dimensions have changed, displays need to be re-attached.
         * Otherwise their dimensions are not updated.
         */
        if (updateInnerDisplaySize(width, height) || cleanOrphanedPortrayals(environment)) {
            attachPortrayals();
        }

        display.reset();
        display.repaint();
    }

    private void setupFieldPortrayals(Entity environment) {
        foodMapPortrayal.setField(environment.get(FoodMap.class).providePortrayable().getField());
        foodMapPortrayal.setMap(FOOD_COLOR_MAP);

        // set portrayal to display the agents
        Continuous2D agentField = obtainAgentWorld().providePortrayable().getField();
        agentWorldPortrayal.setField(agentField);
        trailsPortrayal.setField(agentField);

        habitatMapPortrayal.setField(environment.get(HabitatMap.class).providePortrayable().getField());
        habitatMapPortrayal.setMap(new HabitatColorMap());
    }

    private void setupPathfindingPortrayals(Entity environment) {
        SpeciesPathfindingMaps.Container speciesPathfindingMaps = environment
                .get(SpeciesPathfindingMaps.Container.class);
        GlobalPathfindingMaps globalPathfindingMaps = environment.get(GlobalPathfindingMaps.class);

        foodPotentialsPortrayal.setField(globalPathfindingMaps.getFoodPotentialMap().providePortrayable().getField());
        foodPotentialsPortrayal.setMap(FOOD_POTENTIALS_COLOR_MAP);

        // setup a risk potentials portrayal for every species
        for (Map.Entry<SpeciesDefinition, SpeciesPathfindingMaps> entry : speciesPathfindingMaps.entrySet()) {
            SpeciesDefinition definition = entry.getKey();
            ValueGridPortrayal2D portrayal = riskPortrayals.get(definition);

            if (portrayal == null) {
                portrayal = new FastValueGridPortrayal2D(RISK_POTENTIAL_VALUE_NAME + " for " + definition.getTitle(),
                        true);
                portrayal.setMap(RISK_POTENTIALS_COLOR_MAP);
                riskPortrayals.put(definition, portrayal);
                attachRiskPotentialPortrayal(definition, portrayal);
            }

            portrayal.setField(entry.getValue().provideRiskPotentialsPortrayable().getField());
        }
    }

    private boolean updateInnerDisplaySize(int width, int height) {
        if (width != display.insideDisplay.width || height != display.insideDisplay.height) {
            display.insideDisplay.width = width;
            display.insideDisplay.height = height;
            return true;
        }
        return false;
    }

    /**
     * Cleans orphaned risk portrayals. In case if a species was deleted, the
     * portrayals for its risk values become orphaned and need to be deleted. If
     * this is the case all displays need to be re-attached, because individual
     * displays cannot be detached.
     * 
     * @param environment
     * @return <code>true</code> if orphaned displays were found and deleted
     */
    private boolean cleanOrphanedPortrayals(final Entity environment) {
        return riskPortrayals.keySet().retainAll(environment.get(SpeciesPathfindingMaps.Container.class).keySet());
    }

    /** Attaches displays. Previously attached displays are removed before. */
    private void attachPortrayals() {
        display.detachAll();
        display.attach(habitatMapPortrayal, HABITAT_MAP_PORTRAYAL_NAME);
        display.attach(foodMapPortrayal, FOOD_MAP_PORTRAYAL_NAME, false);
        display.attach(memoryPortrayal, MEMORY_PORTRAYAL_NAME, false);
        display.attach(trailsPortrayal, TRAIL_PORTRAYAL_NAME);
        display.attach(agentWorldPortrayal, AGENT_WORLD_PORTRAYAL_NAME);
        display.attach(foodPotentialsPortrayal, FOOD_POTENTIALS_PORTRAYAL_NAME, false);
        for (Map.Entry<SpeciesDefinition, ValueGridPortrayal2D> entry : riskPortrayals.entrySet()) {
            attachRiskPotentialPortrayal(entry.getKey(), entry.getValue());
        }

        display.setScale(1);
        displayFrame.pack();
    }

    private void attachRiskPotentialPortrayal(SpeciesDefinition definition, ValueGridPortrayal2D portrayal) {
        display.attach(portrayal, RISK_POTENTIALS_PORTRAYAL_NAME + "(" + definition.getTitle() + ")", false);
    }

    /** Disposes the display frame. */
    @Override
    public void quit() {
        ((Kitt) guiState.state).getEntityCreationHandler().getManager().removeListener(agentListener);
        displayFrame.dispose();
    }

    /**
     * Associates each created agent with its proper portrayal.
     * 
     * @author mey
     *
     */
    private class AgentListener implements EntityListener {

        @Override
        public void onAddComponent(UUID uuid, Component component, EntityManager manager) {
            // add only agents that move
            if (!(component instanceof Moving)) {
                return;
            }
            add(uuid);
        }

        /**
         * Adds an agent to GUI portrayals.
         * 
         * @param uuid
         *            the {@link UUID} of the agent entity to be added
         */
        private void add(UUID uuid) {
            Entity entity = ((Kitt) guiState.state).getEntityCreationHandler().loadEntity(uuid);

            obtainAgentWorld().addAgent(entity);
            double trailLengthValue = FISH_TRAIL_LENGTH
                    .divide(((Kitt) guiState.state).getEnvironment().get(EnvironmentDefinition.class).getStepDuration())
                    .getEstimatedValue();

            // trails portrayal need to be set for every agent individually
            SimplePortrayal2D portrayal = new TrailedPortrayal2D(guiState, new AgentPortrayal(memoryPortrayal),
                    trailsPortrayal, trailLengthValue, FISH_TRAIL_MIN_COLOR, FISH_TRAIL_MAX_COLOR);
            agentWorldPortrayal.setPortrayalForObject(entity, new MovablePortrayal2D(portrayal));
            trailsPortrayal.setPortrayalForObject(entity, portrayal);
        }

        @Override
        public void onRemoveEntity(UUID uuid, EntityManager manager) {
            // remove only agents that move
            if (manager.hasComponent(uuid, Moving.class)) {
                remove(uuid);
            }
        }

        @Override
        public void onRemoveComponent(UUID uuid, Component component, EntityManager manager) {
            if (component instanceof Moving) {
                remove(uuid);
            }
        }

        /**
         * Removes an entity from GUI portrayal.
         * 
         * @param uuid
         *            the {@link UUID} of the agent entity to be removed
         */
        private void remove(UUID uuid) {
            Entity entity = ((Kitt) guiState.state).getEntityCreationHandler().loadEntity(uuid);
            obtainAgentWorld().removeAgent(entity);
            agentWorldPortrayal.setPortrayalForObject(entity, null);
            trailsPortrayal.setPortrayalForObject(entity, null);
        }
    }
}

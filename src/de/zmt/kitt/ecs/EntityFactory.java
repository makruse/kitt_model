package de.zmt.kitt.ecs;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.engine.*;
import sim.field.grid.*;
import sim.util.Double2D;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Reproducing.Sex;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.storage.*;
import de.zmt.kitt.util.*;
import ec.util.MersenneTwisterFast;
import ecs.*;

public class EntityFactory implements Serializable {
    private static final long serialVersionUID = 1L;

    // FISH
    private static final Habitat FISH_SPAWN_HABITAT = Habitat.CORALREEF;
    private static final Habitat RESTING_HABITAT = Habitat.CORALREEF;
    private static final Habitat FORAGING_HABITAT = Habitat.SEAGRASS;
    private static final double FEMALE_PROBABILITY = 0.5;
    private static final int FISH_ORDERING = 0;

    // ENVIRONMENT
    private static final String ENVIRONMENT_ENTITY_NAME = "Environment";
    /** Environment needs to be updated after the agents */
    private static final int ENVIRONMENT_ORDERING = 1;

    private final EntityManager manager;
    private final MersenneTwisterFast random;
    private final Schedule schedule;

    private final Collection<EntityCreationListener> listeners = new LinkedList<>();

    public EntityFactory(EntityManager entityManager, SimState state) {
	this.manager = entityManager;
	this.random = state.random;
	this.schedule = state.schedule;
    }

    /**
     * Creates new environment entity and adds it to the schedule.
     * 
     * @param definition
     * @return environment entity
     */
    public Entity createEnvironment(EnvironmentDefinition definition) {
	BufferedImage mapImage = MapUtil.loadMapImage(KittSim.DEFAULT_INPUT_DIR
		+ definition.getMapImageFilename());
	double mapScale = definition.getMapScale();

	// create fields
	IntGrid2D habitatGrid = MapUtil.createHabitatGridFromMap(random,
		mapImage);
	ObjectGrid2D normalGrid = MapUtil
		.createNormalGridFromHabitats(habitatGrid);
	DoubleGrid2D foodGrid = MapUtil.createFoodFieldFromHabitats(
		habitatGrid, random, mapScale);

	// gather components
	Collection<Component> components = Arrays.asList(
		definition,
		new AgentField(mapImage.getWidth() / mapScale, mapImage
			.getHeight() / mapScale), new FoodField(foodGrid),
		new HabitatField(habitatGrid, mapScale), new NormalField(
			normalGrid), new SimulationTime(
			EnvironmentDefinition.START_INSTANT));

	Entity environment = new Entity(manager, ENVIRONMENT_ENTITY_NAME,
		components);
	schedule.scheduleRepeating(schedule.getTime() + 1, environment,
		ENVIRONMENT_ORDERING);
	return environment;
    }

    /**
     * Create fish according to SpeciesDefinitions.
     * 
     * @see #createFish(MersenneTwisterFast, Schedule, EntityManager, Entity,
     *      SpeciesDefinition, Double2D)
     * @see SpeciesDefinition#getInitialNum()
     * @param environment
     * @param speciesDefs
     */
    public void createInitialFish(Entity environment,
	    Collection<SpeciesDefinition> speciesDefs) {
	for (SpeciesDefinition speciesDefinition : speciesDefs) {
	    for (int i = 0; i < speciesDefinition.getInitialNum(); i++) {
		Double2D position = environment.get(HabitatField.class)
			.generateRandomPosition(random, FISH_SPAWN_HABITAT);
		createFish(environment, speciesDefinition, position);
	    }
	}
    }

    /**
     * Create a new fish and add it to schedule and agent field.
     * 
     * @param environment
     *            entity with agent and habitat field
     * @param definition
     * @param position
     *            where the fish spawns
     * @return fish entity
     */
    public Entity createFish(Entity environment, SpeciesDefinition definition,
	    Double2D position) {
	final AgentField agentField = environment.get(AgentField.class);
	HabitatField habitatField = environment.get(HabitatField.class);

	// gather fish components
	Collection<Component> components = createFishComponents(definition,
		position, agentField, habitatField);
	final Entity fish = new Entity(manager, definition.getSpeciesName(),
		components);
	addCompartmentsTo(fish);

	// add fish to schedule and field
	agentField.addAgent(fish);
	final Stoppable scheduleStoppable = schedule.scheduleRepeating(
		schedule.getTime() + 1.0, FISH_ORDERING, fish);

	// create stoppable triggering removal of fish from schedule and field
	fish.setStoppable(new Stoppable() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void stop() {
		scheduleStoppable.stop();
		agentField.removeAgent(fish);
	    }
	});

	for (EntityCreationListener listener : listeners) {
	    listener.onCreateFish(fish);
	}
	return fish;
    }

    // TODO speedup from constants?
    private Collection<Component> createFishComponents(
	    SpeciesDefinition definition, Double2D position,
	    final AgentField agentField, HabitatField habitatField) {
	// compute initial values
	Amount<Duration> initialAge = SpeciesDefinition.getInitialAge();
	Amount<Length> initialLength = FormulaUtil.expectedLength(
		definition.getMaxLength(),
		definition.getGrowthCoeff(), initialAge,
		definition.getBirthLength());
	Amount<Mass> initialBiomass = FormulaUtil.expectedMass(
		definition.getLengthMassCoeff(), initialLength,
		definition.getLengthMassExponent());
	Amount<Power> initialStandardMetabolicRate = FormulaUtil
		.standardMetabolicRate(initialBiomass);

	// instantiate components
	Collection<Component> components = new LinkedList<>();
	components.addAll(Arrays.asList(definition, new Aging(initialAge),
		new Metabolizing(initialStandardMetabolicRate), new Growing(
			initialAge, initialBiomass, initialLength),
		new MassComponent(initialBiomass), new Memorizing(
			agentField.getWidth(),
			agentField.getHeight()), new Moving(position),
		new Reproducing(
			random.nextBoolean(FEMALE_PROBABILITY) ? Sex.FEMALE
				: Sex.MALE)));

	// attraction centers only if enabled
	if (definition.isAttractionEnabled()) {
	    components.add(new AttractionCenters(habitatField
		    .generateRandomPosition(random, FORAGING_HABITAT),
		    habitatField
			    .generateRandomPosition(random, RESTING_HABITAT)));
	}

	return components;
    }

    /**
     * Create new compartments component and add it to fish.
     * 
     * @param fish
     */
    private void addCompartmentsTo(Entity fish) {
	assert (fish.has(Arrays.<Class<? extends Component>> asList(
		Metabolizing.class, SpeciesDefinition.class, Aging.class,
		MassComponent.class, Growing.class, Reproducing.class)));

	Metabolizing metabolizing = fish.get(Metabolizing.class);
	SpeciesDefinition definition = fish.get(SpeciesDefinition.class);
	Aging aging = fish.get(Aging.class);
	MassComponent massComponent = fish.get(MassComponent.class);
	Growing growing = fish.get(Growing.class);
	Reproducing reproducing = fish.get(Reproducing.class);

	ShorttermStorage shortterm = new ShorttermStorage(metabolizing);

	// short-term is full at startup: calculate mass
	Amount<Mass> shorttermBiomass = Compartment.Type.SHORTTERM
		.getGramPerKj().times(shortterm.getAmount())
		.to(UnitConstants.BIOMASS);
	Amount<Mass> remainingBiomass = massComponent.getBiomass()
		.minus(shorttermBiomass);

	// remaining biomass is distributed in fat and protein storage
	Amount<Energy> initialFat = FormulaUtil.initialFat(remainingBiomass);
	Amount<Energy> initialProtein = FormulaUtil
		.initialProtein(remainingBiomass);

	Gut gut = new Gut(definition, metabolizing, aging);
	FatStorage fat = new FatStorage(initialFat, massComponent);
	ProteinStorage protein = new ProteinStorage(initialProtein, growing,
		reproducing);
	ReproductionStorage reproduction = new ReproductionStorage(
		massComponent);

	fish.add(new Compartments(gut, shortterm, fat, protein,
		reproduction));
    }

    public boolean addListener(EntityCreationListener listener) {
	return listeners.add(listener);
    }

    public boolean removeListener(EntityCreationListener listener) {
	return listeners.remove(listener);
    }

    public EntityManager getManager() {
	return manager;
    }

    public static interface EntityCreationListener {
	void onCreateFish(Entity fish);
    }
}

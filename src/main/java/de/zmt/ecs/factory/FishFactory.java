package de.zmt.ecs.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.AttractionCenters;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.MapToWorldConverter;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.pathfinding.PotentialMap;
import de.zmt.pathfinding.SimplePotentialMap;
import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.pathfinding.filter.Kernel;
import de.zmt.pathfinding.filter.NoTrapBlurKernel;
import de.zmt.storage.Compartment.Type;
import de.zmt.storage.ExcessStorage;
import de.zmt.storage.FatStorage;
import de.zmt.storage.Gut;
import de.zmt.storage.ProteinStorage;
import de.zmt.storage.ReproductionStorage;
import de.zmt.storage.ShorttermStorage;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.Fixed2D;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;;

/**
 * Factory for creating fish entities.
 * 
 * @author mey
 *
 */
class FishFactory implements EntityFactory<FishFactory.MyParam> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FishFactory.class.getName());

    /** Fish can spawn everywhere but not in {@link Habitat#MAINLAND}. */
    private static final Set<Habitat> SPAWN_HABITATS = EnumSet.complementOf(EnumSet.of(Habitat.MAINLAND));

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random, MyParam parameter) {
	Entity environment = parameter.environment;
	SpeciesDefinition definition = parameter.definition;

	SpeciesPathfindingMaps.Container speciesPathfindingMaps = environment.get(SpeciesPathfindingMaps.Container.class);
	if (speciesPathfindingMaps.get(definition) == null) {
	    speciesPathfindingMaps.put(definition, createSpeciesFlowMaps(environment, definition));
	}

	final AgentWorld agentWorld = environment.get(AgentWorld.class);
	Int2D randomHabitatPosition = environment.get(HabitatMap.class).generateRandomPosition(random, SPAWN_HABITATS);
	Double2D position = environment.get(EnvironmentDefinition.class).mapToWorld(randomHabitatPosition);

	final FishEntity fishEntity = new FishEntity(manager, definition.getName(),
		createComponents(random, position, parameter));
	agentWorld.addAgent(fishEntity);

	fishEntity.addStoppable(new FishStoppable(fishEntity, agentWorld));
	return fishEntity;
    }

    private static SpeciesPathfindingMaps createSpeciesFlowMaps(Entity environment, SpeciesDefinition definition) {
	HabitatMap habitatMap = environment.get(HabitatMap.class);

	DoubleGrid2D rawRiskGrid = createPredationRiskGrid(habitatMap, definition);
	DoubleGrid2D rawToForagingGrid = createHabitatAttractionGrid(
		definition.getPreferredHabitats(BehaviorMode.FORAGING), habitatMap);
	DoubleGrid2D rawToRestingGrid = createHabitatAttractionGrid(
		definition.getPreferredHabitats(BehaviorMode.RESTING), habitatMap);

	double riskScale = PotentialMap.MAX_REPULSIVE_VALUE
		/ definition.getMaxPredationRisk().doubleValue(UnitConstants.PER_STEP);

	Amount<Length> perceptionRadius = definition.getPerceptionRadius();
	PotentialMap riskPotentialMap = createFilteredPotentialMap(rawRiskGrid, riskScale, perceptionRadius);
	PotentialMap toForagingPotentialMap = createFilteredPotentialMap(rawToForagingGrid, 1, perceptionRadius);
	PotentialMap toRestingPotentialMap = createFilteredPotentialMap(rawToRestingGrid, 1, perceptionRadius);

	return new SpeciesPathfindingMaps(environment.get(GlobalPathfindingMaps.class), riskPotentialMap, toForagingPotentialMap,
		toRestingPotentialMap);
    }

    /**
     * Creates a grid containing predation risks.
     * 
     * @param habitatMap
     * @param definition
     * @return field of predation risks
     */
    private static DoubleGrid2D createPredationRiskGrid(HabitatMap habitatMap, SpeciesDefinition definition) {
	DoubleGrid2D riskGrid = new DoubleGrid2D(habitatMap.getWidth(), habitatMap.getHeight());

	for (int y = 0; y < riskGrid.getHeight(); y++) {
	    for (int x = 0; x < riskGrid.getWidth(); x++) {
		double riskPerStep = definition.getPredationRisk(habitatMap.obtainHabitat(x, y))
			.doubleValue(UnitConstants.PER_STEP);
		riskGrid.set(x, y, riskPerStep);
	    }
	}

	return riskGrid;
    }

    /**
     * Creates a grid with maximum attractive potential for given habitats.
     * 
     * @param attractingHabitats
     *            the habitats that will attract the agent
     * @param habitatMap
     * @return grid that attracts towards {@code attractingHabitats}
     */
    private static DoubleGrid2D createHabitatAttractionGrid(Set<Habitat> attractingHabitats, HabitatMap habitatMap) {
	DoubleGrid2D attractionGrid = new DoubleGrid2D(habitatMap.getWidth(), habitatMap.getHeight());

	for (int y = 0; y < attractionGrid.getHeight(); y++) {
	    for (int x = 0; x < attractionGrid.getWidth(); x++) {
		if (attractingHabitats.contains(habitatMap.obtainHabitat(x, y))) {
		    attractionGrid.set(x, y, PotentialMap.MAX_ATTRACTIVE_VALUE);
		}
	    }
	}

	return attractionGrid;
    }

    /**
     * Creates a {@link PotentialMap} from given grid filtered by a
     * {@link ConvolveOp} to values with a scale applied. The perception radius
     * is used to generate a blur over the grid.
     * 
     * @param grid
     *            the grid to filter
     * @param scale
     *            the scale to apply to the grid values
     * @param perceptionRadius
     *            the perceptionRadius to generate the blur on the grid
     * @return a {@link PotentialMap} from the filtered grid
     */
    private static PotentialMap createFilteredPotentialMap(DoubleGrid2D grid, double scale,
	    Amount<Length> perceptionRadius) {
	int extent = (int) perceptionRadius.longValue(UnitConstants.WORLD_DISTANCE);
	Kernel kernel = new NoTrapBlurKernel(extent, extent).multiply(scale);
	DoubleGrid2D filteredGrid = new ConvolveOp(kernel).filter(grid, null);
	return new SimplePotentialMap(filteredGrid);
    }

    private static Collection<Component> createComponents(MersenneTwisterFast random, Double2D position,
	    MyParam parameter) {
	SpeciesDefinition definition = parameter.definition;
	Entity environment = parameter.environment;
	Amount<Duration> initialAge = parameter.initialAge;

	HabitatMap habitatMap = environment.get(HabitatMap.class);
	AgentWorld agentWorld = environment.get(AgentWorld.class);
	MapToWorldConverter converter = environment.get(EnvironmentDefinition.class);
	Amount<Duration> maxAge = definition.determineMaxAge(random);

	// compute initial values
	Amount<Length> initialLength = FormulaUtil.expectedLength(definition.getAsymptoticLength(),
		definition.getGrowthCoeff(), initialAge, definition.getZeroSizeAge());
	Amount<Mass> initialBiomass = FormulaUtil.expectedMass(definition.getLengthMassCoeff(), initialLength,
		definition.getLengthMassExponent());
	Amount<Power> initialrestingMetabolicRate = FormulaUtil.restingMetabolicRate(initialBiomass);
	Sex sex = definition.determineSex(random);
	Int2D foragingCenter = habitatMap.generateRandomPosition(random,
		definition.getPreferredHabitats(BehaviorMode.FORAGING));
	Int2D restingCenter = habitatMap.generateRandomPosition(random,
		definition.getPreferredHabitats(BehaviorMode.RESTING));

	// create components
	Aging aging = new Aging(initialAge, maxAge);
	Metabolizing metabolizing = new Metabolizing(initialrestingMetabolicRate);
	Growing growing = new Growing(initialBiomass, initialLength);
	Memorizing memorizing = new Memorizing(agentWorld.getWidth(), agentWorld.getHeight());
	Moving moving = new Moving(position);
	LifeCycling lifeCycling = new LifeCycling(sex);
	AttractionCenters attractionCenters = new AttractionCenters(converter.mapToWorld(foragingCenter),
		converter.mapToWorld(restingCenter));
	Flowing flowing = new Flowing();

	// update phase to match current length
	while (lifeCycling.canChangePhase(definition.canChangeSex())
		&& initialLength.isGreaterThan(definition.getNextPhaseLength(lifeCycling.getPhase()))) {
	    lifeCycling.enterNextPhase();
	}

	Compartments compartments = createCompartments(metabolizing, growing, aging, definition,
		lifeCycling.isReproductive(), random);

	return Arrays.asList(definition, aging, metabolizing, growing, memorizing, moving, lifeCycling,
		attractionCenters, compartments, flowing);
    }

    /**
     * Creates {@link Compartments} component.
     * 
     * @param metabolizing
     * @param growing
     * @param aging
     * @param definition
     * @param reproductive
     * @param random
     * @return {@code Compartments} component
     */
    private static Compartments createCompartments(Metabolizing metabolizing, Growing growing, Aging aging,
	    SpeciesDefinition definition, boolean reproductive, MersenneTwisterFast random) {
	ShorttermStorage shortterm = new ShorttermStorage(metabolizing, random.nextDouble());

	// short-term is full at startup: calculate mass
	Amount<Mass> shorttermBiomass = Type.SHORTTERM.toMass(shortterm.getAmount());
	Amount<Mass> remainingBiomass = growing.getBiomass().minus(shorttermBiomass);

	// remaining biomass is distributed in fat and protein storage
	Amount<Energy> initialFat = FormulaUtil.initialFat(remainingBiomass);
	Amount<Energy> initialProtein = FormulaUtil.initialProtein(remainingBiomass);

	Gut gut = new Gut(definition, growing, aging);
	FatStorage fat = new FatStorage(initialFat, growing);
	ProteinStorage protein = new ProteinStorage(initialProtein, growing);
	ReproductionStorage reproduction;
	// if reproductive: random fill
	if (reproductive) {
	    reproduction = new ReproductionStorage(growing, random, random.nextDouble());
	} else {
	    reproduction = new ReproductionStorage(growing, random);
	}
	ExcessStorage excess = new ExcessStorage(metabolizing);

	return new Compartments(gut, shortterm, fat, protein, reproduction, excess);
    }

    /**
     * Stoppable set to every fish entity created.
     * 
     * @author mey
     *
     */
    private static class FishStoppable implements Stoppable {
	private static final long serialVersionUID = 1L;

	private final FishEntity fishEntity;
	private final AgentWorld agentWorld;

	private FishStoppable(FishEntity fishEntity, AgentWorld agentWorld) {
	    this.fishEntity = fishEntity;
	    this.agentWorld = agentWorld;
	}

	@Override
	public void stop() {
	    agentWorld.removeAgent(fishEntity);
	}
    }

    /**
     * Implements {@link Oriented2D} for display.
     * 
     * @author mey
     * 
     */
    private static class FishEntity extends Entity implements Fixed2D, Oriented2D {
	private static final long serialVersionUID = 1L;

	/** Component classes to be displayed when agent is inspected */
	private static final Collection<Class<? extends Component>> CLASSES_TO_INSPECT = Arrays
		.<Class<? extends Component>> asList(Moving.class, Flowing.class, Metabolizing.class, LifeCycling.class,
			Aging.class, Growing.class, Compartments.class);

	public FishEntity(EntityManager manager, String internalName, Collection<Component> components) {
	    super(manager, internalName, components);
	}

	@Override
	protected Collection<? extends Component> getComponentsToInspect() {
	    return get(CLASSES_TO_INSPECT);
	}

	@Override
	public double orientation2D() {
	    return get(Moving.class).getVelocity().angle();
	}

	@Override
	public boolean maySetLocation(Object field, Object newObjectLocation) {
	    get(Moving.class).setPosition((Double2D) newObjectLocation);

	    return true;
	}

    }

    /**
     * Parameter class for {@link FishFactory}.
     * 
     * @author mey
     *
     */
    public static class MyParam {
	private final SpeciesDefinition definition;
	private final Entity environment;
	private final Amount<Duration> initialAge;

	/**
	 * Constructs a {@link FishFactory} parameter object with specified
	 * initial age.
	 * 
	 * @param definition
	 *            species definition of the fish
	 * @param environment
	 *            entity representing the environment the fish is placed
	 *            into
	 * @param initialAge
	 *            the initial age of the created fish
	 */
	public MyParam(SpeciesDefinition definition, Entity environment, Amount<Duration> initialAge) {
	    super();
	    this.definition = definition;
	    this.environment = environment;
	    this.initialAge = initialAge;
	}

	/**
	 * Constructs a {@link FishFactory} parameter object at post settlement
	 * age.
	 * 
	 * @see SpeciesDefinition#getPostSettlementAge()
	 * @param definition
	 *            species definition of the fish
	 * @param environment
	 *            entity representing the environment the fish is placed
	 *            into
	 */
	public MyParam(SpeciesDefinition definition, Entity environment) {
	    this(definition, environment, definition.getPostSettlementAge());
	}
    }
}

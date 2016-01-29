package de.zmt.ecs.factory;

import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.environment.*;
import de.zmt.pathfinding.*;
import de.zmt.pathfinding.filter.*;
import de.zmt.storage.*;
import de.zmt.storage.Compartment.Type;
import de.zmt.util.*;
import ec.util.MersenneTwisterFast;
import sim.engine.Stoppable;
import sim.field.grid.DoubleGrid2D;
import sim.params.def.*;
import sim.params.def.SpeciesDefinition.SexChangeMode;
import sim.portrayal.*;
import sim.util.*;;

/**
 * Factory for creating fish entities.
 * 
 * @author mey
 *
 */
class FishFactory implements EntityFactory<FishFactory.MyParam> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FishFactory.class.getName());

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random, MyParam parameter) {
	Entity environment = parameter.environment;
	SpeciesDefinition definition = parameter.definition;

	SpeciesFlowMap.Container speciesFlowMaps = environment.get(SpeciesFlowMap.Container.class);
	if (speciesFlowMaps.get(definition) == null) {
	    speciesFlowMaps.put(definition, createSpeciesFlowMap(environment, definition));
	}

	final AgentWorld agentWorld = environment.get(AgentWorld.class);
	Int2D randomHabitatPosition = environment.get(HabitatMap.class).generateRandomPosition(random,
		definition.getSpawnHabitats());
	Double2D position = environment.get(EnvironmentDefinition.class).mapToWorld(randomHabitatPosition);

	final FishEntity fishEntity = new FishEntity(manager, definition.getName(),
		createComponents(random, position, parameter));
	agentWorld.addAgent(fishEntity);

	fishEntity.addStoppable(new Stoppable() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void stop() {
		agentWorld.removeAgent(fishEntity);
	    }
	});
	return fishEntity;
    }

    private static SpeciesFlowMap createSpeciesFlowMap(Entity environment, SpeciesDefinition definition) {
	SpeciesFlowMap speciesFlowMap = new SpeciesFlowMap(environment.get(GlobalFlowMap.class));
	DoubleGrid2D rawRiskField = createPredationRiskField(environment.get(HabitatMap.class), definition);
	DoubleGrid2D filteredRiskField = filterPredationRiskField(rawRiskField, definition.getMaxPredationRisk());
	speciesFlowMap.setRiskPotentialMap(new SimplePotentialMap(filteredRiskField));
	return speciesFlowMap;
    }

    /**
     * Creates a field containing predation risks.
     * 
     * @param habitatMap
     * @param definition
     * @return field of predation risks
     */
    private static DoubleGrid2D createPredationRiskField(HabitatMap habitatMap, SpeciesDefinition definition) {
	DoubleGrid2D riskField = new DoubleGrid2D(habitatMap.getWidth(), habitatMap.getHeight());

	for (int y = 0; y < riskField.getHeight(); y++) {
	    for (int x = 0; x < riskField.getWidth(); x++) {
		double riskPerStep = definition.getPredationRisk(habitatMap.obtainHabitat(x, y))
			.doubleValue(UnitConstants.PER_STEP);
		riskField.set(x, y, riskPerStep);
	    }
	}

	return riskField;
    }

    /**
     * Distributes a grid of predation risks to values between -1 and 0. Creates
     * a {@link ConvolveOp} filtering predation risk to values between -1 and 0.
     * 
     * @param riskField
     *            the raw risk field
     * @param maxPredationRisk
     * @return filtered risk field
     */
    private static DoubleGrid2D filterPredationRiskField(DoubleGrid2D riskField, Amount<Frequency> maxPredationRisk) {
	double scalar = PotentialMap.MAX_REPULSIVE_VALUE / maxPredationRisk.doubleValue(UnitConstants.PER_STEP);
	Kernel kernel = NoTrapBlurKernel.getInstance().multiply(scalar);
	return new ConvolveOp(kernel).filter(riskField, null);
    }

    private static Collection<Component> createComponents(MersenneTwisterFast random, Double2D position,
	    MyParam parameter) {
	SpeciesDefinition definition = parameter.definition;
	Entity environment = parameter.environment;
	Amount<Duration> initialAge = parameter.initialAge;

	HabitatMap habitatMap = environment.get(HabitatMap.class);
	AgentWorld agentWorld = environment.get(AgentWorld.class);
	MapToWorldConverter converter = environment.get(EnvironmentDefinition.class);

	// compute initial values
	Amount<Length> initialLength = FormulaUtil.expectedLength(definition.getAsymptoticLength(),
		definition.getGrowthCoeff(), initialAge, definition.getZeroSizeAge());
	Amount<Mass> initialBiomass = FormulaUtil.expectedMass(definition.getLengthMassCoeff(), initialLength,
		definition.getLengthMassDegree());
	Amount<Power> initialrestingMetabolicRate = FormulaUtil.restingMetabolicRate(initialBiomass);
	Sex sex = determineSex(random, definition);
	Int2D foragingCenter = habitatMap.generateRandomPosition(random, definition.getForagingHabitats());
	Int2D restingCenter = habitatMap.generateRandomPosition(random, definition.getRestingHabitats());

	// create components
	Aging aging = new Aging(initialAge);
	Metabolizing metabolizing = new Metabolizing(initialrestingMetabolicRate);
	Growing growing = new Growing(initialAge, initialBiomass, initialLength);
	Memorizing memorizing = new Memorizing(agentWorld.getWidth(), agentWorld.getHeight());
	Moving moving = new Moving(position);
	LifeCycling lifeCycling = new LifeCycling(sex);
	AttractionCenters attractionCenters = new AttractionCenters(converter.mapToWorld(foragingCenter),
		converter.mapToWorld(restingCenter));
	Compartments compartments = createCompartments(metabolizing, growing, aging, definition);
	Flowing flowing = new Flowing(environment.get(SpeciesFlowMap.Container.class).get(definition));

	return Arrays.asList(definition, aging, metabolizing, growing, memorizing, moving, lifeCycling,
		attractionCenters, compartments, flowing);
    }

    /**
     * Determine sex based on {@link SexChangeMode}.
     * 
     * @param random
     *            random number generator
     * @param definition
     *            the species definition
     * @return sex at birth
     */
    private static Sex determineSex(MersenneTwisterFast random, SpeciesDefinition definition) {
	SexChangeMode sexChangeMode = definition.getSexChangeMode();
	switch (sexChangeMode) {
	case GONOCHORISTIC:
	    return random.nextBoolean(definition.getFemaleProbability()) ? Sex.FEMALE : Sex.MALE;
	case PROTANDROUS:
	    return Sex.MALE;
	case PROTOGYNOUS:
	    return Sex.FEMALE;
	default:
	    throw new IllegalArgumentException("Sex at birth for " + sexChangeMode + " is undefined.");
	}
    }

    /**
     * Creates {@link Compartments} component.
     * 
     * @param metabolizing
     * @param growing
     * @param aging
     * @param definition
     * @return {@code Compartments} component
     */
    private static Compartments createCompartments(Metabolizing metabolizing, Growing growing, Aging aging,
	    SpeciesDefinition definition) {
	ShorttermStorage shortterm = new ShorttermStorage(metabolizing);

	// short-term is full at startup: calculate mass
	Amount<Mass> shorttermBiomass = Type.SHORTTERM.toMass(shortterm.getAmount());
	Amount<Mass> remainingBiomass = growing.getBiomass().minus(shorttermBiomass);

	// remaining biomass is distributed in fat and protein storage
	Amount<Energy> initialFat = FormulaUtil.initialFat(remainingBiomass);
	Amount<Energy> initialProtein = FormulaUtil.initialProtein(remainingBiomass);

	Gut gut = new Gut(definition, metabolizing, aging);
	FatStorage fat = new FatStorage(initialFat, growing);
	ProteinStorage protein = new ProteinStorage(initialProtein, growing);
	ReproductionStorage reproduction = new ReproductionStorage(growing);

	return new Compartments(gut, shortterm, fat, protein, reproduction);
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
		.<Class<? extends Component>> asList(Moving.class, Metabolizing.class, LifeCycling.class, Aging.class,
			Growing.class, Compartments.class);

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
	 * Constructs a {@link FishFactory} parameter object with initial age
	 * derived from definition.
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

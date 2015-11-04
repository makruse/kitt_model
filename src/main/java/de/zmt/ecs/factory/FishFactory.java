package de.zmt.ecs.factory;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.*;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.environment.*;
import de.zmt.storage.*;
import de.zmt.storage.Compartment.Type;
import de.zmt.util.FormulaUtil;
import ec.util.MersenneTwisterFast;
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
class FishFactory implements EntityFactory {
    private final SpeciesDefinition definition;
    private final Entity environment;
    private Amount<Duration> initialAge;

    /**
     * Constructs factory to create fish from {@code definition} into
     * {@code environment}.
     * 
     * @param definition
     *            species definition of the fish
     * @param environment
     *            entity representing the environment the fish is placed into
     */
    public FishFactory(SpeciesDefinition definition, Entity environment) {
	super();
	this.definition = definition;
	this.environment = environment;
	this.initialAge = SpeciesDefinition.getInitialAge();
    }

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random) {
	Int2D randomHabitatPosition = environment.get(HabitatMap.class).generateRandomPosition(random,
		definition.getSpawnHabitat());
	Double2D position = environment.get(EnvironmentDefinition.class).mapToWorld(randomHabitatPosition);
	return new FishEntity(manager, definition.getSpeciesName(), createComponents(random, position));
    }

    private Collection<Component> createComponents(MersenneTwisterFast random, Double2D position) {
	HabitatMap habitatMap = environment.get(HabitatMap.class);
	AgentWorld agentWorld = environment.get(AgentWorld.class);
	MapToWorldConverter converter = environment.get(EnvironmentDefinition.class);

	// compute initial values
	Amount<Length> initialLength = FormulaUtil.expectedLength(definition.getMaxLength(),
		definition.getGrowthCoeff(), initialAge, definition.getBirthLength());
	Amount<Mass> initialBiomass = FormulaUtil.expectedMass(definition.getLengthMassCoeff(), initialLength,
		definition.getLengthMassDegree());
	Amount<Power> initialStandardMetabolicRate = FormulaUtil.standardMetabolicRate(initialBiomass);
	Sex sex = determineSex(definition, random);
	Int2D foragingCenter = habitatMap.generateRandomPosition(random, definition.getForagingHabitat());
	Int2D restingCenter = habitatMap.generateRandomPosition(random, definition.getRestingHabitat());

	// create components
	Aging aging = new Aging(initialAge);
	Metabolizing metabolizing = new Metabolizing(initialStandardMetabolicRate);
	Growing growing = new Growing(initialAge, initialBiomass, initialLength);
	Memorizing memorizing = new Memorizing(agentWorld.getWidth(), agentWorld.getHeight());
	Moving moving = new Moving(position);
	LifeCycling lifeCycling = new LifeCycling(sex);
	Compartments compartments = createCompartments(metabolizing, growing, definition, aging);
	AttractionCenters attractionCenters = new AttractionCenters(converter.mapToWorld(foragingCenter),
		converter.mapToWorld(restingCenter));

	return Arrays.asList(definition, aging, metabolizing, growing, memorizing, moving, lifeCycling, compartments,
		attractionCenters);
    }

    /**
     * Determine sex based on {@link SexChangeMode}.
     * 
     * @param definition
     * @param random
     *            random number generator
     * @return sex at birth
     */
    private static Sex determineSex(SpeciesDefinition definition, MersenneTwisterFast random) {
	SexChangeMode sexChangeMode = definition.getSexChangeMode();
	switch (sexChangeMode) {
	case NONE:
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
     * Creates compartments component.
     * 
     * @param metabolizing
     * @param growing
     * @param definition
     * @param aging
     * @return compartments component
     */
    private static Compartments createCompartments(Metabolizing metabolizing, Growing growing,
	    SpeciesDefinition definition, Aging aging) {
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

    public void setInitialAge(Amount<Duration> intialAge) {
	this.initialAge = intialAge;
    }

    /**
     * Implements {@link Oriented2D} for display.
     * 
     * @author mey
     * 
     */
    private static class FishEntity extends Entity implements Fixed2D, Oriented2D {
	private static final long serialVersionUID = 1L;

	public FishEntity(EntityManager manager, String internalName, Collection<Component> components) {
	    super(manager, internalName, components);
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
}

package de.zmt.ecs.factory;

import java.util.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.*;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.environment.*;
import de.zmt.ecs.factory.FishComponentsFactory.Parameters;
import de.zmt.storage.*;
import de.zmt.storage.Compartment.Type;
import de.zmt.util.FormulaUtil;
import ec.util.MersenneTwisterFast;
import sim.params.def.SpeciesDefinition;
import sim.params.def.SpeciesDefinition.SexChangeMode;
import sim.util.*;;

/**
 * Creates components for fish entities.
 * 
 * @author mey
 *
 */
class FishComponentsFactory implements ComponentsFactory<Parameters> {
    private static final long serialVersionUID = 1L;

    private final MersenneTwisterFast random;

    public FishComponentsFactory(MersenneTwisterFast random) {
	super();
	this.random = random;
    }

    @Override
    public Collection<Component> createComponents(Parameters params) {
	// compute initial values
	Amount<Length> initialLength = FormulaUtil.expectedLength(params.definition.getMaxLength(),
		params.definition.getGrowthCoeff(), params.initialAge, params.definition.getBirthLength());
	Amount<Mass> initialBiomass = FormulaUtil.expectedMass(params.definition.getLengthMassCoeff(), initialLength,
		params.definition.getLengthMassDegree());
	Amount<Power> initialStandardMetabolicRate = FormulaUtil.standardMetabolicRate(initialBiomass);
	Sex sex = determineSex(params.definition.getSexChangeMode());
	Int2D foragingCenter = params.habitatMap.generateRandomPosition(random, SpeciesDefinition.getForagingHabitat());
	Int2D restingCenter = params.habitatMap.generateRandomPosition(random, SpeciesDefinition.getRestingHabitat());

	// create components
	Aging aging = new Aging(params.initialAge);
	Metabolizing metabolizing = new Metabolizing(initialStandardMetabolicRate);
	Growing growing = new Growing(params.initialAge, initialBiomass, initialLength);
	Memorizing memorizing = new Memorizing(params.agentWorld.getWidth(), params.agentWorld.getHeight());
	Moving moving = new Moving(params.position);
	LifeCycling lifeCycling = new LifeCycling(sex);
	Compartments compartments = createCompartments(metabolizing, growing, params.definition, aging);
	AttractionCenters attractionCenters = new AttractionCenters(params.converter.mapToWorld(foragingCenter),
		params.converter.mapToWorld(restingCenter));

	return Arrays.asList(params.definition, aging, metabolizing, growing, memorizing, moving, lifeCycling,
		compartments, attractionCenters);
    }

    /**
     * Determine sex based on {@link SexChangeMode}.
     * 
     * @param sexChangeMode
     * @return sex at birth
     */
    private Sex determineSex(SexChangeMode sexChangeMode) {
	switch (sexChangeMode) {
	case NONE:
	    return random.nextBoolean(SpeciesDefinition.getFemaleProbability()) ? Sex.FEMALE : Sex.MALE;
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

    static class Parameters implements ComponentsFactory.Parameters {
	public final SpeciesDefinition definition;
	public final Double2D position;
	public final Amount<Duration> initialAge;
	public final AgentWorld agentWorld;
	public final HabitatMap habitatMap;
	public final MapToWorldConverter converter;

	/**
	 * 
	 * @param definition
	 * @param position
	 * @param initialAge
	 * @param agentWorld
	 *            agent world from environment entity
	 * @param habitatMap
	 *            habitat map from environment entity
	 * @param converter
	 */
	public Parameters(SpeciesDefinition definition, Double2D position, Amount<Duration> initialAge,
		AgentWorld agentWorld, HabitatMap habitatMap, MapToWorldConverter converter) {
	    this.definition = definition;
	    this.position = position;
	    this.initialAge = initialAge;
	    this.agentWorld = agentWorld;
	    this.habitatMap = habitatMap;
	    this.converter = converter;
	}
    }
}

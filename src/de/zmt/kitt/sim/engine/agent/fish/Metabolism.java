package de.zmt.kitt.sim.engine.agent.fish;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Proxiable;
import de.zmt.kitt.sim.engine.agent.fish.Compartments.AbstractCompartmentStorage;
import de.zmt.kitt.sim.engine.agent.fish.Compartments.CompartmentPipeline;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.kitt.sim.portrayal.FishPortrayal.MetabolismPortrayable;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.SpecificEnergy;
import de.zmt.sim.portrayal.inspector.CombinedInspector;
import de.zmt.sim.portrayal.portrayable.ProvidesPortrayable;
import de.zmt.storage.ConfigurableStorage;
import de.zmt.storage.pipeline.AbstractStoragePipeline;

/**
 * Metabolism of a {@link Fish}.
 * 
 * @author cmeyer
 * 
 */
public class Metabolism implements Proxiable, ProvidesInspector,
	ProvidesPortrayable<MetabolismPortrayable>, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Metabolism.class
	    .getName());

    // STORAGE LOSS FACTORS
    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_FAT = 0.87;
    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_PROTEIN = 0.90;
    /** Loss factor for exchanging energy with the fat storage */
    private static final double LOSS_FACTOR_REPRO = 0.87;

    // CAPACITY VALUES used in limits
    private static final double GUT_MAX_CAPACITY_SMR_VALUE = 22;
    private static final double SHORTTERM_MAX_CAPACITY_SMR_VALUE = 9;
    private static final double FAT_MIN_CAPACITY_BIOMASS_VALUE = 0.05;
    private static final double FAT_MAX_CAPACITY_BIOMASS_VALUE = 0.1;
    private static final double PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE = 0.6;
    private static final double PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE = 1.2;
    private static final double REPRO_MAX_CAPACITY_BIOMASS_VALUE = 0.3;
    private static final double DESIRED_EXCESS_SMR_VALUE = 5;

    // CAPACITY LIMITS
    /**
     * Gut maximum storage capacity on SMR:<br>
     * {@value #GUT_MAX_CAPACITY_SMR_VALUE}h
     */
    private static final Amount<Duration> GUT_MAX_CAPACITY_SMR = Amount
	    .valueOf(GUT_MAX_CAPACITY_SMR_VALUE, HOUR);
    /**
     * Short-term maximum storage capacity on SMR:<br>
     * {@value #SHORTTERM_MAX_CAPACITY_SMR_VALUE}h
     */
    private static final Amount<Duration> SHORTTERM_MAX_CAPACITY_SMR = Amount
	    .valueOf(SHORTTERM_MAX_CAPACITY_SMR_VALUE, HOUR);
    /**
     * Fat minimum storage capacity on biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(fat) *
     * {@value #FAT_MIN_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MIN_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MIN_CAPACITY_BIOMASS_VALUE);
    /**
     * Fat maximum storage capacity on biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(fat) *
     * {@value #FAT_MAX_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> FAT_MAX_CAPACITY_BIOMASS = Compartment.Type.FAT
	    .getKjPerGram().times(FAT_MAX_CAPACITY_BIOMASS_VALUE);
    /**
     * Protein minimum storage capacity on expected biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(protein) *
     * {@value #PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE}
     * <p>
     * Exceeding this limit will result in starvation.
     */
    private static final Amount<SpecificEnergy> PROTEIN_MIN_CAPACITY_EXP_BIOMASS = Compartment.Type.PROTEIN
	    .getKjPerGram().times(PROTEIN_MIN_CAPACITY_EXP_BIOMASS_VALUE);
    /**
     * Protein maximum storage capacity on expected biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(protein) *
     * {@value #PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> PROTEIN_MAX_CAPACITY_EXP_BIOMASS = Compartment.Type.PROTEIN
	    .getKjPerGram().times(PROTEIN_MAX_CAPACITY_EXP_BIOMASS_VALUE);
    /**
     * Reproduction maximum storage capacity on biomass:<br>
     * {@link Compartment.Type#getEnergyDensity()}(reproduction) *
     * {@value #REPRO_MAX_CAPACITY_BIOMASS_VALUE}
     */
    private static final Amount<SpecificEnergy> REPRO_MAX_CAPACITY_BIOMASS = Compartment.Type.REPRODUCTION
	    .getKjPerGram().times(REPRO_MAX_CAPACITY_BIOMASS_VALUE);
    /**
     * Excess desired storage capacity on SMR:<br>
     * {@value #DESIRED_EXCESS_SMR_VALUE}h
     * <p>
     * Fish will be hungry until desired excess is achieved.
     */
    private static final Amount<Duration> DESIRED_EXCESS_SMR = Amount.valueOf(
	    DESIRED_EXCESS_SMR_VALUE, HOUR);

    /** {@link ActivityType}s during which the fish is feeding. */
    private static final Collection<ActivityType> ACTIVITIES_ALLOWING_FEEDING = Arrays
	    .asList(ActivityType.FORAGING);

    /** Age {@link Duration} of the fish. */
    private Amount<Duration> age;
    /**
     * Age reflecting fish growth. It will fall below {@link #age} if fish could
     * not consume enough food to grow ideally.
     * */
    private Amount<Duration> virtualAge;
    /** Biomass of fish (wet weight). */
    private Amount<Mass> biomass;

    /** Expected biomass of fish derived from its virtual age. */
    private Amount<Mass> expectedBiomass;

    /** Current standard metabolic rate */
    private Amount<Power> standardMetabolicRate;
    /** Fish life stage indicating its ability to reproduce. */
    private LifeStage lifeStage = LifeStage.JUVENILE;
    /** Energy storage compartments */
    private final Compartments compartments;

    /** Sex of the fish. Females can reproduce at adult age. */
    private final Sex sex;

    private final SpeciesDefinition speciesDefinition;
    /** For viewing properties. */
    private final MyPropertiesProxy proxy;

    /**
     * 
     * @param initialAge
     *            to start with.
     * @param female
     *            If true, fish is reproductive at adult age.
     * @param speciesDefinition
     *            of the fish.
     */
    public Metabolism(Sex sex, SpeciesDefinition speciesDefinition) {
	this.sex = sex;
	this.speciesDefinition = speciesDefinition;
	this.age = SpeciesDefinition.getInitialAge();
	this.virtualAge = age;

	Amount<Length> length = FormulaUtil.expectedLength(
		speciesDefinition.getMaxLength(),
		speciesDefinition.getGrowthCoeff(), age,
		speciesDefinition.getBirthLength());
	biomass = FormulaUtil.expectedMass(
		speciesDefinition.getLengthMassCoeff(), length,
		speciesDefinition.getLengthMassExponent());
	expectedBiomass = biomass;

	// TODO fill short-term first
	// total biomass is distributed in fat and protein storage
	Amount<Energy> initialFat = FormulaUtil.initialFat(biomass);
	Amount<Energy> initialProtein = FormulaUtil.initialProtein(biomass);

	compartments = new Compartments(new Gut(), new ShorttermStorage(),
		new FatStorage(initialFat), new ProteinStorage(initialProtein),
		new ReproductionStorage());

	standardMetabolicRate = FormulaUtil.standardMetabolicRate(biomass);
	proxy = new MyPropertiesProxy();
	proxy.length = length;
    }

    /**
     * Updates metabolism by adding food intake to gut, transferring digested
     * food energy to compartments and consuming needed energy for activity
     * type.
     * 
     * @param availableFood
     *            food in reach in dry weight over {@code delta}
     * @param activityCosts
     *            over {@code delta}
     * @param delta
     *            duration since last update
     * @return amount of food that was not processed
     * @throws MetabolismStoppedException
     *             if the fish died during update
     */
    public Amount<Mass> update(Amount<Mass> availableFood,
	    ActivityType activityType, Amount<Duration> delta)
	    throws MetabolismStoppedException {
	if (lifeStage == LifeStage.DEAD) {
	    logger.warning("Metabolism cannot be updated for dead fish");
	    return availableFood;
	}

	AgeResult ageResult = age(delta);
	Amount<Mass> rejectedFood = feed(availableFood, activityType);
	compartments.transferDigested(isReproductive());
	consume(activityType, delta);
	grow(delta, ageResult);

	return rejectedFood;
    }

    private boolean isReproductive() {
	return lifeStage == LifeStage.ADULT && sex == Sex.FEMALE;
    }

    /**
     * Increases age by delta and calculate expected length and biomass.
     * 
     * @param delta
     * @return expected length and the associated new virtual age
     * @throws MaximumAgeException
     *             if fish is beyond maximum age
     */
    private AgeResult age(Amount<Duration> delta) throws MaximumAgeException {
	age = age.plus(delta);
	// metabolism stops if beyond max age
	if (age.isGreaterThan(speciesDefinition.getMaxAge())) {
	    throw new MaximumAgeException();
	}

	Amount<Duration> virtualAgeForExpectedLength = virtualAge.plus(delta);

	Amount<Length> expectedLength = FormulaUtil
		.expectedLength(speciesDefinition.getMaxLength(),
			speciesDefinition.getGrowthCoeff(),
			virtualAgeForExpectedLength,
			speciesDefinition.getBirthLength());

	expectedBiomass = FormulaUtil.expectedMass(
		speciesDefinition.getLengthMassCoeff(), expectedLength,
		speciesDefinition.getLengthMassExponent());

	return new AgeResult(expectedLength, virtualAgeForExpectedLength);
    }

    private class AgeResult {
	public final Amount<Length> expectedLength;
	public final Amount<Duration> virtualAgeForExpectedLength;

	public AgeResult(Amount<Length> expectedLength,
		Amount<Duration> virtualAgeForExpectedLength) {
	    this.expectedLength = expectedLength;
	    this.virtualAgeForExpectedLength = virtualAgeForExpectedLength;
	}
    }

    /**
     * Offer available food for digestion. The remaining amount that was
     * rejected is returned. Food is rejected if the fish is not hungry or its
     * storage limitations exceeded.
     * 
     * @param availableFood
     *            on current patch in dry weight
     * @return rejected food
     */
    private Amount<Mass> feed(Amount<Mass> availableFood,
	    ActivityType activityType) {
	Amount<Mass> rejectedFood;
	Amount<Energy> ingestedEnergy;

	if (canFeed(availableFood, activityType)) {
	    Amount<Energy> energyToIngest = computeEnergyToIngest(availableFood);
	    // transfer energy to gut
	    Amount<Energy> rejectedEnergy = compartments.add(energyToIngest)
		    .getRejected();
	    // convert rejected energy back to mass
	    rejectedFood = rejectedEnergy.divide(
		    speciesDefinition.getEnergyContentFood()).to(
		    UnitConstants.FOOD);
	    ingestedEnergy = energyToIngest.minus(rejectedEnergy);
	}
	// fish cannot feed, nothing ingested
	else {
	    rejectedFood = availableFood;
	    ingestedEnergy = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
	}

	proxy.ingestedEnergy = ingestedEnergy;
	return rejectedFood;
    }

    /**
     * 
     * @param availableFood
     * @return true if hungry and {@code availableFood} is a valid and positive
     *         amount
     */
    private boolean canFeed(Amount<Mass> availableFood,
	    ActivityType activityType) {
	return (ACTIVITIES_ALLOWING_FEEDING.contains(activityType)
		&& isHungry() && availableFood != null && availableFood
		.getEstimatedValue() > 0);
    }

    /**
     * @see #DESIRED_EXCESS_SMR
     * @return True until desired excess amount is achieved
     */
    private boolean isHungry() {
	// return biomass.isLessThan(expectedBiomass);

	Amount<Energy> excessAmount = compartments
		.getStorageAmount(Compartment.Type.EXCESS);
	Amount<Energy> desiredExcessAmount = DESIRED_EXCESS_SMR.times(
		standardMetabolicRate).to(excessAmount.getUnit());

	return desiredExcessAmount.isGreaterThan(excessAmount);
    }

    private Amount<Energy> computeEnergyToIngest(Amount<Mass> availableFood) {
	// ingest desired amount and reject the rest
	// consumption rate depends on fish biomass
	Amount<Mass> foodConsumption = biomass.times(speciesDefinition
		.getMaxConsumptionPerStep());
	// fish cannot consume more than available...
	Amount<Mass> foodToIngest = AmountUtil.min(foodConsumption,
		availableFood);
	return foodToIngest.times(speciesDefinition.getEnergyContentFood()).to(
		UnitConstants.CELLULAR_ENERGY);
    }

    /**
     * Consumes needed energy from compartments.
     * 
     * @param activityType
     * @param delta
     * @throws StarvedToDeathException
     *             if energy is insufficient
     */
    private void consume(ActivityType activityType, Amount<Duration> delta)
	    throws StarvationException {
	Amount<Energy> consumedEnergy = standardMetabolicRate.times(delta)
		.times(activityType.getCostFactor())
		.to(UnitConstants.CELLULAR_ENERGY);

	// subtract needed energy from compartments
	Amount<Energy> energyNotProvided = compartments.add(
		consumedEnergy.opposite()).getRejected();

	// if the needed energy is not available the fish has starved to death
	if (energyNotProvided.getEstimatedValue() < 0) {
	    throw new StarvationException();
	}

	proxy.consumedEnergy = consumedEnergy;
    }

    /**
     * Updates biomass from compartments. Fish will grow in size and
     * {@link #virtualAge} be increased if enough biomass could be accumulated.
     * 
     * @param delta
     * @throws MaximumAgeException
     *             if fish is beyond maximum age
     */
    private void grow(Amount<Duration> delta, AgeResult ageResult) {
	biomass = compartments.computeBiomass();
	standardMetabolicRate = FormulaUtil.standardMetabolicRate(biomass);

	// fish had enough energy to grow, update length and virtual age
	if (biomass.isGreaterThan(expectedBiomass)) {
	    virtualAge = ageResult.virtualAgeForExpectedLength;

	    // fish turns adult if it reaches a certain length
	    if (lifeStage == LifeStage.JUVENILE
		    && ageResult.expectedLength.isGreaterThan(speciesDefinition
			    .getAdultLength())) {
		lifeStage = LifeStage.ADULT;
	    }

	    proxy.length = ageResult.expectedLength;
	}
    }

    /** Stops metabolism, i.e. the fish dies */
    public void stop() {
	lifeStage = LifeStage.DEAD;
    }

    /** @return True if the fish is ready for reproduction */
    public boolean canReproduce() {
	return ((ReproductionStorage) compartments
		.getStorage(Compartment.Type.REPRODUCTION)).atUpperLimit();
    }

    /**
     * Clears reproduction storage, i.e. the fish lays its eggs.
     * 
     * @return Energy amount cleared from storage
     */
    public Amount<Energy> clearReproductionStorage() {
	return ((ReproductionStorage) compartments
		.getStorage(Compartment.Type.REPRODUCTION)).clear();
    }

    // TODO improve encapsulation - only needed for output
    public Amount<Duration> getAge() {
	return age;
    }

    // TODO improve encapsulation - only needed for output
    public Amount<Mass> getBiomass() {
	return biomass;
    }

    // TODO improve encapsulation - only needed for output
    public LifeStage getLifeStage() {
	return lifeStage;
    }

    // TODO improve encapsulation - only needed for output
    public Sex getSex() {
	return sex;
    }

    @Override
    public String toString() {
	return proxy.toString();
    }

    @Override
    public Object propertiesProxy() {
	return proxy;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(compartments, state, compartments
			.getClass().getSimpleName()));
    }

    @Override
    public MetabolismPortrayable providePortrayable() {
	return new MyPortrayable();
    }

    public class MyPropertiesProxy implements Serializable {
	private static final long serialVersionUID = 1L;

	private Amount<Length> length = AmountUtil
		.zero(UnitConstants.BODY_LENGTH);
	private Amount<Energy> ingestedEnergy = AmountUtil
		.zero(UnitConstants.CELLULAR_ENERGY);
	private Amount<Energy> consumedEnergy = AmountUtil
		.zero(UnitConstants.CELLULAR_ENERGY);

	public Sex getSex() {
	    return sex;
	}

	public LifeStage getLifeStage() {
	    return lifeStage;
	}

	public double getAge_day() {
	    return age.to(DAY).getEstimatedValue();
	}

	public double getVirtualAge_day() {
	    return virtualAge.to(DAY).getEstimatedValue();
	}

	public double getLength_cm() {
	    return length.to(CENTIMETER).getEstimatedValue();
	}

	public boolean isHungry() {
	    return Metabolism.this.isHungry();
	}

	public double getBiomass_g() {
	    return biomass.doubleValue(GRAM);
	}

	public double getExpectedBiomass_g() {
	    return expectedBiomass.doubleValue(GRAM);
	}

	public double getStandardMetabolicRate() {
	    return standardMetabolicRate.getEstimatedValue();
	}

	public String nameStandardMetabolicRate() {
	    return "standardMetabolicRate_" + standardMetabolicRate.getUnit();
	}

	public double getIngestedEnergy_kJ() {
	    return ingestedEnergy.doubleValue(KILO(JOULE));
	}

	public double getConsumedEnergy_kJ() {
	    return consumedEnergy.doubleValue(KILO(JOULE));
	}

	@Override
	public String toString() {
	    return Metabolism.class.getSimpleName() + " [ingested="
		    + ingestedEnergy + ", needed=" + consumedEnergy + "]";
	}
    }

    public class MyPortrayable implements MetabolismPortrayable {

	@Override
	public Amount<Mass> getBiomass() {
	    return biomass;
	}

    }

    private class Gut extends AbstractStoragePipeline<Energy> implements
	    CompartmentPipeline {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param lossFactorDigestion
	 *            factor of energy that remains after digestion
	 * @param transitTime
	 *            {@link Duration} the food needs to be digested
	 */
	public Gut() {
	    super(
		    new ConfigurableStorage<Energy>(
			    UnitConstants.CELLULAR_ENERGY) {
			private static final long serialVersionUID = 1L;

			@Override
			protected Amount<Energy> getUpperLimit() {
			    // maximum capacity of gut
			    return standardMetabolicRate.times(
				    GUT_MAX_CAPACITY_SMR).to(amount.getUnit());
			}

			@Override
			protected double getFactorIn() {
			    // energy is lost while digesting
			    return speciesDefinition.getLossFactorDigestion();
			}
		    });
	}

	@Override
	protected DelayedStorage<Energy> createDelayedStorage(
		Amount<Energy> storedAmount) {
	    return new Digesta(storedAmount);
	}

	@Override
	public Type getType() {
	    return Type.GUT;
	}

	/**
	 * Food undergoing digestion.
	 * 
	 * @author cmeyer
	 * 
	 */
	private class Digesta extends DelayedStorage<Energy> {
	    private static final long serialVersionUID = 1L;

	    /** Age of fish when digestion of this digesta is finished. */
	    private final Amount<Duration> digestionFinishedAge;

	    /**
	     * Create new digesta with given amount of energy.
	     * 
	     * @param energy
	     *            in kJ
	     */
	    public Digesta(Amount<Energy> energy) {
		super(energy);
		this.digestionFinishedAge = age.plus(speciesDefinition
			.getGutTransitDuration());
	    }

	    @Override
	    public long getDelay(TimeUnit unit) {
		Amount<Duration> delay = digestionFinishedAge.minus(age);
		return AmountUtil.toTimeUnit(delay, unit);
	    }

	    @Override
	    public int compareTo(Delayed o) {
		// shortcut for better performance
		/*
		 * Apart from that, deserialization will fail without it because
		 * sorting of the priority queue calls getDelay (from compareTo)
		 * for its Digestables, which would need the age field in the
		 * encapsulating object of Metabolism. Metabolism has not
		 * finished deserialization at this point and the age field is
		 * not available, leading to a NullPointerException.
		 */
		if (o instanceof Digesta) {
		    return digestionFinishedAge
			    .compareTo(((Digesta) o).digestionFinishedAge);
		}
		return super.compareTo(o);
	    }

	}
    }

    private class ShorttermStorage extends AbstractCompartmentStorage {
	private static final long serialVersionUID = 1L;

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return standardMetabolicRate.times(SHORTTERM_MAX_CAPACITY_SMR).to(
		    amount.getUnit());
	}

	@Override
	public Type getType() {
	    return Type.SHORTTERM;
	}

    }

    private class FatStorage extends AbstractCompartmentStorage {
	private static final long serialVersionUID = 1L;

	public FatStorage(Amount<Energy> amount) {
	    super(amount);
	}

	@Override
	protected Amount<Energy> getLowerLimit() {
	    return biomass.times(FAT_MIN_CAPACITY_BIOMASS).to(
		    UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return biomass.times(FAT_MAX_CAPACITY_BIOMASS).to(
		    UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_FAT;
	}

	@Override
	protected double getFactorOut() {
	    return 1 / getFactorIn();
	}

	@Override
	public Type getType() {
	    return Type.FAT;
	}

    }

    private class ProteinStorage extends AbstractCompartmentStorage {
	private static final long serialVersionUID = 1L;

	public ProteinStorage(Amount<Energy> amount) {
	    super(amount);
	}

	@Override
	protected Amount<Energy> getLowerLimit() {
	    // amount is factor of protein amount in total expected biomass
	    return PROTEIN_MIN_CAPACITY_EXP_BIOMASS.times(expectedBiomass)
		    .times(Type.PROTEIN.getGrowthFraction(isReproductive()))
		    .to(UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return PROTEIN_MAX_CAPACITY_EXP_BIOMASS.times(expectedBiomass)
		    .times(Type.PROTEIN.getGrowthFraction(isReproductive()))
		    .to(UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_PROTEIN;
	}

	@Override
	protected double getFactorOut() {
	    return 1 / getFactorIn();
	}

	@Override
	public Type getType() {
	    return Type.PROTEIN;
	}

    }

    private class ReproductionStorage extends AbstractCompartmentStorage {
	private static final long serialVersionUID = 1L;

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return biomass.times(REPRO_MAX_CAPACITY_BIOMASS).to(
		    UnitConstants.CELLULAR_ENERGY);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_REPRO;
	}

	@Override
	public Type getType() {
	    return Type.REPRODUCTION;
	}

    }

    public static enum LifeStage {
	JUVENILE, ADULT, DEAD
    }

    public static enum Sex {
	FEMALE, MALE
    }

    /**
     * Parent class for all exceptions thrown during
     * {@link Metabolism#update(Amount, ActivityType, Amount)} indicating that
     * the metabolism stopped, i.e. the fish died.
     * 
     * @author cmeyer
     * 
     */
    public static class MetabolismStoppedException extends Exception {
	private static final long serialVersionUID = 1L;
    }

    public static class MaximumAgeException extends MetabolismStoppedException {
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
	    return " is too old to live any longer.";
	}
    }

    public static class StarvationException extends MetabolismStoppedException {
	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
	    return " starved to death.";
	}
    }

}

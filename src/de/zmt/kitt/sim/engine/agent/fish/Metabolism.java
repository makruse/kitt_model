package de.zmt.kitt.sim.engine.agent.fish;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Proxiable;
import de.zmt.kitt.sim.engine.agent.fish.Compartments.CompartmentType;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.EnergyDensity;
import de.zmt.sim.portrayal.inspector.CombinedInspector;
import de.zmt.storage.*;

/**
 * Metabolism of a {@link Fish}.
 * 
 * @author cmeyer
 * 
 */
public class Metabolism implements Proxiable, ProvidesInspector {
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

    // STORAGE CAPACITY LIMITS
    private static final double GUT_MAX_CAPACITY_VALUE = 7;
    /**
     * factor for gut capacity: {@value #GUT_MAX_CAPACITY_VALUE} *
     * {@link #standardMetabolicRate}
     */
    private static final Amount<Duration> GUT_MAX_CAPACITY_SMR = Amount
	    .valueOf(GUT_MAX_CAPACITY_VALUE, HOUR);

    private static final double SHORTTERM_MAX_CAPACITY_VALUE = 5;
    /**
     * Factor for maximum short-term storage capacity:<br>
     * {@value #SHORTTERM_MAX_CAPACITY_VALUE} * {@link #standardMetabolicRate}
     */
    private static final Amount<Duration> SHORTTERM_MAX_CAPACITY_SMR = Amount
	    .valueOf(SHORTTERM_MAX_CAPACITY_VALUE, HOUR);

    private static final double FAT_MIN_CAPACITY_VALUE = 0.05;
    /**
     * Factor for minimum fat storage capacity:<br>
     * Capacity in kJ = {@value #FAT_MIN_CAPACITY_VALUE} * {@link #biomass}
     */
    private static final Amount<EnergyDensity> FAT_MIN_CAPACITY_BIOMASS = Amount
	    .valueOf(FAT_MIN_CAPACITY_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);

    private static final double FAT_MAX_CAPACITY_VALUE = 0.1;
    /**
     * Factor for maximum fat storage capacity:<br>
     * Capacity in kJ = {@value #FAT_MAX_CAPACITY_VALUE} * {@link #biomass}
     */
    private static final Amount<EnergyDensity> FAT_MAX_CAPACITY_BIOMASS = Amount
	    .valueOf(FAT_MAX_CAPACITY_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);

    private static final double PROTEIN_MIN_CAPACITY_VALUE = 0.6;
    /**
     * Factor for minimum protein storage capacity:<br>
     * Capacity in kJ = {@value #PROTEIN_MIN_CAPACITY_VALUE} * expected protein
     * growth
     */
    private static final Amount<EnergyDensity> PROTEIN_MIN_CAPACITY_EXP_GRWTH = Amount
	    .valueOf(PROTEIN_MIN_CAPACITY_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);

    private static final double REPRO_MAX_CAPACITY_VALUE = 0.3;
    /**
     * Factor for minimum reproduction storage capacity:<br>
     * Capacity in kJ = {@value #REPRO_MAX_CAPACITY_VALUE} * {@link #biomass}
     */
    private static final Amount<EnergyDensity> REPRO_MAX_CAPACITY_BIOMASS = Amount
	    .valueOf(REPRO_MAX_CAPACITY_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);

    // DESIRED EXCESS
    private static final double DESIRED_EXCESS_VALUE = 5;
    /**
     * Factor for desired excess energy:<br>
     * {@value #DESIRED_EXCESS_VALUE} * {@link #standardMetabolicRate}
     * <p>
     * Fish will be hungry until desired excess is achieved.
     */
    private static final Amount<Duration> DESIRED_EXCESS_SMR = Amount.valueOf(
	    DESIRED_EXCESS_VALUE, HOUR);

    // GROWTH FRACTIONS
    // TODO growth fraction values differ from document. verify.
    /** Fraction of protein growth from total. */
    private static final double GROWTH_FRACTION_PROTEIN = 0.95;
    /** Fraction of fat growth from total for non-reproductive fish. */
    private static final double GROWTH_FRACTION_FAT_NONREPRO = 1 - GROWTH_FRACTION_PROTEIN;
    /** Fraction of reproduction energy growth from total for reproductive fish. */
    private static final double GROWTH_FRACTION_REPRO = 0.015;
    /** Fraction of fat growth from total for reproductive fish. */
    private static final double GROWTH_FRACTION_FAT_REPRO = 1
	    - GROWTH_FRACTION_PROTEIN - GROWTH_FRACTION_REPRO;

    /** Energy storage compartments */
    private final Compartments compartments;
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
    /** Fish is able to reproduce at adult age */
    private final boolean female;

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
    public Metabolism(boolean female, SpeciesDefinition speciesDefinition) {
	this.female = female;
	this.speciesDefinition = speciesDefinition;
	this.age = SpeciesDefinition.getInitialAge();
	this.virtualAge = age;

	Amount<Length> length = FormulaUtil.expectedLength(
		speciesDefinition.getGrowthLength(),
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
	FeedResult feedResult = feed(availableFood);
	transfer();
	Amount<Energy> consumedEnergy = consume(activityType, delta);
	Amount<Length> length = grow(delta, ageResult);
	updateProxy(length, feedResult.ingestedEnergy, consumedEnergy);

	return feedResult.rejectedFood;
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
		.expectedLength(speciesDefinition.getGrowthLength(),
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
     * @return {@link FeedResult} object with rejected food and ingested energy
     */
    private FeedResult feed(Amount<Mass> availableFood) {
	Amount<Mass> rejectedFood;
	Amount<Energy> ingestedEnergy;

	if (canFeed(availableFood)) {
	    Amount<Energy> energyToIngest = computeEnergyToIngest(availableFood);
	    // transfer energy to gut
	    Amount<Energy> rejectedEnergy = compartments.add(energyToIngest)
		    .getRejected();
	    // convert rejected energy back to mass
	    rejectedFood = rejectedEnergy.divide(
		    speciesDefinition.getEnergyDensityFood()).to(
		    AmountUtil.MASS_UNIT);
	    ingestedEnergy = energyToIngest.minus(rejectedEnergy);
	}
	// fish cannot feed, nothing ingested
	else {
	    rejectedFood = availableFood;
	    ingestedEnergy = AmountUtil.zero(AmountUtil.ENERGY_UNIT);
	}

	return new FeedResult(rejectedFood, ingestedEnergy);
    }

    private class FeedResult {
	public final Amount<Mass> rejectedFood;
	public final Amount<Energy> ingestedEnergy;

	public FeedResult(Amount<Mass> rejectedFood,
		Amount<Energy> ingestedEnergy) {
	    this.rejectedFood = rejectedFood;
	    this.ingestedEnergy = ingestedEnergy;
	}
    }

    /**
     * 
     * @param availableFood
     * @return true if hungry and {@code availableFood} is a valid and positive
     *         amount
     */
    private boolean canFeed(Amount<Mass> availableFood) {
	return (availableFood != null && availableFood.getEstimatedValue() > 0 && isHungry());
    }

    private Amount<Energy> computeEnergyToIngest(Amount<Mass> availableFood) {
	// ingest desired amount and reject the rest
	// consumption rate depends on fish biomass
	Amount<Mass> foodConsumption = biomass.times(speciesDefinition
		.getMaxConsumptionPerStep());
	// fish cannot consume more than available...
	Amount<Mass> foodToIngest = AmountUtil.min(foodConsumption,
		availableFood);
	return foodToIngest.times(speciesDefinition.getEnergyDensityFood()).to(
		AmountUtil.ENERGY_UNIT);
    }

    /** Transfers digested energy from gut to compartments. */
    private void transfer() {
	// only reproductive fish produce reproduction energy
	if (lifeStage == LifeStage.ADULT && female) {
	    compartments.transferDigested(GROWTH_FRACTION_FAT_REPRO,
		    GROWTH_FRACTION_PROTEIN, GROWTH_FRACTION_REPRO);
	} else {
	    compartments.transferDigested(GROWTH_FRACTION_FAT_NONREPRO,
		    GROWTH_FRACTION_PROTEIN, 0);
	}
    }

    /**
     * Consumes needed energy from compartments.
     * 
     * @param activityType
     * @param delta
     * @throws StarvedToDeathException
     *             if energy is insufficient
     * @return consumed energy
     */
    private Amount<Energy> consume(ActivityType activityType,
	    Amount<Duration> delta) throws StarvationException {
	Amount<Energy> energyConsumed = standardMetabolicRate.times(delta)
		.times(activityType.getCostFactor()).to(AmountUtil.ENERGY_UNIT);

	// subtract needed energy from compartments
	Amount<Energy> energyNotProvided = compartments.add(
		energyConsumed.opposite()).getRejected();

	// if the needed energy is not available the fish has starved to death
	if (energyNotProvided.getEstimatedValue() < 0) {
	    throw new StarvationException();
	}

	return energyConsumed;
    }

    /**
     * Updates biomass from compartments. Fish will grow in size and
     * {@link #virtualAge} be increased if enough biomass could be accumulated.
     * 
     * @param delta
     * @throws MaximumAgeException
     *             if fish is beyond maximum age
     * @return length
     */
    private Amount<Length> grow(Amount<Duration> delta, AgeResult ageResult) {
	biomass = FormulaUtil.biomassFromCompartments(compartments);
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

	    return ageResult.expectedLength;
	}

	// fish did not grow, return old length
	return proxy.length;
    }

    private void updateProxy(Amount<Length> length,
	    Amount<Energy> ingestedEnergy, Amount<Energy> consumedEnergy) {
	proxy.length = length;
	proxy.ingestedEnergy = ingestedEnergy;
	proxy.consumedEnergy = consumedEnergy;
    }

    /**
     * @see #DESIRED_EXCESS_SMR
     * @return True until desired excess amount is achieved
     */
    private boolean isHungry() {
	// return biomass.isLessThan(expectedBiomass);

	Amount<Energy> excessAmount = compartments
		.getAmount(CompartmentType.EXCESS);
	Amount<Energy> desiredExcessAmount = DESIRED_EXCESS_SMR.times(
		standardMetabolicRate).to(excessAmount.getUnit());

	return desiredExcessAmount.isGreaterThan(excessAmount);
    }

    /** Stops metabolism, i.e. the fish dies */
    public void stop() {
	lifeStage = LifeStage.DEAD;
    }

    /** @return True if the fish is ready for reproduction */
    public boolean canReproduce() {
	return ((ReproductionStorage) compartments
		.getCompartment(CompartmentType.REPRODUCTION)).atUpperLimit();
    }

    /**
     * Clears reproduction storage, i.e. the fish lays its eggs.
     * 
     * @return Energy amount cleared from storage
     */
    public Amount<Energy> clearReproductionStorage() {
	return ((ReproductionStorage) compartments
		.getCompartment(CompartmentType.REPRODUCTION)).clear();
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

    public class MyPropertiesProxy {
	private Amount<Length> length;
	private Amount<Energy> ingestedEnergy;
	private Amount<Energy> consumedEnergy;

	public boolean isFemale() {
	    return female;
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

	public double getEnergyIngested_kJ() {
	    return ingestedEnergy.doubleValue(KILO(JOULE));
	}

	public double getEnergyConsumed_kJ() {
	    return consumedEnergy.doubleValue(KILO(JOULE));
	}

	@Override
	public String toString() {
	    return Metabolism.class.getSimpleName() + " [ingested="
		    + ingestedEnergy + ", needed=" + consumedEnergy + "]";
	}
    }

    private class Gut extends StoragePipeline<Energy> {
	/**
	 * 
	 * @param lossFactorDigestion
	 *            factor of energy that remains after digestion
	 * @param transitTime
	 *            {@link Duration} the food needs to be digested
	 */
	public Gut() {
	    super(new LimitedStorage<Energy>(AmountUtil.ENERGY_UNIT) {

		@Override
		protected Amount<Energy> getUpperLimit() {
		    // maximum capacity of gut
		    return standardMetabolicRate.times(GUT_MAX_CAPACITY_SMR)
			    .to(amount.getUnit());
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

	/**
	 * Food undergoing digestion.
	 * 
	 * @author cmeyer
	 * 
	 */
	private class Digesta extends DelayedStorage<Energy> {
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
	}
    }

    private class ShorttermStorage extends EnergyStorage {

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return standardMetabolicRate.times(SHORTTERM_MAX_CAPACITY_SMR).to(
		    amount.getUnit());
	}

    }

    private class FatStorage extends EnergyStorage {
	public FatStorage(Amount<Energy> amount) {
	    super(amount);
	}

	@Override
	protected Amount<Energy> getLowerLimit() {
	    return biomass.times(FAT_MIN_CAPACITY_BIOMASS).to(
		    AmountUtil.ENERGY_UNIT);
	}

	@Override
	protected Amount<Energy> getUpperLimit() {
	    return biomass.times(FAT_MAX_CAPACITY_BIOMASS).to(
		    AmountUtil.ENERGY_UNIT);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_FAT;
	}

	@Override
	protected double getFactorOut() {
	    return 1 / getFactorIn();
	}

    }

    private class ProteinStorage extends EnergyStorage {
	public ProteinStorage(Amount<Energy> amount) {
	    super(amount);
	}

	@Override
	protected Amount<Energy> getLowerLimit() {
	    // amount is factor of protein amount in total expected biomass
	    return PROTEIN_MIN_CAPACITY_EXP_GRWTH.times(expectedBiomass)
		    .times(GROWTH_FRACTION_PROTEIN).to(AmountUtil.ENERGY_UNIT);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_PROTEIN;
	}

	@Override
	protected double getFactorOut() {
	    return 1 / getFactorIn();
	}

    }

    private class ReproductionStorage extends EnergyStorage {
	@Override
	protected Amount<Energy> getUpperLimit() {
	    return biomass.times(REPRO_MAX_CAPACITY_BIOMASS).to(
		    AmountUtil.ENERGY_UNIT);
	}

	@Override
	protected double getFactorIn() {
	    return LOSS_FACTOR_REPRO;
	}

    }

    /**
     * Storage for energy using {@link Unit} defined in
     * {@link EnvironmentDefinition#ENERGY_UNIT}.
     * 
     * @author cmeyer
     * 
     */
    private class EnergyStorage extends LimitedStorage<Energy> {
	public EnergyStorage(Amount<Energy> amount) {
	    this();
	    this.amount = amount;
	}

	/**
	 * Create a new empty {@link EnergyStorage}.
	 */
	public EnergyStorage() {
	    super(AmountUtil.ENERGY_UNIT);
	}
    }

    public static enum LifeStage {
	JUVENILE, ADULT, DEAD
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

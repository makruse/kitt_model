package de.zmt.kitt.sim.engine.agent.fish;

import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.portrayal.*;
import sim.portrayal.inspector.*;
import sim.util.Proxiable;
import de.zmt.kitt.sim.engine.agent.fish.Compartments.CompartmentType;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.kitt.util.*;
import de.zmt.kitt.util.quantity.EnergyDensity;
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

    /** Length of fish. */
    private Amount<Length> length;
    /** Current standard metabolic rate */
    private Amount<Power> standardMetabolicRate;
    /** Energy currently ingested */
    private Amount<Energy> energyIngested;

    private Amount<Energy> energyConsumed;

    /** Fish life stage indicating its ability to reproduce. */
    private LifeStage lifeStage = LifeStage.JUVENILE;
    /** Fish is able to reproduce at adult age */
    private final boolean female;

    private final SpeciesDefinition speciesDefinition;

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

	// initial length and biomass from expected values
	this.length = speciesDefinition.getInitialLength();
	this.biomass = speciesDefinition.getInitialBiomass();
	this.expectedBiomass = biomass;

	// TODO fill short-term first
	// total biomass is distributed in fat and protein storage
	Amount<Energy> initialFat = FormulaUtil.initialFat(biomass);
	Amount<Energy> initialProtein = FormulaUtil.initialProtein(biomass);

	compartments = new Compartments(new Gut(), new ShorttermStorage(),
		new FatStorage(initialFat), new ProteinStorage(initialProtein),
		new ReproductionStorage());

	standardMetabolicRate = FormulaUtil.standardMetabolicRate(biomass);
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
     */
    public Amount<Mass> update(Amount<Mass> availableFood,
	    ActivityType activityType, Amount<Duration> delta)
	    throws StarvedToDeathException {
	if (lifeStage == LifeStage.DEAD) {
	    logger.warning("Metabolism cannot be updated for dead fish");
	    return availableFood;
	}

	standardMetabolicRate = FormulaUtil.standardMetabolicRate(biomass);
	Amount<Mass> rejectedFood = feed(availableFood);
	transfer();
	consume(activityType, delta);
	grow(delta);

	return rejectedFood;
    }

    /**
     * Offer available food for digestion. The remaining amount that was
     * rejected is returned. Food is rejected if the fish is not hungry or its
     * storage limitations exceeded.
     * 
     * @param availableFood
     *            on current patch in dry weight
     * @return amount of available food that was rejected
     */
    private Amount<Mass> feed(Amount<Mass> availableFood) {
	energyIngested = AmountUtil.zero(AmountUtil.ENERGY_UNIT);

	// nothing available: nothing to reject
	if (availableFood == null || availableFood.getEstimatedValue() <= 0) {
	    return AmountUtil.zero(AmountUtil.MASS_UNIT);
	}
	// not hungry: reject everything
	else if (!isHungry()) {
	    return availableFood;
	}

	// ingest desired amount and reject the rest
	// consumption rate depends on fish biomass
	Amount<Mass> foodConsumption = biomass.times(speciesDefinition
		.getMaxConsumptionPerStep());
	// fish cannot consume more than available...
	Amount<Mass> foodToIngest = AmountUtil.min(foodConsumption,
		availableFood);

	Amount<Energy> energyContent = foodToIngest.times(
		speciesDefinition.getEnergyDensityFood()).to(
		AmountUtil.ENERGY_UNIT);
	// transfer energy to gut
	Amount<Energy> rejectedEnergy = compartments.add(energyContent)
		.getRejected();
	energyIngested = energyContent.minus(rejectedEnergy);
	// convert rejected energy back to mass
	Amount<Mass> rejectedFood = rejectedEnergy.divide(
		speciesDefinition.getEnergyDensityFood()).to(
		AmountUtil.MASS_UNIT);

	return rejectedFood;
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
     */
    private void consume(ActivityType activityType, Amount<Duration> delta)
	    throws StarvedToDeathException {
	energyConsumed = standardMetabolicRate.times(delta)
		.times(activityType.getCostFactor()).to(AmountUtil.ENERGY_UNIT);

	// subtract needed energy from compartments
	Amount<Energy> energyNotProvided = compartments.add(
		energyConsumed.opposite()).getRejected();

	// if the needed energy is not available the fish has starved to death
	if (energyNotProvided.getEstimatedValue() < 0) {
	    throw new StarvedToDeathException();
	}
    }

    /**
     * Updates biomass from compartments. Fish will grow in size and
     * {@link #virtualAge} be increased if enough biomass could be accumulated.
     * 
     * @param delta
     */
    private void grow(Amount<Duration> delta) {
	age = age.plus(delta);
	Amount<Duration> newVirtualAge = virtualAge.plus(delta);

	Amount<Length> expectedLength = FormulaUtil.expectedLength(
		speciesDefinition.getGrowthLength(),
		speciesDefinition.getGrowthCoeff(), newVirtualAge,
		speciesDefinition.getBirthLength());

	expectedBiomass = FormulaUtil.expectedMass(
		speciesDefinition.getLengthMassCoeff(), expectedLength,
		speciesDefinition.getLengthMassExponent());
	biomass = FormulaUtil.biomassFromCompartments(compartments);

	// fish had enough energy to grow, update length and virtual age
	if (biomass.isGreaterThan(expectedBiomass)) {
	    length = expectedLength;
	    virtualAge = newVirtualAge;

	    // fish turns adult if it reaches a certain length
	    if (lifeStage == LifeStage.JUVENILE
		    && length.isGreaterThan(speciesDefinition.getAdultLength())) {
		lifeStage = LifeStage.ADULT;
	    }
	}
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

    public Amount<Duration> getAge() {
	return age;
    }

    public Amount<Mass> getBiomass() {
	return biomass;
    }

    public Amount<Mass> getExpectedBiomass() {
	return expectedBiomass;
    }

    public Amount<Length> getLength() {
	return length;
    }

    public Amount<Energy> getEnergyIngested() {
	return energyIngested;
    }

    public Amount<Energy> getEnergyConsumed() {
	return energyConsumed;
    }

    public LifeStage getLifeStage() {
	return lifeStage;
    }

    public boolean isFemale() {
	return female;
    }

    /**
     * @see #DESIRED_EXCESS_SMR
     * @return True until desired excess amount is achieved
     */
    public boolean isHungry() {
	Amount<Energy> excessAmount = compartments
		.getAmount(CompartmentType.EXCESS);
	Amount<Energy> desiredExcessAmount = DESIRED_EXCESS_SMR.times(
		standardMetabolicRate).to(excessAmount.getUnit());

	return desiredExcessAmount.isGreaterThan(excessAmount);
    }

    @Override
    public String toString() {
	return "Metabolism [ingested=" + energyIngested + ", needed="
		+ energyConsumed + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(compartments, state, compartments
			.getClass().getSimpleName()));
    }

    public class MyPropertiesProxy {
	public boolean isFemale() {
	    return female;
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
	    return energyIngested.doubleValue(KILO(JOULE));
	}

	public double getEnergyConsumed_kJ() {
	    return energyConsumed.doubleValue(KILO(JOULE));
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

    public static class StarvedToDeathException extends Exception {
	private static final long serialVersionUID = 1L;
    }

}

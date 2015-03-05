package de.zmt.kitt.sim.engine.agent.fish;

import static javax.measure.unit.SI.*;

import java.util.logging.Logger;

import javax.measure.quantity.Energy;

import org.jscience.physics.amount.Amount;

import sim.engine.storage.*;
import sim.util.Proxiable;
import de.zmt.kitt.util.AmountUtil;

/**
 * Compound energy {@link MutableStorage} consisting of all simulated body
 * compartments that play a part in metabolism. Responsible for storing and
 * releasing energy.
 * <p>
 * Incoming raw energy from food is digested within the gut over a time span and
 * decreases in amount. After digestion, the energy is stored in a short-term
 * buffer where it is used directly for consumption or stored in other
 * compartments, which are either fat, protein or reproduction (ovaries).
 * 
 * @author cmeyer
 * 
 */
public class Compartments implements MutableStorage<Energy>, Proxiable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Compartments.class
	    .getName());

    /** Energy compartments to consume from in depletion order. */
    private static final CompartmentType[] CONSUMABLE_COMPARTMENTS = {
	    CompartmentType.SHORTTERM, CompartmentType.FAT,
	    CompartmentType.PROTEIN };

    /** Processes food to energy. */
    private final StoragePipeline<Energy> gut;
    /** short-term storage (kJ) */
    private final MutableStorage<Energy> shortterm;
    /** fat storage (kJ) */
    private final MutableStorage<Energy> fat;
    /** protein storage (kJ) */
    private final MutableStorage<Energy> protein;
    /** reproduction storage (kJ) */
    private final MutableStorage<Energy> reproduction;

    public Compartments(StoragePipeline<Energy> gut,
	    MutableStorage<Energy> shortterm,
	    MutableStorage<Energy> fat,
	    MutableStorage<Energy> protein,
	    MutableStorage<Energy> reproduction) {
	this.gut = gut;
	this.shortterm = shortterm;
	this.fat = fat;
	this.protein = protein;
	this.reproduction = reproduction;
    }

    /**
     * Get digested energy from gut and transfer it to other compartments,
     * divided by given growth fractions.
     * 
     * @param growthFractionFat
     * @param growthFractionProtein
     * @param growthFractionRepro
     */
    public void transferDigested(double growthFractionFat,
	    double growthFractionProtein, double growthFractionRepro) {
	// transfer energy from digested food to short-term storage
	Amount<Energy> surplus = shortterm.add(gut.drainExpired())
		.getRejectedAmount();

	// Store energy surplus in body compartments.
	if (surplus.getEstimatedValue() > 0) {
	    Amount<Energy> surplusFat = surplus.times(growthFractionFat);
	    Amount<Energy> surplusProtein = surplus
		    .times(growthFractionProtein);
	    Amount<Energy> surplusRepro = surplus.times(growthFractionRepro);

	    Amount<Energy> rejectedFat = fat.add(surplusFat)
		    .getRejectedAmount();
	    Amount<Energy> rejectedRepro = reproduction.add(surplusRepro)
		    .getRejectedAmount();

	    // excess energy is stored in protein
	    protein.add(surplusProtein.plus(rejectedFat).plus(rejectedRepro));

	    // Inform about excess. Might be a hint for model errors.
	    if (rejectedFat.getEstimatedValue() > 0) {
		logger.fine("Fat storage exceeded. " + rejectedFat
			+ " were stored in protein.");
	    }
	    if (rejectedRepro.getEstimatedValue() > 0) {
		logger.fine("Reproduction storage exceeded. " + rejectedRepro
			+ " were stored in protein.");
	    }
	}
    }

    /**
     * 
     * @param type
     * @return Energy stored in given {@link CompartmentType}
     */
    public Amount<Energy> getAmount(CompartmentType type) {
	return getCompartment(type).getAmount();
    }

    /**
     * 
     * @param type
     * @return object of compartment with given type
     */
    protected MutableStorage<Energy> getCompartment(CompartmentType type) {
	switch (type) {
	case GUT:
	    return gut;
	case SHORTTERM:
	    return shortterm;
	case FAT:
	    return fat;
	case PROTEIN:
	    return protein;
	case REPRODUCTION:
	    return reproduction;
	default:
	    throw new IllegalArgumentException("Invalid compartment type.");
	}
    }

    /** Sum of energy stored in all compartments */
    @Override
    public Amount<Energy> getAmount() {
	return gut.getAmount().plus(shortterm.getAmount())
		.plus(fat.getAmount()).plus(protein.getAmount())
		.plus(reproduction.getAmount());
    }

    /**
     * If {@code amount} is positive, it will enter the gut and be digested,
     * otherwise the negative amount is subtracted from compartments in the
     * following order:<br>
     * Short-term, fat, reproduction, protein.
     * <p>
     * In the positive case a rejected amount will imply that the gut cannot
     * store any more food without exceeding its capacity limits. If there is a
     * rejected amount in the negative case, the fish cannot consume the given
     * amount and will die due to starvation.
     */
    @Override
    public ChangeResult<Energy> add(Amount<Energy> amount) {
	if (amount.getEstimatedValue() > 0) {
	    // incoming food: add energy to gut
	    return gut.add(amount);
	} else {
	    // consuming energy: subtract from compartments
	    Amount<Energy> energyToConsume = amount;
	    Amount<Energy> storedEnergy = AmountUtil.zero(amount);
	    for (int i = 0; energyToConsume.getEstimatedValue() < 0
		    && i < CONSUMABLE_COMPARTMENTS.length; i++) {
		// take the next compartment until nothing gets rejected
		// if last compartment still rejects, the fish will die
		ChangeResult<Energy> result = getCompartment(
			CONSUMABLE_COMPARTMENTS[i]).add(energyToConsume);
		energyToConsume = result.getRejectedAmount();
		storedEnergy.plus(result.getStoredAmount());
	    }
	    return new ChangeResult<Energy>(storedEnergy, energyToConsume);
	}
    }

    /** Clear all compartments and return the sum */
    @Override
    public Amount<Energy> clear() {
	return gut.clear().plus(shortterm.clear()).plus(fat.clear())
		.plus(protein.clear()).plus(reproduction.clear());
    }

    public enum CompartmentType {
	GUT, SHORTTERM, FAT, PROTEIN, REPRODUCTION
    }

    @Override
    public String toString() {
	return "Compartments [total=" + getAmount() + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public int getGutContentSize() {
	    return gut.getPipelineSize();
	}

	public double getGutContent_kJ() {
	    return gut.getAmount().doubleValue(KILO(JOULE));
	}

	public double getShortterm_kJ() {
	    return shortterm.getAmount().doubleValue(KILO(JOULE));
	}

	public double getFat_kJ() {
	    return fat.getAmount().doubleValue(KILO(JOULE));
	}

	public double getProtein_kJ() {
	    return protein.getAmount().doubleValue(KILO(JOULE));
	}

	public double getReproduction_kJ() {
	    return reproduction.getAmount().doubleValue(KILO(JOULE));
	}
    }
}
package de.zmt.ecs.component.agent;

import java.util.logging.Logger;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.storage.*;
import de.zmt.util.*;
import sim.util.*;

/**
 * Compound energy {@link MutableStorage} consisting of all simulated body
 * compartments that play a part in metabolism. Responsible for storing and
 * releasing energy.
 * <p>
 * Incoming raw energy from food is digested within the gut over a time span and
 * decreases in amount. After digestion, the energy is stored in a short-term
 * buffer where it is used directly for consumption or stored in other
 * compartments, which are either fat, protein or reproduction (ovaries).
 * <p>
 * Any energy left will be stored in excess, which makes the fish stop being
 * hungry if filled more than the desired amount. This ensures other
 * compartments being full before the fish stops feeding.
 * 
 * @author mey
 * 
 */
public class Compartments implements LimitedStorage<Energy>, Proxiable, Component {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Compartments.class.getName());

    /** Compartments to consume from in depletion order. */
    private static final Compartment.Type[] CONSUMABLE_COMPARTMENTS = { Compartment.Type.SHORTTERM,
	    Compartment.Type.FAT, Compartment.Type.PROTEIN };
    /** Compartments included in biomass. */
    private static final Compartment.Type[] BIOMASS_COMPARTMENTS = { Compartment.Type.SHORTTERM, Compartment.Type.FAT,
	    Compartment.Type.PROTEIN, Compartment.Type.EXCESS };

    /**
     * Gut storage. Processes food to energy. Modeled as portions of amounts of
     * energy that can be consumed after some time has passed.
     */
    private final Gut gut;
    /** Short-term storage (kJ) */
    private final ShorttermStorage shortterm;
    /** Fat storage (kJ) */
    private final FatStorage fat;
    /**
     * Protein storage (kJ). Represents vital body tissue like muscle, organs
     * and skin.
     */
    private final ProteinStorage protein;
    /**
     * Reproduction storage (kJ). Represents stored reproductive energy like
     * ovaries.
     */
    private final ReproductionStorage reproduction;
    /** Excess storage (kJ). Stores energy if other compartments are full. */
    private final ExcessStorage excess;

    /**
     * Creates a new compartment storage with given compartments.
     * 
     * @see de.zmt.storage
     * @param gut
     * @param shortterm
     * @param fat
     * @param protein
     * @param reproduction
     */
    public Compartments(Gut gut, ShorttermStorage shortterm, FatStorage fat, ProteinStorage protein,
	    ReproductionStorage reproduction) {
	this.gut = gut;
	this.shortterm = shortterm;
	this.fat = fat;
	this.protein = protein;
	this.reproduction = reproduction;
	this.excess = new ExcessStorage();
    }

    /**
     * Get digested energy from gut and transfer it to other compartments,
     * divided by given growth fractions. The consumed energy is subtracted
     * first from the digested, then from the other compartments.
     * 
     * @param reproductive
     *            reproductive fish transfer energy to reproduction storage
     * @param consumedEnergy
     *            the energy that was consumed (negative amount)
     * @return a change result which contains the energy that could not be
     *         provided
     */
    public ChangeResult<Energy> transferDigested(boolean reproductive, Amount<Energy> consumedEnergy) {
	// first subtract energy from digested
	Amount<Energy> remaining = excess.clear().plus(gut.drainExpired()).plus(consumedEnergy);

	// if digested energy was not enough:
	if (remaining.getEstimatedValue() < 0) {
	    // ... subtract it from compartments
	    return add(remaining);
	}

	// digested was more than consumed energy: add remaining to shortterm
	ChangeResult<Energy> shorttermResult = shortterm.add(remaining);
	Amount<Energy> stored = shorttermResult.getStored();

	// Store energy surplus from shortterm in body compartments.
	Amount<Energy> surplus = shorttermResult.getRejected();
	if (surplus.getEstimatedValue() > 0) {
	    Amount<Energy> surplusFat = surplus.times(Compartment.Type.FAT.getGrowthFraction(reproductive));
	    Amount<Energy> surplusProtein = surplus.times(Compartment.Type.PROTEIN.getGrowthFraction(reproductive));
	    Amount<Energy> surplusRepro = surplus.times(Compartment.Type.REPRODUCTION.getGrowthFraction(reproductive));

	    ChangeResult<Energy> fatResult = fat.add(surplusFat);
	    ChangeResult<Energy> proteinResult = protein.add(surplusProtein);
	    ChangeResult<Energy> reproductionResult = reproduction.add(surplusRepro);

	    // store exceeding energy in excess
	    ChangeResult<Energy> excessResult = excess.add(
		    fatResult.getRejected().plus(proteinResult.getRejected()).plus(reproductionResult.getRejected()));

	    // sum stored energy
	    stored = stored.plus(fatResult.getStored()).plus(proteinResult.getStored())
		    .plus(reproductionResult.getStored().plus(excessResult.getStored()));
	}
	
	return new ChangeResult<>(stored, AmountUtil.zero(consumedEnergy));
    }

    /**
     * @return sum of mass amounts from all compartments excluding gut.
     */
    public Amount<Mass> computeBiomass() {
	Amount<Mass> biomass = AmountUtil.zero(UnitConstants.BIOMASS);
	for (Compartment.Type type : BIOMASS_COMPARTMENTS) {
	    biomass = biomass.plus(getStorage(type).toMass());
	}

	return biomass;
    }

    /**
     * 
     * @param type
     * @return Energy stored in given {@link de.zmt.storage.Compartment.Type}
     */
    public Amount<Energy> getStorageAmount(Compartment.Type type) {
	return getStorage(type).getAmount();
    }

    /**
     * @return true if ready for reproduction
     */
    public boolean canReproduce() {
	return reproduction.atUpperLimit();
    }

    /**
     * Clears reproduction storage, i.e. the fish lays its eggs.
     * 
     * @return Energy amount cleared from storage
     */
    public Amount<Energy> clearReproductionStorage() {
	return reproduction.clear();
    }

    /**
     * 
     * @param type
     * @return object of compartment with given type
     */
    private Compartment getStorage(Compartment.Type type) {
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
	case EXCESS:
	    return excess;
	default:
	    throw new IllegalArgumentException("Invalid compartment type.");
	}
    }

    /** Sum of energy stored in all compartments */
    @Override
    public Amount<Energy> getAmount() {
	Amount<Energy> sum = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);

	for (Compartment.Type type : Compartment.Type.values()) {
	    sum = sum.plus(getStorageAmount(type));
	}

	return sum;
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
	if (amount.getEstimatedValue() >= 0) {
	    // incoming food: add energy to gut
	    return gut.add(amount);
	} else {
	    // consuming energy: subtract from compartments
	    Amount<Energy> consumedEnergy = amount;
	    Amount<Energy> storedEnergy = AmountUtil.zero(amount);
	    for (int i = 0; consumedEnergy.getEstimatedValue() < 0 && i < CONSUMABLE_COMPARTMENTS.length; i++) {
		// take the next compartment until nothing gets rejected
		// if last compartment still rejects, the fish will die
		ChangeResult<Energy> result = getStorage(CONSUMABLE_COMPARTMENTS[i]).add(consumedEnergy);
		consumedEnergy = result.getRejected();
		storedEnergy = storedEnergy.plus(result.getStored());
	    }
	    return new ChangeResult<>(storedEnergy, consumedEnergy);
	}
    }

    /** Clears all compartments and returns the sum. */
    @Override
    public Amount<Energy> clear() {
	return gut.clear().plus(shortterm.clear()).plus(fat.clear()).plus(protein.clear()).plus(reproduction.clear());
    }

    /**
     * Returns <code>true</code> if no energy can be retrieved, i.e. if the
     * protein storage is at its minimum capacity.
     */
    @Override
    public boolean atLowerLimit() {
	return protein.atLowerLimit();
    }

    /**
     * Returns <code>true</code> if no incoming energy can be stored, i.e. if
     * the gut is at its maximum capacity.
     */
    @Override
    public boolean atUpperLimit() {
	return gut.atUpperLimit();
    }

    @Override
    public double doubleValue() {
	return getAmount().getEstimatedValue();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [total=" + getAmount() + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Valuable getTotal() {
	    return ValuableAmountAdapter.wrap(getAmount());
	}

	public Gut getGut() {
	    return gut;
	}

	public ShorttermStorage getShortterm() {
	    return shortterm;
	}

	public FatStorage getFat() {
	    return fat;
	}

	public ProteinStorage getProtein() {
	    return protein;
	}

	public ReproductionStorage getReproduction() {
	    return reproduction;
	}

	public ExcessStorage getExcess() {
	    return excess;
	}
    }

    public static class ExcessStorage extends Compartment.AbstractCompartmentStorage {
	private static final long serialVersionUID = 1L;

	@Override
	public Type getType() {
	    return Type.EXCESS;
	}
    }
}
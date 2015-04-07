package de.zmt.kitt.sim.engine.agent.fish;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.SpecificEnergy;

/**
 * Body compartment identified by its {@link Type}.
 * 
 * @author cmeyer
 * 
 */
public interface Compartment {
    /**
     * @return {@link Type} of compartment.
     */
    Type getType();

    /**
     * Body compartment types storing energy including conversion methods from
     * mass to energy and energy to mass.
     * 
     * @author cmeyer
     * 
     */
    public enum Type {
	GUT, SHORTTERM, FAT, PROTEIN, REPRODUCTION, EXCESS;

	private static final String ILLEGAL_ARGUMENT_MSG = "No valid value for ";

	// ENERGY-BIOMASS CONVERSIONS
	// TODO verify values
	private static final double KJ_PER_GRAM_FAT_VALUE = 36.3;
	private static final double KJ_PER_GRAM_PROTEIN_VALUE = 6.5;
	private static final double KJ_PER_GRAM_REPRO_VALUE = 23.5;
	/**
	 * 1 g fat = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable energy
	 * 
	 * @see #GRAM_PER_KJ_FAT
	 */
	private static final Amount<SpecificEnergy> KJ_PER_GRAM_FAT = Amount
		.valueOf(KJ_PER_GRAM_FAT_VALUE, UnitConstants.ENERGY_CONTENT_TISSUE);
	/**
	 * 1 g protein = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable energy
	 * 
	 * @see #GRAM_PER_KJ_PROTEIN
	 */
	private static final Amount<SpecificEnergy> KJ_PER_GRAM_PROTEIN = Amount
		.valueOf(KJ_PER_GRAM_PROTEIN_VALUE,
			UnitConstants.ENERGY_CONTENT_TISSUE);
	/**
	 * 1 g reproduction = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable
	 * energy
	 * 
	 * @see #GRAM_PER_KJ_REPRO
	 */
	private static final Amount<SpecificEnergy> KJ_PER_GRAM_REPRO = Amount
		.valueOf(KJ_PER_GRAM_REPRO_VALUE,
			UnitConstants.ENERGY_CONTENT_TISSUE);
	/**
	 * 1 kJ fat = (1/{@value #KJ_PER_GRAM_FAT_VALUE}) g fat in biomass
	 * 
	 * @see #KJ_PER_GRAM_FAT
	 */
	private static final Amount<?> GRAM_PER_KJ_FAT = KJ_PER_GRAM_FAT
		.inverse();
	/**
	 * 1 kJ gut / short-term / protein = (1/
	 * {@value #KJ_PER_GRAM_PROTEIN_VALUE}) g protein in biomass
	 * 
	 * @see #KJ_PER_GRAM_PROTEIN
	 */
	private static final Amount<?> GRAM_PER_KJ_PROTEIN = KJ_PER_GRAM_PROTEIN
		.inverse();
	/**
	 * 1 kJ reproduction = (1/{@value #KJ_PER_GRAM_REPRO_VALUE}) g
	 * reproduction in biomass
	 * 
	 * @see #KJ_PER_GRAM_REPRO
	 */
	private static final Amount<?> GRAM_PER_KJ_REPRO = KJ_PER_GRAM_REPRO
		.inverse();

	// GROWTH FRACTIONS
	// TODO growth fraction values differ from document. verify.
	/** Fraction of protein growth from total. */
	private static final double GROWTH_FRACTION_PROTEIN = 0.95;
	/** Fraction of fat growth from total for non-reproductive fish. */
	private static final double GROWTH_FRACTION_FAT_MALE = 1 - GROWTH_FRACTION_PROTEIN;
	/**
	 * Fraction of reproduction energy growth from total for reproductive
	 * fish.
	 */
	private static final double GROWTH_FRACTION_REPRO_REPRODUCTIVE = 0.015;
	/** Fraction of fat growth from total for reproductive fish. */
	private static final double GROWTH_FRACTION_FAT_REPRODUCTIVE = 1
		- GROWTH_FRACTION_PROTEIN - GROWTH_FRACTION_REPRO_REPRODUCTIVE;

	public Amount<SpecificEnergy> getKjPerGram() {
	    switch (this) {
	    case FAT:
		return KJ_PER_GRAM_FAT;
	    case SHORTTERM:
	    case PROTEIN:
	    case EXCESS:
		return KJ_PER_GRAM_PROTEIN;
	    case REPRODUCTION:
		return KJ_PER_GRAM_REPRO;
	    default:
		throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
	    }
	}

	public Amount<?> getGramPerKj() {
	    switch (this) {
	    case FAT:
		return GRAM_PER_KJ_FAT;
	    case SHORTTERM:
	    case PROTEIN:
	    case EXCESS:
		return GRAM_PER_KJ_PROTEIN;
	    case REPRODUCTION:
		return GRAM_PER_KJ_REPRO;
	    default:
		throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
	    }
	}

	/**
	 * @param reproductive
	 *            reproductive fish store energy for reproduction
	 * @return Fraction of growth for this compartment type.
	 */
	public double getGrowthFraction(boolean reproductive) {
	    switch (this) {
	    case FAT:
		return reproductive ? GROWTH_FRACTION_FAT_REPRODUCTIVE
			: GROWTH_FRACTION_FAT_MALE;
	    case PROTEIN:
		return GROWTH_FRACTION_PROTEIN;
	    case REPRODUCTION:
		return reproductive ? GROWTH_FRACTION_REPRO_REPRODUCTIVE : 0;
	    default:
		throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
	    }
	}
    }
}
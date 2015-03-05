package de.zmt.kitt.util;

import static java.lang.Math.*;
import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.sim.engine.agent.fish.*;
import de.zmt.kitt.sim.engine.agent.fish.Compartments.CompartmentType;
import de.zmt.kitt.util.quantity.EnergyDensity;

/**
 * Utility class for scientific formulae.
 * 
 * @author cmeyer
 * 
 */
public class FormulaUtil {
    // ENERGY METABOLISM
    private static final double SMR_COEFF_VALUE = 0.307;
    /**
     * (RMR in kj/h)= {@value #SMR_COEFF_VALUE} * (g fish wet weight)^
     * {@value #SMR_EXPONENT}
     */
    private static final Amount<Power> SMR_COEFF = Amount.valueOf(
	    SMR_COEFF_VALUE, KILO(JOULE).divide(HOUR).asType(Power.class));
    /**
     * (RMR in kj/h)= {@value #SMR_COEFF_VALUE} * (g fish wet weight)^
     * {@value #SMR_EXPONENT}
     */
    private static final double SMR_EXPONENT = 0.81;

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
    private static final Amount<EnergyDensity> KJ_PER_GRAM_FAT = Amount
	    .valueOf(KJ_PER_GRAM_FAT_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);
    /**
     * 1 g protein = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable energy
     * 
     * @see #GRAM_PER_KJ_PROTEIN
     */
    private static final Amount<EnergyDensity> KJ_PER_GRAM_PROTEIN = Amount
	    .valueOf(KJ_PER_GRAM_PROTEIN_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);
    /**
     * 1 g reproduction = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable
     * energy
     * 
     * @see #GRAM_PER_KJ_REPRO
     */
    private static final Amount<EnergyDensity> KJ_PER_GRAM_REPRO = Amount
	    .valueOf(KJ_PER_GRAM_REPRO_VALUE, AmountUtil.ENERGY_DENSITY_UNIT);

    /**
     * 1 kJ fat = (1/{@value #KJ_PER_GRAM_FAT_VALUE}) g fat in biomass
     * 
     * @see #KJ_PER_GRAM_FAT
     */
    private static final Amount<?> GRAM_PER_KJ_FAT = KJ_PER_GRAM_FAT.inverse();
    /**
     * 1 kJ gut / short-term / protein = (1/{@value #KJ_PER_GRAM_PROTEIN_VALUE})
     * g protein in biomass
     * 
     * @see #KJ_PER_GRAM_PROTEIN
     */
    private static final Amount<?> GRAM_PER_KJ_PROTEIN = KJ_PER_GRAM_PROTEIN
	    .inverse();
    /**
     * 1 kJ reproduction = (1/{@value #KJ_PER_GRAM_REPRO_VALUE}) g reproduction
     * in biomass
     * 
     * @see #KJ_PER_GRAM_REPRO
     */
    private static final Amount<?> GRAM_PER_KJ_REPRO = KJ_PER_GRAM_REPRO
	    .inverse();

    private static final double INITIAL_FRACTION_FAT = 0.05;
    private static final double INITIAL_FRACTION_PROTEIN = 0.95;

    // INITIALIZE
    /**
     * 
     * @param biomass
     *            preferably in g
     * @return body fat energy in kJ
     */
    public static Amount<Energy> initialFat(Amount<Mass> biomass) {
	return energyInCompartment(biomass, INITIAL_FRACTION_FAT,
		KJ_PER_GRAM_FAT);
    }

    /**
     * 
     * @param biomass
     *            preferably in g
     * @return body protein energy in kJ
     */
    public static Amount<Energy> initialProtein(Amount<Mass> biomass) {
	return energyInCompartment(biomass, INITIAL_FRACTION_PROTEIN,
		KJ_PER_GRAM_PROTEIN);
    }

    /**
     * Energy amount in compartment as a fraction of total biomass.
     * 
     * @param biomass
     *            preferably in g
     * @param fraction
     *            of total biomass
     * @param kJPerGram
     *            in that compartment
     * @return {@link Amount} of energy
     */
    private static Amount<Energy> energyInCompartment(Amount<Mass> biomass,
	    double fraction, Amount<?> kJPerGram) {
	return biomass.times(fraction).times(kJPerGram)
		.to(AmountUtil.ENERGY_UNIT);
    }

    // METABOLISM
    /**
     * The standard metabolic rate (SMR) is the minimum rate of energy the fish
     * consumes. Any activity adds up on it.<br>
     * {@code kj/h = A*(g fish wet weight)^B}
     * 
     * @param biomass
     * @return SMR in kJ/h
     */
    public static Amount<Power> standardMetabolicRate(Amount<Mass> biomass) {
	double biomassFactor = Math
		.pow(biomass.doubleValue(GRAM), SMR_EXPONENT);
	return SMR_COEFF.times(biomassFactor);
    }

    // GROWTH
    /**
     * vBGF: {@code L(t)= L * (1 - e^(-K * (t - t(0))))},<br>
     * changed to {@code L(t)= L*(1 - e^(-K * t)) + L(0))}
     * 
     * @param growthLength
     *            Length of fish that it grows during its lifetime. (L)
     * @param growthCoeff
     *            Coefficient defining steepness of growth curve. (K)
     * @param age
     *            of fish, preferably in years. (T)
     * @param birthLength
     *            Length of fish at birth (L(0))
     * @return {@link Amount} of expected length at given age with the unit of
     *         {@code fullGrownSizeL} being passed. (L(t))
     */
    public static Amount<Length> expectedLength(Amount<Length> growthLength,
	    double growthCoeff, Amount<Duration> age, Amount<Length> birthLength) {
	return growthLength.times(
		1 - exp(-growthCoeff * (age.to(YEAR).getEstimatedValue())))
		.plus(birthLength);
    }

    /**
     * Expected biomass at given length, without reproduction:<br>
     * {@code WW = a * L ^ b}
     * 
     * @param lengthMassCoeff
     *            length coefficient (a)
     * @param length
     *            length of fish (L)
     * @param lengthMassExponent
     *            (e)
     * @return {@link Amount} of expected mass without reproduction at given
     *         size (WW)
     */
    public static Amount<Mass> expectedMass(Amount<Mass> lengthMassCoeff,
	    Amount<Length> length, double lengthMassExponent) {
	double lengthFactor = pow(length.to(CENTIMETER).getEstimatedValue(),
		lengthMassExponent);
	return lengthMassCoeff.times(lengthFactor);
    }

    /**
     * Compute biomass from energy values of body compartments, excluding gut.
     * 
     * @param compartments
     * @return {@link Amount} of biomass in g.
     */
    public static Amount<Mass> biomassFromCompartments(Compartments compartments) {
	Amount<Energy> shorttermEnergy = compartments
		.getAmount(CompartmentType.SHORTTERM);
	Amount<Energy> proteinEnergy = compartments
		.getAmount(CompartmentType.PROTEIN);
	Amount<Energy> fatEnergy = compartments.getAmount(CompartmentType.FAT);
	Amount<Energy> reproEnergy = compartments
		.getAmount(CompartmentType.REPRODUCTION);

	Amount<?> convertedProtein = (shorttermEnergy.plus(proteinEnergy))
		.times(GRAM_PER_KJ_PROTEIN);
	Amount<?> convertedFat = fatEnergy.times(GRAM_PER_KJ_FAT);
	Amount<?> convertedRepro = reproEnergy.times(GRAM_PER_KJ_REPRO);

	return (convertedProtein.plus(convertedFat).plus(convertedRepro))
		.to(AmountUtil.MASS_UNIT);
    }
}

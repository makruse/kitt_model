package de.zmt.kitt.util;

import static java.lang.Math.*;
import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.*;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.sim.engine.agent.fish.Compartment;

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

    // INITIALIZE
    /**
     * 
     * @param biomass
     *            preferably in g
     * @return body fat energy in kJ
     */
    public static Amount<Energy> initialFat(Amount<Mass> biomass) {
	return energyInCompartment(biomass, Compartment.Type.FAT);
    }

    /**
     * 
     * @param biomass
     *            preferably in g
     * @return body protein energy in kJ
     */
    public static Amount<Energy> initialProtein(Amount<Mass> biomass) {
	return energyInCompartment(biomass, Compartment.Type.PROTEIN);
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
	    Compartment.Type type) {
	return biomass.times(type.getGrowthFraction(false))
		.times(type.getEnergyDensity()).to(AmountUtil.ENERGY_UNIT);
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
	double lengthFactor = pow(length.doubleValue(CENTIMETER),
		lengthMassExponent);
	return lengthMassCoeff.times(lengthFactor);
    }
}

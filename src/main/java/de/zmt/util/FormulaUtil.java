package de.zmt.util;

import static java.lang.Math.*;
import static javax.measure.unit.NonSI.YEAR;
import static javax.measure.unit.SI.*;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import de.zmt.storage.Compartment;
import de.zmt.util.quantity.AreaDensity;

/**
 * Utility class for scientific formulae.
 * 
 * @author mey
 * 
 */
public final class FormulaUtil {
    private FormulaUtil() {

    }

    private static final double RMR_COEFF_ML_O2_PER_H = 0.307;
    private static final double RMR_MG_PER_ML_O2 = 1.429;
    private static final double RMR_KJ_PER_MG_O2 = 0.0142;
    private static final double RMR_COEFF_KJ_PER_H_VALUE = RMR_COEFF_ML_O2_PER_H * RMR_MG_PER_ML_O2 * RMR_KJ_PER_MG_O2;
    /**
     * @see #restingMetabolicRate(Amount)
     */
    private static final Amount<Power> RMR_COEFF = Amount.valueOf(RMR_COEFF_KJ_PER_H_VALUE,
	    UnitConstants.ENERGY_PER_TIME);
    /**
     * @see #restingMetabolicRate(Amount)
     */
    private static final double RMR_DEGREE = 0.81;

    /**
     * Returns the initial amount of body fat derived from its growth fraction.
     * 
     * @see de.zmt.storage.Compartment.Type#getGrowthFraction(boolean)
     * @param biomass
     *            preferably in g
     * @return body fat energy in kJ
     */
    public static Amount<Energy> initialFat(Amount<Mass> biomass) {
	return energyInCompartment(biomass, Compartment.Type.FAT);
    }

    /**
     * Returns the initial amount of body protein derived from its growth
     * fraction.
     * 
     * @see de.zmt.storage.Compartment.Type#getGrowthFraction(boolean)
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
     * @param type
     *            of compartment
     * @return {@link Amount} of energy
     */
    private static Amount<Energy> energyInCompartment(Amount<Mass> biomass, Compartment.Type type) {
	return type.toEnergy(biomass.times(type.getGrowthFraction(false)));
    }

    // METABOLISM
    /**
     * The resting metabolic rate (RMR) is the minimum rate of energy the fish
     * consumes. Any activity adds up on it.
     * 
     * <pre>
     * RMR in kj/h = ({@value #RMR_COEFF_ML_O2_PER_H} * biomass [g] ^ {@value #RMR_DEGREE}) [ml O<sub>2</sub>/h]
     *  * {@value #RMR_MG_PER_ML_O2} [mg O<sub>2</sub>/ml O<sub>2</sub>] * {@value #RMR_KJ_PER_MG_O2} [kJ/mg O<sub>2</sub>]
     * </pre>
     * 
     * @see "Winberg 1960 from Bochdansky & Legett 2000"
     * @param biomass
     * @return RMR in kJ/h
     */
    public static Amount<Power> restingMetabolicRate(Amount<Mass> biomass) {
	double biomassFactor = Math.pow(biomass.doubleValue(GRAM), RMR_DEGREE);
	return RMR_COEFF.times(biomassFactor);
    }

    // GROWTH
    /**
     * Calculates expected length using von Bertalanffy Growth Function (vBGF).
     * <br>
     * {@code L(t)= L * (1 - e^(-K * (t - t(0))))}
     * 
     * @param asymptoticLength
     *            the mean length the fish of this stock would reach if they
     *            were to grow for an infinitely long period (L)
     * @param growthCoeff
     *            Coefficient defining steepness of growth curve, how fast the
     *            fish approaches its {@code asymptoticLength} (K)
     * @param age
     *            age of the fish, preferably in years (T)
     * @param zeroSizeAge
     *            age at which the fish has a size of zero (t(0))
     * @return {@link Amount} of expected length at given age (L(t))
     */
    public static Amount<Length> expectedLength(Amount<Length> asymptoticLength, double growthCoeff,
	    Amount<Duration> age, Amount<Duration> zeroSizeAge) {
	return asymptoticLength.times(1 - exp(-growthCoeff * (age.doubleValue(YEAR) - zeroSizeAge.doubleValue(YEAR))));
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
    public static Amount<Mass> expectedMass(Amount<Mass> lengthMassCoeff, Amount<Length> length,
	    double lengthMassExponent) {
	double lengthFactor = pow(length.doubleValue(CENTIMETER), lengthMassExponent);
	return lengthMassCoeff.times(lengthFactor);
    }

    /**
     * Calculates total density of algae for the point in time after
     * {@code delta}.
     * <p>
     * {@code dP/dt = r * P (1 - P/K)}<br>
     * {@code P} represents population size (algae density), {@code r} the
     * growth rate and {@code K} the carrying capacity, i.e. the maximum
     * density.
     * 
     * @see <a href=
     *      "https://en.wikipedia.org/wiki/Logistic_function#In_ecology:_modeling_population_growth">
     *      Wikipedia: Modeling Population Growth</a>
     * @param current
     *            density of algae ({@code P}) between {@code 0} and {@code K}.
     * @param max
     *            maximum density ({@code K})
     * @param algalGrowthRate
     *            rate of algal growth ({@code r})
     * @param delta
     *            duration of growth ({@code dt})
     * @return cumulative density of algae present after {@code delta} has
     *         passed ({@code P + dP * dt}). Will not exceed maximum {@code K}.
     * @throws IllegalArgumentException
     *             if {@code current} is beyond maximum density from habitat
     */
    public static Amount<AreaDensity> growAlgae(Amount<AreaDensity> current, Amount<AreaDensity> max,
	    Amount<Frequency> algalGrowthRate, Amount<Duration> delta) {
	if (current.isGreaterThan(max)) {
	    throw new IllegalArgumentException(
		    "Current density is beyond habitat maximum.\ncurrent: " + current + ", max: " + max);
	}

	// growth per time span from growth rate (kg / (m2 * s))
	Amount<?> growth = algalGrowthRate.times(current).times(Amount.ONE.minus(current.divide(max)));
	// cumulative amount of algae for delta
	Amount<AreaDensity> cumulative = current.plus(growth.times(delta));

	// may exceed max, especially if growth rate is high
	return AmountUtil.min(cumulative, max);
    }
}

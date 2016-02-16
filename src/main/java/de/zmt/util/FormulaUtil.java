package de.zmt.util;

import static javax.measure.unit.NonSI.YEAR;
import static javax.measure.unit.SI.*;

import javax.measure.Measurable;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;
import javax.measure.quantity.Velocity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.storage.Compartment;
import de.zmt.util.quantity.AreaDensity;
import de.zmt.util.quantity.LinearMassDensity;

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

    private static final double NET_COST_SWIMMING_CONST = 0.0169406;
    private static final double NET_COST_SWIMMING_COEFF = 0.023572;
    private static final Unit<Velocity> NET_COST_SWIMMING_UNIT_CM_PER_S = CENTIMETER.divide(SECOND)
	    .asType(Velocity.class);
    /**
     * For speed values smaller than this value the result would be negative, so
     * the formula returns 0.
     */
    private static final double NET_COST_SWIMMING_MIN_SPEED = 0.48739777697164716;

    private static final int INITIAL_AGE_DISTRIBUTION_ALPHA = 6;
    private static final int INITIAL_AGE_DISTRIBUTION_BETA = 1;

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
     * RMR in kj/h = ({@value #RMR_COEFF_ML_O2_PER_H} &sdot; biomass [g]<sup>{@value #RMR_DEGREE}</sup>) [ml O<sub>2</sub>/h]
     *  &sdot; {@value #RMR_MG_PER_ML_O2} [mg O<sub>2</sub>/ml O<sub>2</sub>] &sdot; {@value #RMR_KJ_PER_MG_O2} [kJ/mg O<sub>2</sub>]
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
     * Calculates expected length using von Bertalanffy Growth Function (vBGF):
     * 
     * <pre>
     * L(t) = L &sdot; (1 - e<sup>-K &sdot; (t - t(0))</sup>)
     * </pre>
     * 
     * @see "El-Sayed Ali et al. 2011"
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
	return asymptoticLength
		.times(1 - Math.exp(-growthCoeff * (age.doubleValue(YEAR) - zeroSizeAge.doubleValue(YEAR))));
    }

    /**
     * Expected length at given biomass, without reproduction:
     * 
     * <pre>
     * L = (WW / a)<sup>(1 / b)</sup>
     * </pre>
     * 
     * @see #expectedMass(Amount, Amount, double)
     * @see "El-Sayed Ali et al. 2011"
     * @param lengthMassCoeff
     *            length coefficient (a)
     * @param biomass
     *            amount of expected mass without reproduction (WW)
     * @param invLengthMassExponent
     *            (1 / b)
     * @return length of fish (L)
     */
    public static Amount<Length> expectedLength(Amount<LinearMassDensity> lengthMassCoeff, Amount<Mass> biomass,
	    double invLengthMassExponent) {
	double length = biomass.divide(lengthMassCoeff).to(UnitConstants.BODY_LENGTH).getEstimatedValue();
	return Amount.valueOf(Math.pow(length, invLengthMassExponent), UnitConstants.BODY_LENGTH);
    }

    /**
     * Expected biomass at given length, without reproduction:
     * 
     * <pre>
     * WW = a &sdot; L<sup>b</sup>
     * </pre>
     * 
     * @see "El-Sayed Ali et al. 2011"
     * @param lengthMassCoeff
     *            length coefficient (a)
     * @param length
     *            length of fish (L)
     * @param lengthMassExponent
     *            (b)
     * @return amount of expected mass without reproduction at given size (WW)
     */
    public static Amount<Mass> expectedMass(Amount<LinearMassDensity> lengthMassCoeff, Amount<Length> length,
	    double lengthMassExponent) {
	Amount<Length> lengthRaised = Amount.valueOf(Math.pow(length.doubleValue(CENTIMETER), lengthMassExponent),
		CENTIMETER);
	return lengthMassCoeff.times(lengthRaised).to(UnitConstants.BIOMASS);
    }

    /**
     * Computes the net cost of swimming for the given speed.
     * 
     * <pre>
     * cost [ml O<sub>2</sub> / h] = 1.193 + 1.66 &sdot; log(speed [cm / s])
     * cost [kJ / h] = cost [ml O<sub>2</sub> / h] &sdot; 0.0142 [kJ / ml O<sub>2</sub>]
     *               = {@value #NET_COST_SWIMMING_CONST} + {@value #NET_COST_SWIMMING_COEFF} &sdot; log(speed [cm / s])
     * </pre>
     * 
     * For speed values below {@value #NET_COST_SWIMMING_MIN_SPEED}, zero is
     * returned instead of negative values.
     * 
     * @see "Korsmeyer et al. 2002"
     * @param speed
     * @return net cost of swimming
     */
    public static Amount<Power> netCostOfSwimming(Measurable<Velocity> speed) {
	double speedCmPerS = speed.doubleValue(NET_COST_SWIMMING_UNIT_CM_PER_S);

	if (speedCmPerS < NET_COST_SWIMMING_MIN_SPEED) {
	    return AmountUtil.zero(UnitConstants.ENERGY_PER_TIME);
	}
	double costKjPerHour = NET_COST_SWIMMING_COEFF
		+ NET_COST_SWIMMING_CONST * Math.log(speedCmPerS);
	return Amount.valueOf(costKjPerHour, UnitConstants.ENERGY_PER_TIME);
    }

    /**
     * Calculates total density of algae for the point in time after
     * {@code delta}.
     * 
     * <pre>
     * dP/dt = r &sdot; P (1 - P / K)
     * </pre>
     * 
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
     *         passed (<tt>P + dP &sdot; dt</tt>). Will not exceed maximum
     *         {@code K}.
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

    /**
     * Creates initial age durations for a population with many young
     * individuals and only a few older ones. Uses a beta distribution:
     * 
     * <pre>
     * age [s] = x<sup>{@value #INITIAL_AGE_DISTRIBUTION_ALPHA}-1</sup> &sdot; (1-x)<sup>{@value #INITIAL_AGE_DISTRIBUTION_BETA}-1</sup> &sdot; (max - min) + min
     *             = x<sup>{@value #INITIAL_AGE_DISTRIBUTION_ALPHA}-1</sup> &sdot; (max - min) + min
     * </pre>
     * 
     * @param x
     *            the input value between 0 and 1
     * @param max
     *            the maximum age returned
     * @param min
     *            the minimum age returned
     * @return the initial age corresponding to {@code x}
     */
    public static Amount<Duration> initialAgeDistribution(double x, Amount<Duration> max, Amount<Duration> min) {
	Unit<Duration> unit = UnitConstants.AGE;
	return Amount.valueOf(betaDistribution(x, INITIAL_AGE_DISTRIBUTION_ALPHA, INITIAL_AGE_DISTRIBUTION_BETA,
		max.doubleValue(unit), min.doubleValue(unit)), unit);
    }

    private static double betaDistribution(double x, double alpha, double beta, double scale, double shift) {
        double alphaPart = 1;
        double betaPart = 1;
    
        // optimization
        if (alpha != 1) {
            alphaPart = Math.pow(x, alpha - 1);
        }
        if (beta != 1) {
            betaPart = Math.pow(1 - x, beta - 1);
        }
        return alphaPart * betaPart * (scale - shift) + shift;
    }
}

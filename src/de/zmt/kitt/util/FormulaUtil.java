package de.zmt.kitt.util;

import org.joda.time.DateTimeConstants;

/**
 * Utility class for scientific formulae.
 * 
 * @author cmeyer
 * 
 */
public class FormulaUtil {
    /** Minimum repro fraction (of total body weight) */
    private static final double MIN_REPRO_FRACTION = 0.1;
    /** Maximum repro fraction (of total body weight) */
    private static final double MAX_REPRO_FRACTION = 0.3;

    // ENERGY METABOLISM
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    private static final double RESTING_METABOLIC_RATE_A = 0.0072;
    /** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
    private static final double RESTING_METABOLIC_RATE_B = 0.79;
    /** Verlustfaktor bei flow shortterm storage <=> bodyFat */
    private static final double LOSS_FACTOR_FAT_TO_ENERGY = 0.87;
    /** Verlustfaktor bei flow shortterm storage <=> bodyTissue */
    private static final double LOSS_FACTOR_TISSUE_TO_ENERGY = 0.90;
    /** Verlustfaktor bei flow shortterm storage <=> reproFraction */
    private static final double LOSS_FACTOR_REPRO_TO_ENERGY = 0.87;

    // energy-biomass conversions
    /** metabolizable energy (kJ) from 1 g bodyFat */
    private static final double ENERGY_PER_GRAM_FAT = 36.3;
    /** metabolizable energy (kJ) from 1 g bodyTissue */
    private static final double ENERGY_PER_GRAM_TISSUE = 6.5;
    /** metabolizable energy (kJ) from 1 g reproFraction (ovaries) */
    private static final double ENERGY_PER_GRAM_REPRO = 23.5;
    /** 1 kJ fat*conversionRate = fat in g */
    private static final double CONVERSION_RATE_FAT = 0.028;
    /** 1 kJ bodyTissue*conversionRate = body tissue in g */
    private static final double CONVERSION_RATE_TISSUE = 0.154;
    /** 1 kJ repro*conversionRate = repro in g */
    private static final double CONVERSION_RATE_REPRO = 0.043;

    /** Conversion factor for cm/s to m/min */
    private static final double SPEED_CONVERSION_FACTOR = 100d / 60d;

    // INITIALIZE
    /**
     * 
     * @param biomass
     *            in g wet weight
     * @return body tissue energy in kJ
     */
    public static double initialBodyTissue(double biomass) {
	return biomass * 0.95 * ENERGY_PER_GRAM_TISSUE;
    }

    /**
     * 
     * @param biomass
     *            in g wet weight
     * @return body fat energy in kJ
     */
    public static double initialBodyFat(double biomass) {
	return biomass * 0.05 * ENERGY_PER_GRAM_FAT;
    }

    // MOVE
    /**
     * Net activity cost over the given time.
     * <p>
     * (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142<br>
     * Korsmeyer et al., 2002
     * 
     * @param speed
     *            in meter per minute during dt
     * @param dt
     *            delta time in minutes
     * @return cost in kj for maintaining the given speed over given time
     */
    public static final double netActivityCost(double speed, double dt) {
	double activityCostsPerHour = (1.193 * Math.pow(speed
		* SPEED_CONVERSION_FACTOR, 1.66)) * 0.0142;
	return (activityCostsPerHour / DateTimeConstants.MINUTES_PER_HOUR) * dt;
    }

    // FEED

    // UPDATE ENERGY

    // REPRODUCE
    /**
     * 20-30% of biomass energy needed for reproduction.
     * 
     * @see "Wootton 1985"
     * @param biomass
     * @return
     */
    public static double energyNeededForReproduction(double biomass) {
	return biomass * 0.2 * ENERGY_PER_GRAM_REPRO;
    }

}

package de.zmt.util;

import static javax.measure.unit.NonSI.YEAR;
import static javax.measure.unit.SI.*;
import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.zmt.util.quantity.LinearMassDensity;

public class FormulaUtilTest {
    private static final double MAX_ERROR = 1e-13;

    private static final Amount<Length> ASYMPTOTIC_LENGTH = Amount.valueOf(39.1, CENTIMETER)
            .to(UnitConstants.BODY_LENGTH);
    private static final double GROWTH_COEFF = 0.15;
    private static final Amount<Duration> ZERO_SIZE_AGE = Amount.valueOf(-1.25, YEAR);

    private static final Amount<LinearMassDensity> LENGTH_MASS_COEFF = Amount.valueOf(0.0319, GRAM.divide(CENTIMETER))
            .to(UnitConstants.MASS_PER_LENGTH);
    private static final double LENGTH_MASS_EXPONENT = 2.928;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void expectedLengthAndAge() {
        Amount<Duration> age = Amount.valueOf(1, YEAR);
        Amount<Length> expectedLength = FormulaUtil.expectedLength(ASYMPTOTIC_LENGTH, GROWTH_COEFF, age, ZERO_SIZE_AGE);
        Amount<Duration> expectedAge = FormulaUtil.expectedAge(ASYMPTOTIC_LENGTH, GROWTH_COEFF, expectedLength,
                ZERO_SIZE_AGE);
        assertThat(age, is(amountCloseTo(expectedAge, MAX_ERROR)));
    }

    @Test
    public void expectedMassAndLength() {
        Amount<Length> length = Amount.valueOf(1, METER);
        Amount<Mass> expectedMass = FormulaUtil.expectedMass(LENGTH_MASS_COEFF, length, LENGTH_MASS_EXPONENT);
        Amount<Length> expectedLength = FormulaUtil.expectedLength(LENGTH_MASS_COEFF, expectedMass,
                1 / LENGTH_MASS_EXPONENT);
        assertThat(length, is(amountCloseTo(expectedLength, MAX_ERROR)));
    }

}

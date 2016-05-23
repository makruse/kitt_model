package de.zmt.params.def;

import static javax.measure.unit.NonSI.YEAR;
import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.params.def.AgeDistribution;

public class AgeDistributionTest {
    private static final Amount<Duration> MIN = Amount.valueOf(1, YEAR);
    private static final Amount<Duration> MAX = Amount.valueOf(3, YEAR);
    private static final Amount<Duration> INITIAL_PHASE_AGE = Amount.valueOf(1.25, YEAR);
    private static final Amount<Duration> TERMINAL_PHASE_AGE = Amount.valueOf(1.5, YEAR);

    private AgeDistribution distribution;

    @Before
    public void setUp() throws Exception {
	distribution = new AgeDistribution(MIN, MAX, INITIAL_PHASE_AGE, TERMINAL_PHASE_AGE, null);
    }

    @Test
    public void evaluateCdf() {
	double min = 0;
	double initialMin = min + Phase.JUVENILE.getProbability();
	double terminalMin = initialMin + Phase.INITIAL.getProbability();
	double max = terminalMin + Phase.TERMINAL.getProbability();

	assertThat(distribution.evaluateCdf(min), is(amountCloseTo(MIN)));
	assertThat(distribution.evaluateCdf(initialMin), is(amountCloseTo(INITIAL_PHASE_AGE)));
	assertThat(distribution.evaluateCdf(terminalMin),
		is(amountCloseTo(TERMINAL_PHASE_AGE)));
	assertThat(distribution.evaluateCdf(max),
		is(amountCloseTo(MAX)));
    }

}

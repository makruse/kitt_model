package sim.util.distribution;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class PiecewiseLinearTest {
    private PiecewiseLinear distribution;

    @Before
    public void setUp() throws Exception {
	distribution = new PiecewiseLinear(null);
    }

    @Test
    public void evaluateCdfOnUniform() {
	distribution.addInterval(1, 1);
	assertThat(distribution.evaluateCdf(0d), is(0d));
	assertThat(distribution.evaluateCdf(1d), is(1d));
    }

    @Test
    public void evaluateCdfOnSingleInterval() {
	distribution.addInterval(1, 0.5);
	assertThat(distribution.evaluateCdf(0.5d), is(0.25d));
	assertThat(distribution.evaluateCdf(1d), is(0.5d));
    }

    @Test
    public void evaluateCdfOnIntervals() {
	distribution.addInterval(0.25, 0.5);
	distribution.addInterval(0.25, 0.25);
	distribution.addInterval(0.5, 0.25);

	assertThat(distribution.evaluateCdf(0.25d), is(0.5d));
	assertThat(distribution.evaluateCdf(0.5d), is(0.75d));
	assertThat(distribution.evaluateCdf(1d), is(1d));
    }

    @Test
    public void evaluateCdfOnShiftAndScale() {
	double scale = 2;
	double shift = 1;

	distribution.setScale(scale);
	distribution.setShift(shift);

	distribution.addInterval(shift, shift);
	assertThat(distribution.evaluateCdf(0d), is(shift));
	assertThat(distribution.evaluateCdf(0.5d), is(0.5d * scale + shift));
	assertThat(distribution.evaluateCdf(1d), is(1 * scale + shift));
    }
}

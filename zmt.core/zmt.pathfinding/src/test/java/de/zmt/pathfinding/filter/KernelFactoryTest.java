package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class KernelFactoryTest {
    private static final double MAX_ERROR = 1E-14d;
    private static final int NOTRAP_KERNEL_EXTENT = 3;
    private static final int NOTRAP_ORIGIN_EMPHASIS_FACTOR = 5;
    private static final int GAUSSIAN_RADIUS = 1;

    @Test
    public void createNoTrapBlur() {
	Kernel kernel = KernelFactory.createNoTrapBlur(NOTRAP_KERNEL_EXTENT, NOTRAP_KERNEL_EXTENT);
	assertThat(kernel.sum(), is(closeTo(1, MAX_ERROR)));
	assertThat(kernel.getOriginWeight(),
		is(closeTo(kernel.getWeight(0, 0) * NOTRAP_ORIGIN_EMPHASIS_FACTOR, MAX_ERROR)));
    }

    @Test
    public void createGaussianBlur() {
	Kernel kernel = KernelFactory.createGaussianBlur(GAUSSIAN_RADIUS);

	// box weights for matcher
	Collection<Double> weights = new ArrayList<>(kernel.getWeights().length);
	for (double value : kernel.getWeights()) {
	    weights.add(value);
	}

	assertThat(kernel.getWidth(), is(kernel.getHeight()));
	assertThat(kernel.getWidth(), greaterThan(GAUSSIAN_RADIUS * 2));
	// is uneven
	assertThat(kernel.getWidth() % 2, is(1));
	// origin has the highest weight
	assertThat(weights, everyItem(lessThanOrEqualTo(kernel.getOriginWeight())));
    }
}

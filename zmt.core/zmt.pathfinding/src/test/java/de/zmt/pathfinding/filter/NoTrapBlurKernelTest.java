package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class NoTrapBlurKernelTest {
    private static final double MAX_ERROR = 1E-14d;
    private static final int KERNEL_EXTENT = 3;
    private static final int ORIGIN_EMPHASIS_FACTOR = 5;

    private Kernel kernel;

    @Before
    public void setUp() throws Exception {
	kernel = new NoTrapBlurKernel(KERNEL_EXTENT, KERNEL_EXTENT);
    }

    @Test
    public void originWeight() {
	System.out.println(kernel);
	assertThat(kernel.sum(), is(closeTo(1, MAX_ERROR)));
	assertThat(kernel.getOriginWeight(), is(closeTo(kernel.getWeight(0, 0) * ORIGIN_EMPHASIS_FACTOR, MAX_ERROR)));
    }

}

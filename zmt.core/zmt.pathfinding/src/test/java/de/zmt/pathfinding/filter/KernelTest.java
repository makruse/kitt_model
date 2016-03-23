package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class KernelTest {
    private static final int KERNEL_EXTENT = 3;
    private static final double MAX_ERROR = 1E-14d;
    private static final double DOUBLE_SUM = KERNEL_EXTENT * KERNEL_EXTENT * 2d;

    private Kernel kernel;

    @Before
    public void setUp() throws Exception {
	kernel = KernelFactory.createConstant(KERNEL_EXTENT, KERNEL_EXTENT);
    }

    @Test
    public void sum() {
	assertThat(kernel.sum(), is(closeTo(KERNEL_EXTENT * KERNEL_EXTENT, MAX_ERROR)));
    }

    @Test
    public void normalize() {
	assertThat(kernel.normalize().sum(), is(closeTo(1, MAX_ERROR)));
    }

    @Test
    public void add() {
	assertThat(kernel.add(1).sum(), is(DOUBLE_SUM));
    }

    @Test
    public void multiply() {
	assertThat(kernel.multiply(2).sum(), is(DOUBLE_SUM));
    }

    public void equals() {
	assertThat(new Kernel(1, 1, new double[] { 1 }), is(new Kernel(1, 1, new double[] { 1 })));
    }
}

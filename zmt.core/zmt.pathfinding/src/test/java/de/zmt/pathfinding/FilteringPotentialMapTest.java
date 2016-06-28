package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.pathfinding.filter.Kernel;
import sim.field.grid.DoubleGrid2D;

public class FilteringPotentialMapTest {
    private static final int MAP_SIZE = 1;
    private static final double INITIAL_VALUE = 1;
    private static final double KERNEL_FACTOR = 2;
    /** A convolve operation that doubles the source. */
    private static final ConvolveOp DOUBLING_OP = new ConvolveOp(new Kernel(1, 1, new double[] { KERNEL_FACTOR }));
    private static final int VALUE = 2;

    private DoubleGrid2D src;
    private FilteringPotentialMap map;

    @Before
    public void setUp() throws Exception {
        src = new DoubleGrid2D(MAP_SIZE, MAP_SIZE, INITIAL_VALUE);
        map = new FilteringPotentialMap(DOUBLING_OP, src);
    }

    @Test
    public void obtainPotential() {
        double firstResult = INITIAL_VALUE * KERNEL_FACTOR;
        double secondResult = VALUE * KERNEL_FACTOR;

        assertThat(map.obtainPotential(0, 0), is(firstResult));

        src.setTo(VALUE);
        map.markDirty(0, 0);
        assertThat("Value in map should reflect changed source.", map.obtainPotential(0, 0), is(secondResult));
    }
}

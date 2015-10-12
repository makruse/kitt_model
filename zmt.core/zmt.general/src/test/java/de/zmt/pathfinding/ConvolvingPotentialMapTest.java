package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import de.zmt.pathfinding.filter.*;
import sim.field.grid.DoubleGrid2D;

public class ConvolvingPotentialMapTest {
    private static final int MAP_SIZE = 1;
    /** A convolve operation that doubles the source. */
    private static final ConvolveOp DOUBLING_OP = new ConvolveOp(new Kernel(1, 1, new double[] { 2 }));

    private DoubleGrid2D src;
    private ConvolvingPotentialMap map;

    @Before
    public void setUp() throws Exception {
	src = new DoubleGrid2D(MAP_SIZE, MAP_SIZE, 1);
	map = new ConvolvingPotentialMap(DOUBLING_OP, src);
    }

    @Test
    public void obtainPotentialOnManualUpdate() {
	map.setAutoUpdate(false);
	assertThat(map.obtainPotential(0, 0), is(2d));

	src.setTo(2);
	map.markDirty(0, 0);
	assertThat("Value in map should not have changed, update is manual.", map.obtainPotential(0, 0), is(2d));

	map.updateIfDirtyAll();
	assertThat("Value in map should now reflect changed source.", map.obtainPotential(0, 0), is(4d));
    }

    @Test
    public void obtainPotentialOnAutoUpdate() {
	map.setAutoUpdate(true);
	assertThat(map.obtainPotential(0, 0), is(2d));

	src.setTo(2);
	map.markDirty(0, 0);
	assertThat("Value in map should reflect change in src, because of enabled automatic update.",
		map.obtainPotential(0, 0), is(4d));
    }

}

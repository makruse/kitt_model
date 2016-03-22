package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import sim.field.grid.DoubleGrid2D;

public class BasicMorphOpTest {
    /**
     * <pre>
     * 1 0 0
     * 0 0 0
     * 0 0 0
     * </pre>
     */
    private static final double[][] GRID = new double[][] { { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    private static final double[][] RESULT_ERODE = new double[][] { { 1, 1, 0 }, { 1, 1, 0 }, { 0, 0, 0 } };

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void filterDilate() {
	assertThat(BasicMorphOp.getDefaultDilate().filter(new DoubleGrid2D(GRID)).field, is(RESULT_ERODE));
    }

    @Test
    public void filterErode() {
	assertThat(BasicMorphOp.getDefaultErode().filter(new DoubleGrid2D(RESULT_ERODE)).field, is(GRID));
    }

}

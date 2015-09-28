package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import sim.field.grid.DoubleGrid2D;

public class ConvolveOpTest {
    /**
     * <pre>
     * 0 0 0
     * 0 1 0
     * 0 0 0
     * </pre>
     */
    private static final DoubleGrid2D SINGLE_DOT_GRID = new DoubleGrid2D(
	    new double[][] { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } });
    /**
     * <pre>
     * 0 0 0 0 0
     * 0 1 1 1 0
     * 0 1 0 1 0
     * 0 1 1 1 0
     * 0 0 0 0 0
     * </pre>
     */
    private static final DoubleGrid2D ISLAND_GRID = new DoubleGrid2D(new double[][] { { 0, 0, 0, 0, 0 },
	    { 0, 1, 1, 1, 0 }, { 0, 1, 0, 1, 0 }, { 0, 1, 1, 1, 0 }, { 0, 0, 0, 0, 0 } });

    private static final double[] WEIGHTS_LINEAR = new double[] { 1, 1, 1 };
    private static final Kernel KERNEL_HORIZONTAL = new Kernel(3, 1, WEIGHTS_LINEAR);
    private static final Kernel KERNEL_VERTICAL = new Kernel(1, 3, WEIGHTS_LINEAR);

    private static final double ORIGIN_WEIGHT = 5;
    private static final Kernel KERNEL_BOX = new Kernel(3, 3, new double[] { 1, 1, 1, 1, ORIGIN_WEIGHT, 1, 1, 1, 1 });

    private static final double[][] RESULT_HORIZONTAL = new double[][] { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
    private static final double[][] RESULT_VERTICAL = new double[][] { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } };
    private static final double RESULT_ORIGIN = (8 * 1 + ORIGIN_WEIGHT * 0);
    private static final double RESULT_DIAGONAL_ADJACENT = (2 * 1 + ORIGIN_WEIGHT * 1);
    private static final double RESULT_STRAIGHT_ADJACENT = (4 * 1 + ORIGIN_WEIGHT * 1);
    private static final double[][] RESULT_BOX = new double[][] { { 1, 2, 3, 2, 1 },
	    { 2, RESULT_DIAGONAL_ADJACENT, RESULT_STRAIGHT_ADJACENT, RESULT_DIAGONAL_ADJACENT, 2 },
	    { 3, RESULT_STRAIGHT_ADJACENT, RESULT_ORIGIN, RESULT_STRAIGHT_ADJACENT, 3 },
	    { 2, RESULT_DIAGONAL_ADJACENT, RESULT_STRAIGHT_ADJACENT, RESULT_DIAGONAL_ADJACENT, 2 }, { 1, 2, 3, 2, 1 } };

    @Test
    public void filterHorizontal() {
	assertThat(new ConvolveOp(KERNEL_HORIZONTAL).filter(SINGLE_DOT_GRID, null).field, is(RESULT_HORIZONTAL));

    }

    @Test
    public void filterVertical() {
	assertThat(new ConvolveOp(KERNEL_VERTICAL).filter(SINGLE_DOT_GRID, null).field, is(RESULT_VERTICAL));
    }

    @Test
    public void filterBox() {
	assertThat(new ConvolveOp(KERNEL_BOX).filter(ISLAND_GRID, null).field, is(equalTo(RESULT_BOX)));
    }
}

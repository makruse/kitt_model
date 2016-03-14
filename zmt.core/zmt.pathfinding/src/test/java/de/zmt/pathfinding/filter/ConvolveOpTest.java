package de.zmt.pathfinding.filter;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.zmt.pathfinding.EdgeHandler;
import sim.field.grid.BooleanGrid;
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

    private static final DoubleGrid2D SINGULAR_GRID = new DoubleGrid2D(new double[][] { { 1 } });

    private static final double[] WEIGHTS_LINEAR = new double[] { 1, 1, 1 };
    private static final Kernel KERNEL_HORIZONTAL = new Kernel(3, 1, WEIGHTS_LINEAR);
    private static final Kernel KERNEL_VERTICAL = new Kernel(1, 3, WEIGHTS_LINEAR);

    private static final double ORIGIN_WEIGHT = 5;
    private static final Kernel KERNEL_BOX = new Kernel(3, 3, new double[] { 1, 1, 1, 1, ORIGIN_WEIGHT, 1, 1, 1, 1 });
    private static final Kernel KERNEL_CONSTANT = new ConstantKernel(3, 3);

    /**
     * Columns 2 and 4 are not filtered and copied from source.
     * 
     * <pre>
     * 1 0 1 0 1
     * 1 0 1 0 1
     * 1 0 1 0 1
     * 1 0 1 0 1
     * 1 0 1 0 1
     * </pre>
     */
    private static final boolean[][] INCLUDES_ISLAND_GRID = new boolean[][] { { true, true, true, true, true },
	    { false, false, false, false, false }, { true, true, true, true, true },
	    { false, false, false, false, false }, { true, true, true, true, true } };

    private static final double[][] RESULT_HORIZONTAL = new double[][] { { 0, 1, 0 }, { 0, 1, 0 }, { 0, 1, 0 } };
    private static final double[][] RESULT_VERTICAL = new double[][] { { 0, 0, 0 }, { 1, 1, 1 }, { 0, 0, 0 } };
    private static final double RESULT_ORIGIN = (8 * 1 + ORIGIN_WEIGHT * 0);
    private static final double RESULT_DIAGONAL_ADJACENT = (2 * 1 + ORIGIN_WEIGHT * 1);
    private static final double RESULT_STRAIGHT_ADJACENT = (4 * 1 + ORIGIN_WEIGHT * 1);
    private static final double[][] RESULT_BOX = new double[][] { { 1, 2, 3, 2, 1 },
	    { 2, RESULT_DIAGONAL_ADJACENT, RESULT_STRAIGHT_ADJACENT, RESULT_DIAGONAL_ADJACENT, 2 },
	    { 3, RESULT_STRAIGHT_ADJACENT, RESULT_ORIGIN, RESULT_STRAIGHT_ADJACENT, 3 },
	    { 2, RESULT_DIAGONAL_ADJACENT, RESULT_STRAIGHT_ADJACENT, RESULT_DIAGONAL_ADJACENT, 2 }, { 1, 2, 3, 2, 1 } };
    /**
     * Every second column is left untouched and copied from source (
     * {@link #ISLAND_GRID}).
     */
    private static final double[][] RESULT_WITH_INCLUDES_BOX = new double[][] { { 1, 2, 3, 2, 1 }, { 0, 1, 1, 1, 0 },
	    { 3, RESULT_STRAIGHT_ADJACENT, RESULT_ORIGIN, RESULT_STRAIGHT_ADJACENT, 3 }, { 0, 1, 1, 1, 0 },
	    { 1, 2, 3, 2, 1 } };

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

    @Test
    public void filterBoxWithIncludes() {
	assertThat(new ConvolveOp(KERNEL_BOX).filter(ISLAND_GRID, null, new BooleanGrid(INCLUDES_ISLAND_GRID)).field,
		is(equalTo(RESULT_WITH_INCLUDES_BOX)));
    }

    @Test
    public void filterConstantOnCustomValue() {
	// no change in constant kernel with average filter
	assertThat(new ConvolveOp(KERNEL_CONSTANT, new EdgeHandler(-1)).filter(SINGULAR_GRID).get(0, 0),
		is(closeTo(-8 + 1, 1E-15d)));
    }
}

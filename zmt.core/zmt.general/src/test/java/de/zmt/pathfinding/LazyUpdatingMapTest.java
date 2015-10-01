package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import de.zmt.pathfinding.LazyUpdatingMap;
import de.zmt.sim.field.grid.BooleanGrid;

public class LazyUpdatingMapTest {
    private static final int MAP_SIZE = 3;
    /**
     * <pre>
     * 1 0 0
     * 0 0 0
     * 0 0 0
     * </pre>
     */
    private static final boolean[][] REFRESHED_RESULT_ZERO = new boolean[][] { { true, false, false },
	    { false, false, false }, { false, false, false } };
    /**
     * <pre>
     * 0 0 0
     * 0 1 1
     * 0 1 1
     * </pre>
     */
    private static final boolean[][] REFRESHED_RESULT_ONE = new boolean[][] { { false, false, false },
	    { false, true, true }, { false, true, true } };

    @Test
    public void markDirtyOnZeroExtend() {
	TestLazyUpdatingMap map = new TestLazyUpdatingMap(MAP_SIZE, MAP_SIZE, 0, 0);
	map.markDirty(0, 0);
	map.refreshIfDirtyAll();
	assertThat(map.refreshed.getField(), is(equalTo(REFRESHED_RESULT_ZERO)));
    }

    @Test
    public void markDirtyOnOneExtend() {
	TestLazyUpdatingMap map = new TestLazyUpdatingMap(MAP_SIZE, MAP_SIZE, 1, 1);
	map.markDirty(MAP_SIZE - 1, MAP_SIZE - 1);
	map.refreshIfDirtyAll();
	assertThat(map.refreshed.getField(), is(equalTo(REFRESHED_RESULT_ONE)));
    }

    private static class TestLazyUpdatingMap extends LazyUpdatingMap {
	final BooleanGrid refreshed;

	public TestLazyUpdatingMap(int width, int height, int xExtend, int yExtend) {
	    super(width, height, xExtend, yExtend);
	    this.refreshed = new BooleanGrid(width, height);
	}

	@Override
	protected void refresh(int x, int y) {
	    refreshed.set(x, y, true);
	}

    }
}

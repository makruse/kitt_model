package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;

import sim.field.grid.BooleanGrid;

public class LazyUpdatingMapTest {
    private static final int MAP_SIZE = 3;

    /**
     * <pre>
     * 0 0 0
     * 0 0 0
     * 0 0 0
     * </pre>
     */
    private static final boolean[][] NOT_UPDATED_RESULT = new boolean[][] { { false, false, false },
	    { false, false, false }, { false, false, false } };

    /**
     * <pre>
     * 1 0 0
     * 0 0 0
     * 0 0 0
     * </pre>
     */
    private static final boolean[][] UPDATED_RESULT_ZERO = new boolean[][] { { true, false, false },
	    { false, false, false }, { false, false, false } };
    /**
     * <pre>
     * 0 0 0
     * 0 1 1
     * 0 1 1
     * </pre>
     */
    private static final boolean[][] UPDATED_RESULT_ONE = new boolean[][] { { false, false, false },
	    { false, true, true }, { false, true, true } };

    @Test
    public void markDirtyOnZeroExtend() {
	TestLazyUpdatingMap map = new TestLazyUpdatingMap(MAP_SIZE, MAP_SIZE, 0, 0);
	map.markDirty(0, 0);
	assertThat(map.updated.getField(), is(equalTo(NOT_UPDATED_RESULT)));
	map.updateIfDirtyAll();
	assertThat(map.updated.getField(), is(equalTo(UPDATED_RESULT_ZERO)));
    }

    @Test
    public void markDirtyOnOneExtend() {
	TestLazyUpdatingMap map = new TestLazyUpdatingMap(MAP_SIZE, MAP_SIZE, 1, 1);
	// lower right corner
	map.markDirty(MAP_SIZE - 1, MAP_SIZE - 1);
	assertThat(map.updated.getField(), is(equalTo(NOT_UPDATED_RESULT)));
	map.updateIfDirtyAll();
	assertThat(map.updated.getField(), is(equalTo(UPDATED_RESULT_ONE)));
    }

    @Test
    public void notifyListeners() {
	LazyUpdatingMap map = new LazyUpdatingMap(1, 1) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected void update(int x, int y) {
	    }
	};

	MapChangeListener listener = mock(MapChangeListener.class);
	map.addListener(listener);
	map.forceUpdateAll();
	verify(listener).changed(0, 0);
    }

    private static class TestLazyUpdatingMap extends LazyUpdatingMap {
	private static final long serialVersionUID = 1L;

	final BooleanGrid updated;

	public TestLazyUpdatingMap(int width, int height, int xExtend, int yExtend) {
	    super(width, height, xExtend, yExtend);
	    this.updated = new BooleanGrid(width, height);
	}

	@Override
	protected void update(int x, int y) {
	    updated.set(x, y, true);
	}

    }
}

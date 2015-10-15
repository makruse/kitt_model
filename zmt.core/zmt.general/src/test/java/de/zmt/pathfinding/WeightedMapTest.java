package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import org.junit.*;

public class WeightedMapTest {

    private PathfindingMap map1;
    private PathfindingMap map2;

    @Before
    public void setUp() throws Exception {
	map1 = new SimplePathfindingMap();
	map2 = new SimplePathfindingMap();
    }

    @Test
    public void equalsOnWrapped() {
	PathfindingMap wrappedMap1 = new WeightedMap<PathfindingMap>(map1, 0);
	assertThat(wrappedMap1, is(equalTo(map1)));
	assertThat(wrappedMap1, is(not(equalTo(map2))));
    }

    @Test
    public void equalsOnDifferentWeight() {
	PathfindingMap map1Wrapped1 = new WeightedMap<PathfindingMap>(map1, 1);
	PathfindingMap map1Wrapped2 = new WeightedMap<PathfindingMap>(map1, 2);
	assertThat(map1Wrapped1, is(equalTo(map1Wrapped2)));
    }

    private static class SimplePathfindingMap implements PathfindingMap {
	private static final int SIZE = 1;

	@Override
	public int getWidth() {
	    return SIZE;
	}

	@Override
	public int getHeight() {
	    return SIZE;
	}
    }
}

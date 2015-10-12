package de.zmt.pathfinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class EqualDimensionsMapsTest {
    private static final int MAP_SIZE = 1;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;

    private Collection<PathfindingMap> maps;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	maps = new EqualDimensionsMaps<>(new ArrayList<PathfindingMap>(), MAP_SIZE,
		MAP_SIZE);
    }

    @Test
    public void addOnValid() {
	PathfindingMap map = new TestPathfindingMap(MAP_SIZE, MAP_SIZE);
	maps.add(map);
	assertThat(maps.contains(map), is(true));
    }

    @Test
    public void addOnInvalid() {
        thrown.expect(IllegalArgumentException.class);
	maps.add(new TestPathfindingMap(INVALID_MAP_SIZE, INVALID_MAP_SIZE));
    }

    private class TestPathfindingMap implements PathfindingMap {
	private final int width;
	private final int height;

	public TestPathfindingMap(int width, int height) {
	    super();
	    this.width = width;
	    this.height = height;
	}
	@Override
	public int getWidth() {
	    return width;
	}
	@Override
	public int getHeight() {
	    return height;
	}
    }
}

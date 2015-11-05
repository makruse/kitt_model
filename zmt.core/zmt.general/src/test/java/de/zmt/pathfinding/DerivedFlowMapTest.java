package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

import sim.util.Double2D;

public class DerivedFlowMapTest {
    private static final int MAP_SIZE = 1;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;

    private static final MyConstantPathfindingMap PATHFINDING_MAP = new MyConstantPathfindingMap();
    private static final double WEIGHT_VALUE = 2;

    private MyDerivedFlowMap map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	map = new MyDerivedFlowMap();
    }

    @Test
    public void addAndRemove() {
	assertThat(map.getIntegralMaps(), is(empty()));

	assertThat(map.addMap(PATHFINDING_MAP), is(true));
	assertThat(map.getIntegralMaps(), contains((PathfindingMap) PATHFINDING_MAP));
	assertTrue(map.wasComputeDirectionCalled());

	assertThat(map.removeMap(PATHFINDING_MAP), is(true));
	assertThat(map.getIntegralMaps(), is(empty()));
	assertTrue(map.wasComputeDirectionCalled());
    }

    @Test
    public void addOnInvalid() {
	thrown.expect(IllegalArgumentException.class);
	map.addMap(new FlowMap() {

	    @Override
	    public int getWidth() {
		return INVALID_MAP_SIZE;
	    }

	    @Override
	    public int getHeight() {
		return INVALID_MAP_SIZE;
	    }

	    @Override
	    public Double2D obtainDirection(int x, int y) {
		return null;
	    }
	});
    }

    @Test
    public void updateOnDynamic() {
	MyDynamicMap dynamicMap = new MyDynamicMap();
	map.addMap(dynamicMap);
	assertTrue(map.wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	assertTrue(map.isDirty(0, 0));
	map.obtainDirection(0, 0);
	assertTrue(map.wasComputeDirectionCalled());
	assertFalse(map.isDirty(0, 0));
    }

    @Test
    public void updateOnInnerMap() {
	MyDynamicMap dynamicMap = new MyDynamicMap();
	MyDerivedFlowMap innerMap = new MyDerivedFlowMap();

	map.addMap(innerMap);
	// forced update from adding map
	assertTrue(map.wasComputeDirectionCalled());

	innerMap.addMap(dynamicMap);
	// marked dirty from inner map's update
	assertTrue(map.isDirty(0, 0));
	map.updateIfDirty(0, 0);
	assertTrue(map.wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	// inner map is marked dirty from dynamic map's change
	assertTrue(innerMap.isDirty(0, 0));
	// inner map's update is propagated to outer map
	map.updateIfDirty(0, 0);
	assertTrue(map.wasComputeDirectionCalled());
    }

    @Test
    public void addAndRemoveMapWithWeight() {
	assertThat(map.addMap(PATHFINDING_MAP, WEIGHT_VALUE), is(true));
	assertThat(map.getIntegralMaps(), contains((PathfindingMap) PATHFINDING_MAP));
	assertThat(map.obtainWeight(PATHFINDING_MAP), is(WEIGHT_VALUE));

	assertThat(map.removeMap(PATHFINDING_MAP), is(true));
	assertThat(map.getIntegralMaps(), is(empty()));
    }

    @Test
    public void setWeight() {
	map.addMap(PATHFINDING_MAP);
	assertThat(map.obtainWeight(PATHFINDING_MAP), is(DerivedFlowMap.NEUTRAL_WEIGHT));
	assertTrue(map.wasComputeDirectionCalled());

	map.setWeight(PATHFINDING_MAP, WEIGHT_VALUE);
	assertThat(map.obtainWeight(PATHFINDING_MAP), is(WEIGHT_VALUE));
	assertTrue(map.wasComputeDirectionCalled());
    }

    private static class MyDerivedFlowMap extends DerivedFlowMap<PathfindingMap> {
	private static final long serialVersionUID = 1L;

	private boolean computeDirectionCalled = false;

	public MyDerivedFlowMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}

	@Override
	protected Double2D computeDirection(int x, int y) {
	    computeDirectionCalled = true;
	    return DIRECTION_NEUTRAL;
	}

	public boolean wasComputeDirectionCalled() {
	    boolean called = computeDirectionCalled;
	    computeDirectionCalled = false;
	    return called;
	}
    }

    private static class MyDynamicMap extends BasicMapChangeNotifier implements PathfindingMap {
	private static final long serialVersionUID = 1L;

	@Override
	public int getWidth() {
	    return MAP_SIZE;
	}

	@Override
	public int getHeight() {
	    return MAP_SIZE;
	}

    }

    private static class MyConstantPathfindingMap extends ConstantPathfindingMap {

	public MyConstantPathfindingMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}
    }

}

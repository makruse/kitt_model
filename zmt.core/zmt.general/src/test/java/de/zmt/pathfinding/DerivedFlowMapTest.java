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

    private static final MyMap PATHFINDING_MAP = new MyMap();
    private static final double WEIGHT_VALUE = 2;

    private boolean computeDirectionCalled;
    private DerivedFlowMap<PathfindingMap> map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	computeDirectionCalled = false;
	map = new DerivedFlowMap<PathfindingMap>(MAP_SIZE, MAP_SIZE) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected Double2D computeDirection(int x, int y) {
		computeDirectionCalled = true;
		return DIRECTION_NEUTRAL;
	    }
	};
    }

    @Test
    public void addAndRemove() {
	assertThat(map.getIntegralMaps(), is(empty()));

	assertThat(map.addMap(PATHFINDING_MAP), is(true));
	assertThat(map.getIntegralMaps(), contains((PathfindingMap) PATHFINDING_MAP));
	assertTrue(wasComputeDirectionCalled());

	assertThat(map.removeMap(PATHFINDING_MAP), is(true));
	assertThat(map.getIntegralMaps(), is(empty()));
	assertTrue(wasComputeDirectionCalled());
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
	assertTrue(wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	map.obtainDirection(0, 0);
	assertTrue(wasComputeDirectionCalled());
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
	assertTrue(wasComputeDirectionCalled());

	map.setWeight(PATHFINDING_MAP, WEIGHT_VALUE);
	assertThat(map.obtainWeight(PATHFINDING_MAP), is(WEIGHT_VALUE));
	assertTrue(wasComputeDirectionCalled());
    }

    private boolean wasComputeDirectionCalled() {
	boolean called = computeDirectionCalled;
	computeDirectionCalled = false;
	return called;
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

    private static class MyMap extends ConstantPathfindingMap {

	public MyMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}
    }

}

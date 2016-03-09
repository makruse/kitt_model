package de.zmt.pathfinding;

import static de.zmt.pathfinding.DerivedFlowMap.NEUTRAL_WEIGHT;
import static de.zmt.util.DirectionUtil.NEUTRAL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import sim.util.Double2D;

public class DerivedFlowMapTest {
    private static final int MAP_SIZE = 1;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;
    private static final double WEIGHT_VALUE = 2;

    private MyDynamicMap dynamicMap;
    private MyDerivedFlowMap map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	dynamicMap = new MyDynamicMap();
	map = new MyDerivedFlowMap();
    }

    @Test
    public void addAndRemove() {
        assertThat(map.getUnderlyingMaps(), is(empty()));
    
	map.addMap(dynamicMap);
	assertThat(map.getUnderlyingMaps(), contains((PathfindingMap) dynamicMap));
	assertTrue(map.wasComputeDirectionCalled());
    
	assertThat(map.removeMap(dynamicMap), is(true));
        assertThat(map.getUnderlyingMaps(), is(empty()));
	assertTrue(map.wasComputeDirectionCalled());
    }

    @Test
    public void addOnInvalid() {
        thrown.expect(IllegalArgumentException.class);
	map.addMap(new PathfindingMap() {
    
            @Override
            public int getWidth() {
        	return INVALID_MAP_SIZE;
            }
    
            @Override
            public int getHeight() {
        	return INVALID_MAP_SIZE;
            }
        });
    }

    @Test
    public void updateOnDynamic() {
	map.addMap(dynamicMap);
	assertTrue(map.wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	assertTrue(map.isDirty(0, 0));
	map.obtainDirection(0, 0);

	assertTrue(map.wasComputeDirectionCalled());
	assertFalse(map.isDirty(0, 0));
    }

    /**
     * Test updating if a {@code DerivedFlowMap} is added to another
     * {@code DerivedFlowMap}.
     */
    @Test
    public void updateOnInnerMap() {
	MyDerivedFlowMap outerMap = new MyDerivedFlowMap();
	map.addMap(dynamicMap);
	outerMap.addMap(map);
	// called from forced update from adding map
	assertTrue(outerMap.wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	// map is marked dirty from dynamic map's change
	assertTrue(map.isDirty(0, 0));
	// map's update is propagated to outer map
	outerMap.updateIfDirty(0, 0);
	assertTrue(outerMap.wasComputeDirectionCalled());
    }

    @Test
    public void addAndRemoveMapWithWeight() {
	map.addMap(dynamicMap, WEIGHT_VALUE);
	assertThat(map.getUnderlyingMaps(), contains((PathfindingMap) dynamicMap));
	assertThat(map.getWeight(dynamicMap), is(WEIGHT_VALUE));
	assertTrue(map.wasComputeDirectionCalled());

	assertThat(map.removeMap(dynamicMap), is(true));
	assertThat(map.getUnderlyingMaps(), is(empty()));
	assertTrue(map.wasComputeDirectionCalled());
    }

    @Test
    public void setWeight() {
	map.addMap(dynamicMap);
	assertThat(map.getWeight(dynamicMap), is(NEUTRAL_WEIGHT));
	assertTrue(map.wasComputeDirectionCalled());

	map.setWeight(dynamicMap, WEIGHT_VALUE);
	assertThat(map.getWeight(dynamicMap), is(WEIGHT_VALUE));
	assertTrue(map.wasComputeDirectionCalled());
    }

    private static class MyDerivedFlowMap extends DerivedFlowMap<PathfindingMap> {
	private static final long serialVersionUID = 1L;

	private boolean computeDirectionCalled;

	public MyDerivedFlowMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}

	@Override
	protected Double2D computeDirection(int x, int y) {
	    computeDirectionCalled = true;
	    return NEUTRAL;
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

}

package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_NEUTRAL;
import static org.junit.Assert.*;

import org.junit.*;

import sim.util.Double2D;

public class DerivedFlowMapTest {
    private static final int MAP_SIZE = 1;

    private MyDynamicMap dynamicMap;
    private MyDerivedFlowMap map;

    @Before
    public void setUp() throws Exception {
	dynamicMap = new MyDynamicMap();
	map = new MyDerivedFlowMap(dynamicMap);
    }

    @Test
    public void updateOnDynamic() {
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
	MyDerivedFlowMap outerMap = new MyDerivedFlowMap(map);
	// forced update from adding map
	assertTrue(map.wasComputeDirectionCalled());

	dynamicMap.notifyListeners(0, 0);
	// map is marked dirty from dynamic map's change
	assertTrue(map.isDirty(0, 0));
	// map's update is propagated to outer map
	outerMap.updateIfDirty(0, 0);
	assertTrue(outerMap.wasComputeDirectionCalled());
    }

    private static class MyDerivedFlowMap extends DerivedFlowMap<PathfindingMap> {
	private static final long serialVersionUID = 1L;

	private boolean computeDirectionCalled;

	public MyDerivedFlowMap(PathfindingMap integralMap) {
	    super(integralMap);
	    forceUpdateAll();
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

}

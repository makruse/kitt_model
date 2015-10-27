package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromWeightedMapTest {
    private static final double WEIGHT_VALUE = 2;
    private static final int MAP_SIZE = 1;
    private static final PathfindingMap PATHFINDING_MAP = new MyPathfindingMap();

    private TestFlowFromWeightedMap map;

    @Before
    public void setUp() throws Exception {
	map = new TestFlowFromWeightedMap();
    }

    @Test
    public void addAndRemoveMapWithWeight() {
	map.addMap(PATHFINDING_MAP, WEIGHT_VALUE);
	assertThat(map.getIntegralMaps().isEmpty(), is(false));

	PathfindingMap integralMap = map.getIntegralMaps().iterator().next();
	assertThat(map.obtainWeight(integralMap), is(WEIGHT_VALUE));

	map.removeMap(PATHFINDING_MAP);
	assertThat(map.getIntegralMaps().isEmpty(), is(true));
    }

    @Test
    public void addAndRemoveMapWithoutExplicitWeight() {
	map.addMap(PATHFINDING_MAP);
	assertThat(map.getIntegralMaps().isEmpty(), is(false));

	PathfindingMap integralMap = map.getIntegralMaps().iterator().next();
	assertThat(map.obtainWeight(integralMap), is(FlowFromWeightedMap.NEUTRAL_WEIGHT));

	map.removeMap(PATHFINDING_MAP);
	assertThat(map.getIntegralMaps().isEmpty(), is(true));
    }

    @Test
    public void setWeight() {
	map.addMap(PATHFINDING_MAP);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));

	map.direction = DIRECTION_DOWN;
	map.setWeight(PATHFINDING_MAP, WEIGHT_VALUE);
	assertThat(map.obtainWeight(PATHFINDING_MAP), is(WEIGHT_VALUE));
	// verify that map was updated
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
    }

    private static class TestFlowFromWeightedMap extends FlowFromWeightedMap<PathfindingMap> {
	private static final long serialVersionUID = 1L;

	private Double2D direction = DIRECTION_NEUTRAL;

	public TestFlowFromWeightedMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}

	@Override
	protected Double2D computeDirection(int x, int y) {
	    return direction;
	}

    }

    private static class MyPathfindingMap extends ConstantPathfindingMap {

	public MyPathfindingMap() {
	    super(MAP_SIZE, MAP_SIZE);
	}

    }
}

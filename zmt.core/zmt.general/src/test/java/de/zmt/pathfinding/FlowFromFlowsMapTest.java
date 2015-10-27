package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class FlowFromFlowsMapTest {
    private static final int MAP_SIZE = 1;
    private static final MyFlowMap FLOW_MAP_DOWN = new MyFlowMap(DIRECTION_DOWN);
    private static final MyFlowMap FLOW_MAP_RIGHT = new MyFlowMap(DIRECTION_RIGHT);
    private static final MyFlowMap FLOW_MAP_LEFT = new MyFlowMap(DIRECTION_LEFT);

    private FlowFromFlowsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromFlowsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	map.addMap(FLOW_MAP_DOWN);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(FLOW_MAP_DOWN, 2);
	assertThat("Weight should not alter result when there is only a single map added.", map.obtainDirection(0, 0),
		is(DIRECTION_DOWN));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(FLOW_MAP_DOWN);
	map.addMap(FLOW_MAP_RIGHT);
	Double2D downRight = DIRECTION_DOWN.add(DIRECTION_RIGHT).normalize();
	assertThat(map.obtainDirection(0, 0), is(downRight));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(FLOW_MAP_RIGHT, 2);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_RIGHT));
	map.addMap(FLOW_MAP_LEFT, 1.5);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_RIGHT));

	map.setWeight(FLOW_MAP_LEFT, 3);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_LEFT));
    }

    private static class MyFlowMap extends ConstantFlowMap implements FlowMap {
	public MyFlowMap(Double2D value) {
	    super(MAP_SIZE, MAP_SIZE, value);
	}
    }
}

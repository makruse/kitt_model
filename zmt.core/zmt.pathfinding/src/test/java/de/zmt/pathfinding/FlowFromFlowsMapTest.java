package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import sim.util.Double2D;

public class FlowFromFlowsMapTest {
    private static final int MAP_SIZE = 1;
    private static final MyFlowMap FLOW_MAP_SOUTH = new MyFlowMap(SOUTH);
    private static final MyFlowMap FLOW_MAP_EAST = new MyFlowMap(EAST);
    private static final MyFlowMap FLOW_MAP_WEST = new MyFlowMap(WEST);

    private FlowFromFlowsMap map;

    @Before
    public void setUp() throws Exception {
	map = new FlowFromFlowsMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
        assertThat(map.obtainDirection(0, 0), is(NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
        map.addMap(FLOW_MAP_SOUTH);
        assertThat(map.obtainDirection(0, 0), is(SOUTH));
    }

    @Test
    public void obtainDirectionOnMulti() {
        map.addMap(FLOW_MAP_SOUTH);
        map.addMap(FLOW_MAP_EAST);
        assertThat(map.obtainDirection(0, 0), is(SOUTHEAST));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(FLOW_MAP_SOUTH, 2);
	assertThat("Weight should not alter result when there is only a single map added.", map.obtainDirection(0, 0),
		is(SOUTH));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(FLOW_MAP_EAST, 2);
	assertThat(map.obtainDirection(0, 0), is(EAST));
	map.addMap(FLOW_MAP_WEST, 1.5);
	assertThat(map.obtainDirection(0, 0), is(EAST));

	map.setWeight(FLOW_MAP_WEST, 3);
	assertThat(map.obtainDirection(0, 0), is(WEST));
    }

    private static class MyFlowMap extends TestConstantFlowMap {
	public MyFlowMap(Double2D value) {
	    super(MAP_SIZE, MAP_SIZE, value);
	}
    }
}

package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.*;
import org.junit.rules.ExpectedException;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

public class CombinedFlowMapTest {
    private static final int MAP_SIZE = 1;
    private static final MyFlowMap FLOW_MAP_SOUTH = new MyFlowMap(DIRECTION_SOUTH);
    private static final MyFlowMap FLOW_MAP_EAST = new MyFlowMap(DIRECTION_EAST);
    private static final MyFlowMap FLOW_MAP_WEST = new MyFlowMap(DIRECTION_WEST);

    private static final int INVALID_MAP_SIZE = -MAP_SIZE;
    private static final double WEIGHT_VALUE = 2;

    private CombinedFlowMap map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	map = new CombinedFlowMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnEmpty() {
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));
    }

    @Test
    public void obtainDirectionOnSingle() {
	map.addMap(FLOW_MAP_SOUTH);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_SOUTH));
    }

    @Test
    public void obtainDirectionOnSingleWithWeight() {
	map.addMap(FLOW_MAP_SOUTH, 2);
	assertThat("Weight should not alter result when there is only a single map added.", map.obtainDirection(0, 0),
		is(DIRECTION_SOUTH));
    }

    @Test
    public void obtainDirectionOnMulti() {
	map.addMap(FLOW_MAP_SOUTH);
	map.addMap(FLOW_MAP_EAST);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_SOUTHEAST));
    }

    @Test
    public void obtainDirectionOnMultiWithWeight() {
	map.addMap(FLOW_MAP_EAST, 2);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_EAST));
	map.addMap(FLOW_MAP_WEST, 1.5);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_EAST));

	map.setWeight(FLOW_MAP_WEST, 3);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_WEST));
    }

    @Test
    public void addAndRemove() {
	assertThat(map.getUnderlyingMaps(), is(empty()));

	assertThat(map.addMap(FLOW_MAP_SOUTH), is(true));
	assertThat(map.getUnderlyingMaps(), contains((FlowMap) FLOW_MAP_SOUTH));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_SOUTH));

	assertThat(map.removeMap(FLOW_MAP_SOUTH), is(true));
	assertThat(map.getUnderlyingMaps(), is(empty()));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));
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

	    @Override
	    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
		return null;
	    }
	});
    }

    @Test
    public void addAndRemoveMapWithWeight() {
	assertThat(map.addMap(FLOW_MAP_SOUTH, WEIGHT_VALUE), is(true));
	assertThat(map.getUnderlyingMaps(), contains((FlowMap) FLOW_MAP_SOUTH));
	assertThat(map.obtainWeight(FLOW_MAP_SOUTH), is(WEIGHT_VALUE));

	assertThat(map.removeMap(FLOW_MAP_SOUTH), is(true));
	assertThat(map.getUnderlyingMaps(), is(empty()));
    }

    @Test
    public void setWeight() {
	map.addMap(FLOW_MAP_WEST);
	map.addMap(FLOW_MAP_EAST);
	assertThat(map.obtainWeight(FLOW_MAP_WEST), is(CombinedFlowMap.NEUTRAL_WEIGHT));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));

	map.setWeight(FLOW_MAP_WEST, WEIGHT_VALUE);
	assertThat(map.obtainWeight(FLOW_MAP_WEST), is(WEIGHT_VALUE));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_WEST));
    }

    private static class MyFlowMap extends TestConstantFlowMap implements FlowMap {
	public MyFlowMap(Double2D value) {
	    super(MAP_SIZE, MAP_SIZE, value);
	}
    }
}

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
    private static final MyFlowMap FLOW_MAP_DOWN = new MyFlowMap(DIRECTION_DOWN);
    private static final MyFlowMap FLOW_MAP_RIGHT = new MyFlowMap(DIRECTION_RIGHT);
    private static final MyFlowMap FLOW_MAP_LEFT = new MyFlowMap(DIRECTION_LEFT);

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

    @Test
    public void addAndRemove() {
	assertThat(map.getIntegralMaps(), is(empty()));

	assertThat(map.addMap(FLOW_MAP_DOWN), is(true));
	assertThat(map.getIntegralMaps(), contains((FlowMap) FLOW_MAP_DOWN));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));

	assertThat(map.removeMap(FLOW_MAP_DOWN), is(true));
	assertThat(map.getIntegralMaps(), is(empty()));
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
	assertThat(map.addMap(FLOW_MAP_DOWN, WEIGHT_VALUE), is(true));
	assertThat(map.getIntegralMaps(), contains((FlowMap) FLOW_MAP_DOWN));
	assertThat(map.obtainWeight(FLOW_MAP_DOWN), is(WEIGHT_VALUE));

	assertThat(map.removeMap(FLOW_MAP_DOWN), is(true));
	assertThat(map.getIntegralMaps(), is(empty()));
    }

    @Test
    public void setWeight() {
	map.addMap(FLOW_MAP_LEFT);
	map.addMap(FLOW_MAP_RIGHT);
	assertThat(map.obtainWeight(FLOW_MAP_LEFT), is(CombinedFlowMap.NEUTRAL_WEIGHT));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_NEUTRAL));

	map.setWeight(FLOW_MAP_LEFT, WEIGHT_VALUE);
	assertThat(map.obtainWeight(FLOW_MAP_LEFT), is(WEIGHT_VALUE));
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_LEFT));
    }

    private static class MyFlowMap extends TestConstantFlowMap implements FlowMap {
	public MyFlowMap(Double2D value) {
	    super(MAP_SIZE, MAP_SIZE, value);
	}
    }
}

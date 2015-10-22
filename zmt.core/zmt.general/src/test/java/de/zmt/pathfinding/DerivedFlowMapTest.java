package de.zmt.pathfinding;

import static de.zmt.pathfinding.DirectionConstants.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;
import org.junit.rules.ExpectedException;

import sim.util.Double2D;

public class DerivedFlowMapTest {
    private static final int MAP_SIZE = 1;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;

    private DerivedFlowMap<FlowMap> map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	map = new SimpleDerivedFlowMap(MAP_SIZE, MAP_SIZE);
    }

    @Test
    public void obtainDirectionOnDynamic() {
	DynamicFlowMap dynamicMap = new DynamicFlowMap(DIRECTION_DOWN, DIRECTION_UP);
	map.addMap(dynamicMap);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
	dynamicMap.nextIteration();
	assertThat("Change of dynamic map was not correctly propagated.", map.obtainDirection(0, 0), is(DIRECTION_UP));
    }

    @Test
    public void obtainDirectionOnAddAndRemove() {
	FlowMap flowMap = new DynamicFlowMap(DIRECTION_DOWN);
	map.addMap(flowMap);
	assertThat(map.obtainDirection(0, 0), is(DIRECTION_DOWN));
	map.removeMap(flowMap);
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
	});
    }

    private static class SimpleDerivedFlowMap extends DerivedFlowMap<FlowMap> {
	private static final long serialVersionUID = 1L;

	public SimpleDerivedFlowMap(int width, int height) {
	    super(width, height);
	}

	/**
	 * Returns location of first integral map or the neutral direction if
	 * maps are empty.
	 */
	@Override
	protected Double2D computeDirection(int x, int y) {
	    for (FlowMap map : getIntegralMaps()) {
		return map.obtainDirection(x, y);
	    }
	    return DIRECTION_NEUTRAL;
	}
    }

    private static class DynamicFlowMap extends DynamicPathfindingMap<Double2D>implements FlowMap {
	private static final long serialVersionUID = 1L;

	public DynamicFlowMap(Double2D... iterations) {
	    super(MAP_SIZE, MAP_SIZE, iterations);
	}

	@Override
	public Double2D obtainDirection(int x, int y) {
	    return mapIterations.peek();
	}
    }

}

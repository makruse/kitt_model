package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.*;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

import sim.util.Double2D;

public class CombinedFlowMapTest {
    private static final int MAP_SIZE = 3;
    private static final int MAP_CENTER = (MAP_SIZE - 1) >> 1;
    /**
     * <pre>
     * 0 0 0
     * 0 0 0
     * 0 1 0
     * </pre>
     */
    private static final PotentialMap POTENTIAL_MAP_DOWN = new SimplePotentialMap(
	    new double[][] { { 0, 0, 0 }, { 0, 0, 1 }, { 0, 0, 0 } });
    /**
     * <pre>
     * 0 0 0
     * 0 0 1
     * 0 0 0
     * </pre>
     */
    private static final PotentialMap POTENTIAL_MAP_RIGHT = new SimplePotentialMap(
	    new double[][] { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 1, 0 } });
    private static final FlowMap FLOW_MAP_UP = new MyFlowMap(DIRECTION_UP);

    private CombinedFlowMap map;

    @Before
    public void setUp() throws Exception {
	map = new CombinedFlowMap(MAP_SIZE, MAP_SIZE);
    }

    /** Tests setting weight to the internal {@code FlowFromPotentialsMap}. */
    @Test
    public void addAndRemoveMapWithWeights() {
	map.addMap(POTENTIAL_MAP_DOWN);
	map.addMap(FLOW_MAP_UP, 2.1);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_UP));
	
	map.addMap(POTENTIAL_MAP_DOWN, 2);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
	
	// we do not know which weight will be removed, but both are OK
	map.removeMap(POTENTIAL_MAP_DOWN);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_UP));

	map.removeMap(FLOW_MAP_UP);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));

	map.removeMap(POTENTIAL_MAP_DOWN);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_NEUTRAL));
    }
    
    @Test
    public void obtainDirectionOnDynamicPotentialMap() {
	DynamicPotentialMap dynamicPotentialMap = new DynamicPotentialMap(POTENTIAL_MAP_DOWN, POTENTIAL_MAP_RIGHT);
	map.addMap(dynamicPotentialMap);
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_DOWN));
	
	dynamicPotentialMap.nextIteration();
	assertThat(obtainDirectionAtMapCenter(), is(DIRECTION_RIGHT));
    }

    private Double2D obtainDirectionAtMapCenter() {
	return map.obtainDirection(MAP_CENTER, MAP_CENTER);
    }

    private static class MyFlowMap extends ConstantFlowMap implements FlowMap {
	public MyFlowMap(Double2D value) {
	    super(MAP_SIZE, MAP_SIZE, value);
	}
    }
    
    private static class DynamicPotentialMap extends DynamicPathfindingMap<PotentialMap>implements PotentialMap {
	private static final long serialVersionUID = 1L;

	public DynamicPotentialMap(PotentialMap... iterations) {
	    super(MAP_SIZE, MAP_SIZE, iterations);
	}

	@Override
	public double obtainPotential(int x, int y) {
	    return mapIterations.peek().obtainPotential(x, y);
	}
    }
}

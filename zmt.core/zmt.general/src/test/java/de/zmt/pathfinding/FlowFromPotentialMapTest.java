package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.DIRECTION_DOWN;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.*;

public class FlowFromPotentialMapTest {
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

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void obtainDirectionOnSingle() {
	FlowFromPotentialMap map = new FlowFromPotentialMap(POTENTIAL_MAP_DOWN);
	assertThat(map.obtainDirection(MAP_CENTER, MAP_CENTER), is(DIRECTION_DOWN));
    }
}

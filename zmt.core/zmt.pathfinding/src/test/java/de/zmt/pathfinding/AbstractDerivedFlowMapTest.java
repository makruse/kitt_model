package de.zmt.pathfinding;

import static de.zmt.pathfinding.AbstractDerivedMap.NEUTRAL_WEIGHT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AbstractDerivedFlowMapTest {
    private static final int MAP_SIZE = 1;
    private static final int INVALID_MAP_SIZE = -MAP_SIZE;
    private static final double WEIGHT_VALUE = 2;

    private MyNotifyingMap notifyingMap;
    private MyDerivedFlowMap map;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        notifyingMap = new MyNotifyingMap();
        map = new MyDerivedFlowMap();
    }

    @Test
    public void addAndRemove() {
        assertThat(map.getUnderlyingMaps(), is(empty()));

        map.addMap(notifyingMap);
        assertThat(map.getUnderlyingMaps(), contains((PathfindingMap) notifyingMap));
        assertTrue(map.wasUpdateCalled());

        assertThat(map.removeMap(notifyingMap), is(true));
        assertThat(map.getUnderlyingMaps(), is(empty()));
        assertTrue(map.wasUpdateCalled());
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
        map.addMap(notifyingMap);
        assertTrue(map.wasUpdateCalled());

        notifyingMap.notifyListeners(0, 0);
        assertTrue(map.isDirty(0, 0));
    }

    /**
     * Test updating if a {@code DerivedFlowMap} is added to another
     * {@code DerivedFlowMap}.
     */
    @Test
    public void updateOnInnerMap() {
        MyDerivedFlowMap outerMap = new MyDerivedFlowMap();
        map.addMap(notifyingMap);
        outerMap.addMap(map);
        // called from forced update from adding map
        assertTrue(outerMap.wasUpdateCalled());

        notifyingMap.notifyListeners(0, 0);
        // map is marked dirty from dynamic map's change
        assertTrue(map.isDirty(0, 0));
        // map's update is propagated to outer map
        outerMap.updateIfDirty(0, 0);
        assertTrue(outerMap.wasUpdateCalled());
    }

    @Test
    public void addAndRemoveMapWithWeight() {
        map.addMap(notifyingMap, WEIGHT_VALUE);
        assertThat(map.getUnderlyingMaps(), contains((PathfindingMap) notifyingMap));
        assertThat(map.getWeight(notifyingMap), is(WEIGHT_VALUE));
        assertTrue(map.wasUpdateCalled());

        assertThat(map.removeMap(notifyingMap), is(true));
        assertThat(map.getUnderlyingMaps(), is(empty()));
        assertTrue(map.wasUpdateCalled());
    }

    @Test
    public void setWeight() {
        map.addMap(notifyingMap);
        assertThat(map.getWeight(notifyingMap), is(NEUTRAL_WEIGHT));
        assertTrue(map.wasUpdateCalled());

        map.setWeight(notifyingMap, WEIGHT_VALUE);
        assertThat(map.getWeight(notifyingMap), is(WEIGHT_VALUE));
        assertTrue(map.wasUpdateCalled());
    }

    @Test
    public void applyChanges() {
        map.applyChanges(DerivedMap.Changes.Factory.addMap((PathfindingMap) notifyingMap, WEIGHT_VALUE));
        assertThat(map.getUnderlyingMaps(), contains((PathfindingMap) notifyingMap));
        assertThat(map.getWeight(notifyingMap), is(WEIGHT_VALUE));
        assertTrue(map.wasUpdateCalled());

        map.applyChanges(DerivedMap.Changes.Factory.removeMap((PathfindingMap) notifyingMap));
        assertThat(map.getUnderlyingMaps(), is(empty()));
        assertTrue(map.wasUpdateCalled());
    }

    private static class MyDerivedFlowMap extends AbstractDerivedMap<PathfindingMap> {
        private static final long serialVersionUID = 1L;

        private boolean updateCalled;

        public MyDerivedFlowMap() {
            super(MAP_SIZE, MAP_SIZE);
        }

        public boolean wasUpdateCalled() {
            boolean called = updateCalled;
            updateCalled = false;
            return called;
        }

        @Override
        protected void update(int x, int y) {
            updateCalled = true;
        }
    }

    private static class MyNotifyingMap extends BasicMapChangeNotifier implements PathfindingMap {
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

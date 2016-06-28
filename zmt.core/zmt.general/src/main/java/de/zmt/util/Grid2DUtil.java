package de.zmt.util;

import java.io.Serializable;
import java.util.Arrays;

import sim.util.DoubleBag;
import sim.util.IntBag;

/**
 * Contains modified methods from {@link sim.field.grid.AbstractGrid2D} to
 * handle real numbers in neighborhood lookup for position and distance.
 * 
 * @see sim.field.grid.AbstractGrid2D
 * @author mey
 * 
 */
public final class Grid2DUtil {
    private Grid2DUtil() {

    }

    /**
     * Contains x and y grid positions from neighborhood lookup.
     * 
     * @author mey
     * 
     */
    public static class LocationsResult implements Serializable {
        private static final long serialVersionUID = 1L;

        public final IntBag xPos;
        public final IntBag yPos;

        public LocationsResult() {
            xPos = new IntBag();
            yPos = new IntBag();
        }

        public LocationsResult(IntBag xPos, IntBag yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public void clear() {
            xPos.clear();
            yPos.clear();
        }

        public void add(int x, int y) {
            xPos.add(x);
            yPos.add(y);
        }

        public int size() {
            // all three bags will have the same number of elements
            return xPos.numObjs;
        }

        @Override
        public String toString() {
            return "LocationsResult [size=" + size() + ", xPos=" + Arrays.toString(xPos.objs) + ", yPos="
                    + Arrays.toString(yPos.objs) + "]";
        }
    }

    /**
     * Contains a {@link LocationsResult} and values from these locations.
     * 
     * @author mey
     * 
     */
    public static class DoubleNeighborsResult implements Serializable {
        private static final long serialVersionUID = 1L;

        public final LocationsResult locations;
        public final DoubleBag values;

        public DoubleNeighborsResult() {
            this.locations = new LocationsResult();
            this.values = new DoubleBag();
        }

        public DoubleNeighborsResult(LocationsResult radialLocationsResult, DoubleBag values) {
            this.locations = radialLocationsResult;
            this.values = values;
        }

        public int size() {
            return locations.size();
        }

        @Override
        public String toString() {
            return "DoubleNeighborsResult [locationsResult=" + locations + ", values=" + Arrays.toString(values.objs)
                    + "]";
        }
    }
}

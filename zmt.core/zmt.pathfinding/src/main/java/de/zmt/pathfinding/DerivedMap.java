package de.zmt.pathfinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A pathfinding map that is derived from other underlying maps. Structural
 * changes are costly because every location needs to be updated thereafter.
 * With {@link Changes} objects these can be chained together to minimize
 * updating.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of pathfinding map that is derived from
 */
public interface DerivedMap<T extends PathfindingMap> extends PathfindingMap {

    /**
     * Applies structural changes from a {@link Changes} object.
     * 
     * @param changes
     *            the changes object
     * @return a map containing all added maps associated with their names
     */
    Map<T, String> applyChanges(Changes<T> changes);

    /**
     * Returns the content of this derived map as a {@link Changes} object. The
     * object can be used to add the same content to another derived map.
     * 
     * @return the map content as changes object
     */
    Changes<T> content();

    /**
     * Class to define structural changes to a {@link DerivedMap}. Instances are
     * immutable and can be created by {@link DerivedMap#content()} or its
     * {@link Factory}.
     * 
     * @author mey
     * @param <T>
     *            the type of pathfinding maps used
     *
     */
    public static final class Changes<T extends PathfindingMap> {
        private final int width;
        private final int height;
        private final Collection<T> mapsToAdd;
        private final Map<T, Double> weightsToPut;
        private final Collection<T> mapsToRemove;

        /**
         * Constructs a new {@link DerivedMap.Changes} object by copying given
         * data. Internal use only.
         * 
         * @param width
         * @param height
         * @param mapsToAdd
         * @param weightsToPut
         * @param mapsToRemove
         */
        Changes(int width, int height, Collection<T> mapsToAdd, Map<T, Double> weightsToPut,
                Collection<T> mapsToRemove) {
            super();
            this.width = width;
            this.height = height;
            // copy to new list / map to prevent wrapping it several times
            this.mapsToAdd = new ArrayList<>(mapsToAdd);
            this.weightsToPut = new HashMap<>(weightsToPut);
            this.mapsToRemove = new ArrayList<>(mapsToRemove);
        }

        /**
         * Adds a map to the {@link DerivedMap.Changes} object.
         * 
         * @param map
         *            the map to add
         * @return this object
         */
        public Changes<T> addMap(T map) {
            checkMapDimensions(map);
            ArrayList<T> newMapsToAdd = new ArrayList<>(mapsToAdd);
            newMapsToAdd.add(map);
            return new Changes<>(width, height, newMapsToAdd, getWeightsToPut(), getMapsToRemove());
        }

        /**
         * Adds a map to the {@link DerivedMap.Changes} object and associate it
         * with given weight.
         * 
         * @param map
         *            the map to add
         * @param weight
         *            the weight to associate the map with
         * @return this object
         */
        public Changes<T> addMap(T map, double weight) {
            return addMap(map).setWeight(map, weight);
        }

        /**
         * Adds a map to be removed to the {@link DerivedMap.Changes} object.
         * 
         * @param map
         *            the map to be removed
         * @return this object
         */
        public Changes<T> removeMap(T map) {
            checkMapDimensions(map);
            ArrayList<T> newMapsToRemove = new ArrayList<>(mapsToRemove);
            newMapsToRemove.add(map);
            return new Changes<>(width, height, getMapsToAdd(), getWeightsToPut(), newMapsToRemove);
        }

        /**
         * Sets a weight association to the {@link DerivedMap.Changes} object.
         * 
         * @param map
         * @param weight
         * @return this object
         */
        public Changes<T> setWeight(T map, double weight) {
            checkMapDimensions(map);
            HashMap<T, Double> newWeightsToPut = new HashMap<>(weightsToPut);
            newWeightsToPut.put(map, weight);
            return new Changes<>(width, height, getMapsToAdd(), newWeightsToPut, getMapsToRemove());
        }

        /**
         * Checks if map's dimensions map those of this object.
         * 
         * @param map
         *            the map to check dimensions for
         * @throws IllegalArgumentException
         *             if dimensions does not match
         */
        private void checkMapDimensions(T map) {
            if (map.getWidth() != width || map.getHeight() != height) {
                throw new IllegalArgumentException("Expected: is <" + getWidth() + ", " + getHeight() + ">\n"
                        + "but: was <" + map.getWidth() + ", " + map.getHeight() + ">");
            }
        }

        /** @return the width of maps referred in changes */
        int getWidth() {
            return width;
        }

        /** @return the height of maps referred in changes */
        int getHeight() {
            return height;
        }

        Collection<T> getMapsToAdd() {
            return Collections.unmodifiableCollection(mapsToAdd);
        }

        Map<T, Double> getWeightsToPut() {
            return Collections.unmodifiableMap(weightsToPut);
        }

        Collection<T> getMapsToRemove() {
            return Collections.unmodifiableCollection(mapsToRemove);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((mapsToAdd == null) ? 0 : mapsToAdd.hashCode());
            result = prime * result + ((mapsToRemove == null) ? 0 : mapsToRemove.hashCode());
            result = prime * result + ((weightsToPut == null) ? 0 : weightsToPut.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Changes<?> other = (Changes<?>) obj;
            if (mapsToAdd == null) {
                if (other.mapsToAdd != null) {
                    return false;
                }
            } else if (!mapsToAdd.equals(other.mapsToAdd)) {
                return false;
            }
            if (mapsToRemove == null) {
                if (other.mapsToRemove != null) {
                    return false;
                }
            } else if (!mapsToRemove.equals(other.mapsToRemove)) {
                return false;
            }
            if (weightsToPut == null) {
                if (other.weightsToPut != null) {
                    return false;
                }
            } else if (!weightsToPut.equals(other.weightsToPut)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[width=" + width + ", height=" + height + "]";
        }

        /**
         * Factory class for creating {@link DerivedMap.Changes} objects.
         * 
         * @author mey
         *
         */
        public static final class Factory {
            private Factory() {

            }

            /**
             * Creates a {@link DerivedMap.Changes} object with a map to add.
             * 
             * @param map
             *            the map to add
             * @return this object
             */
            public static <T extends PathfindingMap> Changes<T> addMap(T map) {
                return create(map).addMap(map);
            }

            /**
             * Creates a {@link DerivedMap.Changes} with a map to add and
             * associate it with given weight.
             * 
             * @param map
             *            the map to add
             * @param weight
             *            the weight to associate the map with
             * @return this object
             */
            public static <T extends PathfindingMap> Changes<T> addMap(T map, double weight) {
                return create(map).addMap(map, weight);
            }

            /**
             * Creates a {@link DerivedMap.Changes} with a map to remove.
             * 
             * @param map
             *            the map to be removed
             * @return this object
             */
            public static <T extends PathfindingMap> Changes<T> removeMap(T map) {
                return create(map).removeMap(map);
            }

            /**
             * Creates a {@link DerivedMap.Changes} with a weight to associate.
             * 
             * @param map
             * @param weight
             * @return this object
             */
            public static <T extends PathfindingMap> Changes<T> setWeight(T map, double weight) {
                return create(map).setWeight(map, weight);
            }

            private static <T extends PathfindingMap> Changes<T> create(T map) {
                return new Changes<>(map.getWidth(), map.getHeight(), Collections.<T> emptyList(),
                        Collections.<T, Double> emptyMap(), Collections.<T> emptyList());
            }
        }
    }
}
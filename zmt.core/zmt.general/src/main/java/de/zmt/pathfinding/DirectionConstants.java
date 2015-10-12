package de.zmt.pathfinding;

import sim.util.Double2D;

/**
 * Direction constants to be used in pathfinding.
 * 
 * @author mey
 *
 */
class DirectionConstants {

    static final Double2D DIRECTION_NEUTRAL = new Double2D(0, 0);
    static final Double2D DIRECTION_DOWN = new Double2D(0, 1);
    static final Double2D DIRECTION_UP = DIRECTION_DOWN.negate();
    static final Double2D DIRECTION_RIGHT = new Double2D(1, 0);
    static final Double2D DIRECTION_LEFT = DIRECTION_RIGHT.negate();

    private DirectionConstants() {

    }

}

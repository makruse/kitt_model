package de.zmt.pathfinding;

import sim.util.Double2D;

/**
 * Direction constants to be used in pathfinding.
 * 
 * @author mey
 *
 */
class DirectionConstants {
    /** Neutral direction not pointing anywhere: (0,0) */
    public static final Double2D DIRECTION_NEUTRAL = new Double2D(0, 0);
    /** Direction pointing down: (0,1) */
    public static final Double2D DIRECTION_DOWN = new Double2D(0, 1);
    /** Direction pointing up: (0,-1) */
    public static final Double2D DIRECTION_UP = DIRECTION_DOWN.negate();
    /** Direction pointing right: (1,0) */
    public static final Double2D DIRECTION_RIGHT = new Double2D(1, 0);
    /** Direction pointing left: (0,-1) */
    public static final Double2D DIRECTION_LEFT = DIRECTION_RIGHT.negate();

    private DirectionConstants() {

    }

}

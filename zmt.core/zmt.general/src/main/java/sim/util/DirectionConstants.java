package sim.util;

/**
 * Contains constant of directions represented as {@link Double2D} unit vectors.
 * 
 * @author mey
 *
 */
public final class DirectionConstants {
    /** Neutral direction not pointing anywhere: (0,0) */
    public static final Double2D NEUTRAL = new Double2D(0, 0);
    /** Direction pointing east: (1,0) */
    public static final Double2D EAST = new Double2D(1, 0);
    /** Direction pointing south: (0,1) */
    public static final Double2D SOUTH = new Double2D(0, 1);
    /** Direction pointing west: (-1,0) */
    public static final Double2D WEST = new Double2D(-1, 0);
    /** Direction pointing north: (0,-1) */
    public static final Double2D NORTH = new Double2D(0, -1);

    private static final double INV_SQRT_2 = 1 / Math.sqrt(2);
    /** Direction pointing south-east: (1,1)^ */
    public static final Double2D SOUTHEAST = new Double2D(INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing south-west: (-1,1)^ */
    public static final Double2D SOUTHWEST = new Double2D(-INV_SQRT_2, INV_SQRT_2);
    /** Direction pointing north-west: (-1,-1)^ */
    public static final Double2D NORTHWEST = new Double2D(-INV_SQRT_2, -INV_SQRT_2);
    /** Direction pointing north-east: (1,-1)^ */
    public static final Double2D NORTHEAST = new Double2D(INV_SQRT_2, -INV_SQRT_2);

    private DirectionConstants() {

    }
}

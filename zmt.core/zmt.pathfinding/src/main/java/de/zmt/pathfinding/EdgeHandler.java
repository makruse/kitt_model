package de.zmt.pathfinding;

import de.zmt.util.MathUtil;
import sim.field.grid.DoubleGrid2D;

/**
 * Handler for edges of potential maps or their raw grid data. The default is to
 * use the nearest value within boundaries. Instances are immutable.
 * 
 * @author mey
 *
 */
public class EdgeHandler {
    private static final EdgeHandler DEFAULT_EDGE_HANDLER = new EdgeHandler(Hint.EXTEND, 0);

    private final Hint hint;
    private final double value;

    /**
     * Returns default {@link EdgeHandler} with {@link Hint#EXTEND}.
     * 
     * @return the default edge handler
     */
    public static EdgeHandler getDefault() {
	return DEFAULT_EDGE_HANDLER;
    }

    /**
     * Constructs a new {@link EdgeHandler} with {@link Hint#CUSTOM}.
     * 
     * @param value
     */
    public EdgeHandler(double value) {
	this(Hint.CUSTOM, value);
    }

    private EdgeHandler(Hint hint, double value) {
	super();
	this.hint = hint;
	this.value = value;
    }

    /**
     * Gets value from grid at given position and handle locations beyond
     * boundaries according to this {@link EdgeHandler}.
     * 
     * @param grid
     * @param x
     * @param y
     * @return the value from grid according to this object
     */
    public double getValue(DoubleGrid2D grid, int x, int y) {
	switch (hint) {
	case EXTEND:
	    return grid.get(MathUtil.clamp(x, 0, grid.getWidth() - 1), MathUtil.clamp(y, 0, grid.getHeight() - 1));
	case CUSTOM:
	    if (x < 0 || x >= grid.getWidth() || y < 0 || y >= grid.getHeight()) {
		return value;
	    }
	    return grid.get(x, y);
	default:
	    throw new UnsupportedOperationException(hint + " not implemented.");
	}
    }

    /**
     * Gets value from map at given position and handle locations beyond
     * boundaries according to this {@link EdgeHandler}.
     * 
     * @param map
     * @param x
     * @param y
     * @return the value from map according to this object
     */
    public double getValue(PotentialMap map, int x, int y) {
	switch (hint) {
	case EXTEND:
	    return map.obtainPotential(MathUtil.clamp(x, 0, map.getWidth() - 1),
		    MathUtil.clamp(y, 0, map.getHeight() - 1));
	case CUSTOM:
	    if (x < 0 || x >= map.getWidth() || y < 0 || y >= map.getHeight()) {
		return value;
	    }
	    return map.obtainPotential(x, y);
	default:
	    throw new UnsupportedOperationException(hint + " not implemented.");
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((hint == null) ? 0 : hint.hashCode());
	// only consider value if valid
	if (hint == Hint.CUSTOM) {
	    long temp;
	    temp = Double.doubleToLongBits(value);
	    result = prime * result + (int) (temp ^ (temp >>> 32));
	}
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
	EdgeHandler other = (EdgeHandler) obj;
	if (hint != other.hint) {
	    return false;
	}
	// only consider value if valid
	if (hint == Hint.CUSTOM && Double.doubleToLongBits(value) != Double.doubleToLongBits(other.value)) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	if (hint == Hint.EXTEND) {
	    return getClass().getSimpleName() + "[condition=" + hint + ", value=" + value + "]";
	}
	return getClass().getSimpleName() + "[condition=" + hint + "]";
    }

    /**
     * The hint for handling edges.
     * 
     * @author mey
     *
     */
    private static enum Hint {
	/** The nearest value within boundaries is used. */
	EXTEND,
	/** A custom value is used. */
	CUSTOM
    }
}
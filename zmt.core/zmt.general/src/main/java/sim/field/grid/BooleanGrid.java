package sim.field.grid;

import java.util.BitSet;

/**
 * Class for storing boolean values over a two-dimensional grid.
 * 
 * @author mey
 *
 */
public class BooleanGrid extends AbstractGrid2D {
    private static final long serialVersionUID = 1L;

    /**
     * The bits representing the values of the grid. Stored in row-major order.
     */
    private final BitSet bits;

    /**
     * Constructs new boolean grid.
     * 
     * @param width
     * @param height
     */
    public BooleanGrid(int width, int height) {
	this(width, height, new BitSet(width * height));
    }

    /**
     * Constructs new boolean grid by copying values from other boolean grid.
     * 
     * @param other
     */
    public BooleanGrid(BooleanGrid other) {
	this(other.width, other.height, (BitSet) other.bits.clone());
    }

    /**
     * Constructs new boolean grid by copying values from given field.
     * 
     * @param values
     *            the values for this grid
     */
    public BooleanGrid(boolean[][] values) {
	this(values.length, values[0].length);
	setTo(values);
    }

    /**
     * Constructs a new {@link BooleanGrid}. Internal constructor.
     * 
     * @param width
     *            the width of the grid
     * @param height
     *            the height of the grid
     * @param bits
     *            the bits representing the grid values
     */
    BooleanGrid(int width, int height, BitSet bits) {
	super();
	this.width = width;
	this.height = height;
	this.bits = bits;
    }

    /**
     * Gets the value at given coordinate.
     * 
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @return the value at given coordinate
     */
    public boolean get(int x, int y) {
	return bits.get(y * width + x);
    }

    /**
     *
     * Sets the value at given coordinate.
     * 
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @param value
     *            the value to be set at given coordinate
     */
    public void set(int x, int y, boolean value) {
	bits.set(y * width + x, value);
    }

    /**
     * Sets all the locations in the grid to the provided value.
     * 
     * @param value
     *            the value to set
     * @return this object
     */
    public BooleanGrid setTo(boolean value) {
	if (!value) {
	    bits.clear();
	} else {
	    bits.set(0, width * height);
	}
	return this;
    }

    /**
     * Sets this grid to the values from the given array.
     * 
     * @param field
     *            a two-dimensional array
     * @return this object
     */
    public BooleanGrid setTo(boolean[][] field) {
	if (field == null) {
	    throw new NullPointerException("Values can't be null.");
	}
	
	int w = field.length;
	int h = 0;
	if (w != 0) {
	    h = field[0].length;
	}
	for (int i = 0; i < w; i++) {
	    if (field[i].length != h) {
		throw new IllegalArgumentException("Cannot use a non-rectangular values array.");
	    }
	}

	if (w != width || h != height) {
	    throw new IllegalArgumentException("Values must match this grid's dimensions.");
	}

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		set(x, y, field[x][y]);
	    }
	}
	return this;
    }

    /**
     * Converts this grid to a two-dimensional array.
     * 
     * @return a two-dimensional array with values from this grid
     */
    public boolean[][] toField() {
	boolean[][] field = new boolean[width][height];
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		field[x][y] = get(x, y);
	    }
	}
	return field;
    }

    @Override
    public String toString() {
	return getClass().getName() + " [width=" + width + ", height=" + height + "]";
    }
}
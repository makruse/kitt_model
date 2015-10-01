package de.zmt.sim.field.grid;

import sim.field.grid.AbstractGrid2D;

/**
 * Class for wrapping a two dimensional boolean array.
 * 
 * @author mey
 *
 */
public class BooleanGrid extends AbstractGrid2D {
    private static final long serialVersionUID = 1L;

    private final boolean[][] field;

    /**
     * Constructs new boolean grid.
     * 
     * @param width
     * @param height
     */
    public BooleanGrid(int width, int height) {
	super();

	this.width = width;
	this.height = height;
	this.field = new boolean[width][height];
    }

    /**
     * Constructs new boolean grid by copying values from other boolean grid.
     * 
     * @param other
     */
    public BooleanGrid(BooleanGrid other) {
	this(other.field);
    }

    /**
     * Constructs new boolean grid by copying values from given field.
     * 
     * @param field
     */
    public BooleanGrid(boolean[][] field) {
	super();

	if (field == null) {
	    throw new NullPointerException("field can't be null.");
	}
	int w = field.length;
	int h = 0;
	if (w != 0) {
	    h = field[0].length;
	}
	for (int i = 0; i < w; i++) {
	    if (field[i].length != h) {
		throw new IllegalArgumentException("Cannot initialize with a non-rectangular field.");
	    }
	}

	width = w;
	height = h;
	this.field = new boolean[w][h];
	// copy field
	for (int x = 0; x < width; x++) {
	    System.arraycopy(field[x], 0, this.field[x], 0, height);
	}
    }

    public boolean get(int x, int y) {
	return field[x][y];
    }

    public void set(int x, int y, boolean value) {
	field[x][y] = value;
    }

    /**
     * Sets all the locations in the grid the provided value.
     * 
     * @param value
     * @return this object
     */
    public final BooleanGrid setTo(final boolean value) {
	boolean[] fieldx = null;
	for (int x = 0; x < width; x++) {
	    fieldx = field[x];
	    for (int y = 0; y < height; y++) {
		fieldx[y] = value;
	    }
	}
	return this;
    }

    public boolean[][] getField() {
	return field;
    }
}
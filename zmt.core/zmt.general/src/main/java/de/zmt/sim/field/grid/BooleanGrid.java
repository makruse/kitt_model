package de.zmt.sim.field.grid;

/**
 * Class for wrapping a two dimensional boolean array.
 * 
 * @author mey
 *
 */
// could be extended to implement Grid2D if necessary
public class BooleanGrid {
    private final int width;
    private final int height;
    private final boolean[][] grid;

    public BooleanGrid(int width, int height) {
        super();
	this.width = width;
	this.height = height;
        grid = new boolean[width][height];
    }

    public void reset() {
	for (int x = 0; x < width; x++) {
    	boolean[] dirtyCellsInner = grid[x];
	    for (int y = 0; y < height; y++) {
    	    dirtyCellsInner[y] = false;
    	}
        }
    }

    public boolean get(int x, int y) {
	return grid[x][y];
    }

    public void set(int x, int y, boolean value) {
        grid[x][y] = value;
    }
    
    public boolean[][] getGrid() {
        return grid;
    }

    public int getWidth() {
	return width;
    }

    public int getHeight() {
	return height;
    }
}
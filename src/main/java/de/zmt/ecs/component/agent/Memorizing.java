package de.zmt.ecs.component.agent;

import java.io.Serializable;

import de.zmt.ecs.Component;
import sim.field.grid.IntGrid2D;
import sim.portrayal.MemoryPortrayal.MemoryPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;
import sim.util.*;

/**
 * Memory of an agent.
 * 
 * @author mey
 * 
 */
public class Memorizing implements ProvidesPortrayable<MemoryPortrayable>, Component {
    private static final long serialVersionUID = 1L;

    /** Field space covered by one memory cell. */
    public static final int MEM_CELL_SIZE = 50;
    /** Inverse of {@link #MEM_CELL_SIZE} to speed up calculations. */
    public static final double MEM_CELL_SIZE_INVERSE = 1 / (double) MEM_CELL_SIZE;
    /** Grid storing visitation count for each cell. */
    private final IntGrid2D grid;

    private final MyPortrayable myPortrayable;

    /**
     * Creates new memory instance mapped to a field, using
     * {@link #MEM_CELL_SIZE_INVERSE}.
     * 
     * @param fieldWidth
     * @param fieldHeight
     */
    public Memorizing(double fieldWidth, double fieldHeight) {
	double preciseWidth = fieldWidth * MEM_CELL_SIZE_INVERSE;
	double preciseHeight = fieldHeight * MEM_CELL_SIZE_INVERSE;

	// add 1 to cover for space cut by the integer cast
	grid = new IntGrid2D((int) preciseWidth + 1, (int) preciseHeight + 1);

	myPortrayable = new MyPortrayable(preciseWidth, preciseHeight);
    }

    /**
     * Get memory value for given field position.
     * 
     * @param fieldPos
     * @return memory value
     */
    public int get(Double2D fieldPos) {
	Int2D gridPosition = mapPosition(fieldPos);
	return grid.get(gridPosition.x, gridPosition.y);
    }

    /**
     * Increase counter of memory cell associated with the given field position.
     * 
     * @param fieldPos
     */
    public void increase(Double2D fieldPos) {
	Int2D gridPosition = mapPosition(fieldPos);
	int currentValue = grid.get(gridPosition.x, gridPosition.y);
	grid.set(gridPosition.x, gridPosition.y, currentValue + 1);
    }

    /**
     * 
     * @param fieldPos
     * @return grid position for given field position
     */
    private static Int2D mapPosition(Double2D fieldPos) {
	Double2D gridPosition = fieldPos.multiply(MEM_CELL_SIZE_INVERSE);
	return new Int2D((int) gridPosition.x, (int) gridPosition.y);
    }

    @Override
    public String toString() {
	return "Memory [width=" + grid.getWidth() + ", height=" + grid.getHeight() + "]";
    }

    @Override
    public MemoryPortrayable providePortrayable() {
	return myPortrayable;
    }

    public class MyPortrayable implements MemoryPortrayable, Serializable {
	private static final long serialVersionUID = 1L;

	public MyPortrayable(double preciseWidth, double preciseHeight) {
	    this.preciseWidth = preciseWidth;
	    this.preciseHeight = preciseHeight;
	}

	/**
	 * Precise number of memory cells spanning over field width:<br>
	 * (field width / {@link #MEM_CELL_SIZE})
	 */
	private final double preciseWidth;
	/**
	 * Precise number of memory cells spanning over field height:<br>
	 * (field height / {@link #MEM_CELL_SIZE})
	 */
	private final double preciseHeight;

	@Override
	public int get(int memX, int memY) {
	    return grid.get(memX, memY);
	}

	@Override
	public final int getWidth() {
	    return grid.getWidth();
	}

	@Override
	public final int getHeight() {
	    return grid.getHeight();
	}

	@Override
	public double getPreciseWidth() {
	    return preciseWidth;
	}

	@Override
	public double getPreciseHeight() {
	    return preciseHeight;
	}

    }
}

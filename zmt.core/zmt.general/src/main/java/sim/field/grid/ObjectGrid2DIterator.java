package sim.field.grid;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator over non-null elements within an {@link ObjectGrid2D}.
 * <p>
 * This can act as a replacement for {@link ObjectGrid2D#elements()} which will
 * create a new bag each time consuming a huge amount of memory if used on a
 * large grid. The iteration order is column-by-column, making the result
 * similar to the iterator of the bag from {@code elements()}.
 * 
 * @author mey
 *
 */
public class ObjectGrid2DIterator implements Iterator<Object> {
    private final ObjectGrid2D grid;
    private int xStart = 0;
    private int yStart = 0;
    private Object element;

    public ObjectGrid2DIterator(ObjectGrid2D grid) {
	this.grid = grid;
	forward();
    }

    @Override
    public boolean hasNext() {
	return element != null;
    }

    @Override
    public Object next() {
	if (element == null) {
	    throw new NoSuchElementException();
	}

	Object next = element;
	forward();
	return next;
    }

    /** Iterates forward to next non-null element. */
    private void forward() {
	element = null;
	for (; xStart < grid.getWidth(); xStart++) {
	    for (; yStart < grid.getHeight(); yStart++) {
		if (element != null) {
		    return;
		}
		element = grid.get(xStart, yStart);
	    }
	    yStart = 0;
	}
    }

}
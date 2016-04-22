package sim.field.grid;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

public class ObjectGrid2DIteratorTest {
    private static final Object[][] GRID_VALUES = new Object[][] { { null, "first" }, { "second", null } };

    private ObjectGrid2D grid;
    private ObjectGrid2DIterator iterator;

    @Before
    public void setUp() throws Exception {
	grid = new ObjectGrid2D(GRID_VALUES);
	iterator = new ObjectGrid2DIterator(grid);
    }

    @Test
    public void test() {
	Collection<Object> returnedElements = new ArrayList<>();
	for (; iterator.hasNext();) {
	    returnedElements.add(iterator.next());
	}
	assertThat(returnedElements.toArray(), is(grid.elements().toArray()));
    }

}

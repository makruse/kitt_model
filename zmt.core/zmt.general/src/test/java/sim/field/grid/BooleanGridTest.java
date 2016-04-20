package sim.field.grid;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class BooleanGridTest {
    private static final int GRID_HEIGHT = 3;
    private static final int GRID_WIDTH = 2;

    private BooleanGrid grid;

    @Before
    public void setUp() throws Exception {
	grid = new BooleanGrid(GRID_WIDTH, GRID_HEIGHT);
    }

    @Test
    public void set() {
	for (int y = 0; y < GRID_HEIGHT; y++) {
	    for (int x = 0; x < GRID_WIDTH; x++) {
		testSet(y, x, true);
		testSet(y, x, false);
	    }
	}
    }

    private void testSet(int y, int x, boolean value) {
        grid.set(x, y, value);
        assertThat(grid.get(x, y), is(value));
    }

    @Test
    public void setTo() {
	testSetTo(true);
	testSetTo(false);
    }

    private void testSetTo(boolean value) {
	grid.setTo(true);
	for (int y = 0; y < GRID_HEIGHT; y++) {
	    for (int x = 0; x < GRID_WIDTH; x++) {
		assertThat(grid.get(x, y), is(true));
	    }
	}
    }

    @Test
    public void toField() {
	grid.set(0, 0, true);
	assertThat(grid.toField()[0][0], is(true));
    }

}

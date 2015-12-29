package sim.engine.output;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.hamcrest.Matchers;
import org.junit.*;

import sim.engine.output.message.CollectMessage;

public class CategoryCollectorTest {
    private static final String CATEGORY_1 = "category1";
    private static final String CATEGORY_2 = "category2";

    private TestCategoryCollector collector;

    @Before
    public void setUp() throws Exception {
	collector = new TestCategoryCollector();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getCollectable() {
	Collectable collectable = collector.getCollectable();
	assertThat(collectable.obtainHeaders(),
		contains(allOf(containsString(CATEGORY_1), containsString(TestCollectable.HEADER)),
			allOf(containsString(CATEGORY_2), containsString(TestCollectable.HEADER))));
	assertThat(collectable.obtainValues(),
		Matchers.<Object> contains(TestCollectable.VALUE, TestCollectable.VALUE));
    }

    private static class TestCategoryCollector extends CategoryCollector<String, TestCollectable> {
	public TestCategoryCollector() {
	    super(new LinkedHashSet<>(Arrays.asList(CATEGORY_1, CATEGORY_2)));
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void collect(CollectMessage message) {
	}

	@Override
	protected TestCollectable createCollectable(String category) {
	    return new TestCollectable();
	}

    }

    private static class TestCollectable implements Collectable {
	private static final long serialVersionUID = 1L;

	private static final String HEADER = "header";
	private static final int VALUE = 1;

	@Override
	public Iterable<String> obtainHeaders() {
	    return Collections.singleton(HEADER);
	}

	@Override
	public Iterable<?> obtainValues() {
	    return Collections.singleton(VALUE);
	}

	@Override
	public int getSize() {
	    return 1;
	}

    }
}

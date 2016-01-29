package sim.engine.output;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.LinkedHashSet;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import sim.engine.output.message.CollectMessage;

public class CategoryCollectorTest {
    private static final String CATEGORY_1 = "category1";
    private static final String CATEGORY_2 = "category2";

    private static final String HEADER = "header";
    private static final int VALUE = 1;
    private TestCategoryCollector collector;

    @Before
    public void setUp() throws Exception {
	collector = new TestCategoryCollector();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getCollectable() {
	Collectable<Integer> collectable = collector.getCollectable();
	assertThat(collectable.obtainHeaders(), contains(allOf(containsString(CATEGORY_1), containsString(HEADER)),
		allOf(containsString(CATEGORY_2), containsString(HEADER))));
	assertThat(collectable.obtainValues(), Matchers.<Object> contains(VALUE, VALUE));
    }

    private static class TestCategoryCollector extends CategoryCollector<String, TestCollectable<Integer>, Integer> {
	public TestCategoryCollector() {
	    super(new LinkedHashSet<>(Arrays.asList(CATEGORY_1, CATEGORY_2)));
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void collect(CollectMessage message) {
	}

	@Override
	protected TestCollectable<Integer> createCollectable(String category) {
	    return new TestCollectable<>(HEADER, VALUE);
	}

    }
}

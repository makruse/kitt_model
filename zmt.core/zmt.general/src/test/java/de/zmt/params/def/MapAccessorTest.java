package de.zmt.params.def;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MapAccessorTest {
    private TestDefinitionWithMap definition;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	definition = new TestDefinitionWithMap();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void locators() {
	assertThat((Iterable<String>) definition.accessor().identifiers(), both(hasItem(is(TestDefinitionWithMap.KEY)))
		.and(not(hasItem(TestDefinitionWithMap.NOT_AUTOMATABLE_KEY))));
    }

    @Test
    public void get() {
	assertThat(definition.accessor().get(TestDefinitionWithMap.KEY), is(TestDefinitionWithMap.VALUE));
    }

    @Test
    public void getOnInvalid() {
	thrown.expect(IllegalArgumentException.class);
	definition.accessor().get(new Object());
    }

    @Test
    public void set() {
	String key = TestDefinitionWithMap.KEY;
	int oldValue = TestDefinitionWithMap.VALUE;
	int newValue = oldValue + 1;
	assertThat(definition.accessor().set(key, newValue), is(oldValue));
	assertThat(definition.map.get(key), is(newValue));
    }

    @Test
    public void setOnNotAutomatable() {
	thrown.expect(NotAutomatable.IllegalAutomationException.class);
	definition.accessor().set(TestDefinitionWithMap.NOT_AUTOMATABLE_KEY, 0);
    }

    private static class TestDefinitionWithMap implements ParamDefinition {
	private static final long serialVersionUID = 1L;

	private static final String KEY = "key";
	private static final int VALUE = 1;
	private static final String NOT_AUTOMATABLE_KEY = "not automatable key";

	private Map<String, Integer> map = new HashMap<>();

	{
	    map.put(KEY, VALUE);
	    map.put(NOT_AUTOMATABLE_KEY, VALUE);
	}

	@Override
	public String getTitle() {
	    return getClass().getSimpleName();
	}

	@Override
	public DefinitionAccessor<Integer> accessor() {
	    return new MapAccessor<>(map, Collections.singleton(NOT_AUTOMATABLE_KEY));
	}

	@Override
	public String toString() {
	    return getTitle() + "[" + map + "]";
	}

    }
}

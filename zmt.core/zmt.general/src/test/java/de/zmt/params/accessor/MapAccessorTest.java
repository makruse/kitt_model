package de.zmt.params.accessor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.zmt.params.MapParamDefinition;

public class MapAccessorTest {
    private DefinitionWithMap definition;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefinitionWithMap();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void locators() {
        assertThat(
                definition.accessor().identifiers().stream().map(identifier -> (String) identifier.get())
                        .collect(Collectors.toList()),
                both(hasItem(is(DefinitionWithMap.KEY))).and(not(hasItem(DefinitionWithMap.NOT_AUTOMATABLE_KEY))));
    }

    @Test
    public void get() {
        assertThat(definition.accessor().get(() -> DefinitionWithMap.KEY), is(DefinitionWithMap.VALUE));
    }

    @Test
    public void getOnInvalid() {
        thrown.expect(IllegalArgumentException.class);
        definition.accessor().get(Object::new);
    }

    @Test
    public void set() {
        String key = DefinitionWithMap.KEY;
        int oldValue = DefinitionWithMap.VALUE;
        int newValue = oldValue + 1;
        assertThat(definition.accessor().set(() -> key, newValue), is(oldValue));
        assertThat(definition.map.get(key), is(newValue));
    }

    @Test
    public void setOnNotAutomatable() {
        thrown.expect(NotAutomatable.IllegalAutomationException.class);
        definition.accessor().set(() -> DefinitionWithMap.NOT_AUTOMATABLE_KEY, 0);
    }

    private static class DefinitionWithMap extends MapParamDefinition<String, Integer> {
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
        protected Map<String, Integer> getMap() {
            return map;
        }

        @Override
        protected Set<String> getNotAutomatableKeys() {
            return Collections.singleton(NOT_AUTOMATABLE_KEY);
        }
    }
}

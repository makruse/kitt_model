package de.zmt.params.accessor;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.zmt.params.ParamDefinition;
import de.zmt.params.TestDefinition;
import de.zmt.params.accessor.NotAutomatable.IllegalAutomationException;

public class ReflectionAccessorTest {
    private DefinitionWithNested definition;
    private ReflectionAccessor accessor;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        definition = new DefinitionWithNested();
        accessor = new ReflectionAccessor(definition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void identifiers() {
        assertThat(accessor.identifiers().stream().map(identifier -> identifier.get()).collect(Collectors.toList()),
                both(hasItem(TestDefinition.FIELD_INT)).and(not(hasItem(DefinitionWithNested.FIELD_NOT_AUTO))));
    }

    @Test
    public void get() throws NoSuchFieldException {
        assertThat(accessor.get(() -> TestDefinition.FIELD_INT), is(definition.getIntValue()));
    }

    @Test
    public void getOnInvalid() {
        thrown.expect(IllegalArgumentException.class);
        accessor.get(() -> new Object());
    }

    @Test
    public void getOnDefinitionCollection() {
        Object collectionDefinition = accessor.get(() -> DefinitionWithNested.FIELD_DEFINITIONS);
        assertThat(collectionDefinition, is(instanceOf(ParamDefinition.class)));
        assertThat(
                ((ParamDefinition) collectionDefinition).accessor()
                        .get(() -> DefinitionWithNested.TITLE_NESTED_DEFINITION),
                is(DefinitionWithNested.NESTED_DEFINITION));
    }
    
    @Test
    public void set() {
        int oldValue = definition.getIntValue();
        int newValue = oldValue + 1;
        assertThat(accessor.set(() -> TestDefinition.FIELD_INT, newValue), is(oldValue));
        assertThat(definition.getIntValue(), is(newValue));
    }

    @Test
    public void setOnNotAutomatable() {
        thrown.expect(IllegalAutomationException.class);
        new DefinitionWithNested().accessor().get(() -> DefinitionWithNested.FIELD_NOT_AUTO);
    }

    private static class DefinitionWithNested extends TestDefinition {
        private static final long serialVersionUID = 1L;

        private static final Field FIELD_DEFINITIONS = getDeclaredField(DefinitionWithNested.class, "definitions");
        private static final String TITLE_NESTED_DEFINITION = "nested definition";
        private static final TestDefinition NESTED_DEFINITION = new TestDefinition(TITLE_NESTED_DEFINITION);

        @SuppressWarnings("unused") // via reflection
        private final Collection<TestDefinition> definitions = Collections.singleton(NESTED_DEFINITION);
    }
}

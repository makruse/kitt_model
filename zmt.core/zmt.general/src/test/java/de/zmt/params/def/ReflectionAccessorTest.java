package de.zmt.params.def;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.zmt.params.def.NotAutomatable.IllegalAutomationException;

public class ReflectionAccessorTest {
    private TestDefinition definition;
    private ReflectionAccessor accessor;

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	definition = new NotAutomatableFieldDefinition();
	accessor = new ReflectionAccessor(definition);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void identifiers() {
	assertThat(accessor.identifiers(), both(hasItem(TestDefinition.FIELD_INT))
		.and(not(hasItem(NotAutomatableFieldDefinition.FIELD_NOT_AUTO))));
    }

    @Test
    public void get() throws NoSuchFieldException {
	assertThat(accessor.get(TestDefinition.FIELD_INT), is(definition.getIntValue()));
    }

    @Test
    public void getOnInvalid() {
	thrown.expect(IllegalArgumentException.class);
	accessor.get(new Object());
    }

    @Test
    public void set() {
	int oldValue = definition.getIntValue();
	int newValue = oldValue + 1;
	assertThat(accessor.set(TestDefinition.FIELD_INT, newValue), is(oldValue));
	assertThat(definition.getIntValue(), is(newValue));
    }

    @Test
    public void setOnNotAutomatable() {
	thrown.expect(IllegalAutomationException.class);
	new NotAutomatableFieldDefinition().accessor().get(NotAutomatableFieldDefinition.FIELD_NOT_AUTO);
    }

    private static class NotAutomatableFieldDefinition extends TestDefinition {
	private static final long serialVersionUID = 1L;

	@NotAutomatable
	private String notAutomatableValue = "not automatable";
	public static final Field FIELD_NOT_AUTO = TestDefinition.getDeclaredField(NotAutomatableFieldDefinition.class,
		"notAutomatableValue");
    }

}

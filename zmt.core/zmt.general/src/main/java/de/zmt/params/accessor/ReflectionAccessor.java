package de.zmt.params.accessor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlTransient;

import com.thoughtworks.xstream.annotations.XStreamOmitField;

import de.zmt.params.ParamDefinition;
import de.zmt.params.accessor.NotAutomatable.IllegalAutomationException;

/**
 * The default {@link DefinitionAccessor} which accesses the automatable
 * {@link ParamDefinition}'s fields via reflection.
 * 
 * @author mey
 *
 */
public class ReflectionAccessor implements DefinitionAccessor<Object> {
    private static final String ILLEGAL_ACCESS_MESSAGE_FORMAT_STRING = "Cannot access field %s.";
    private static final String NO_MATCHING_FIELD_MESSAGE_FORMAT_STRING = "%s does not match available fields: %s";

    /** The target {@link ParamDefinition}. */
    private final ParamDefinition target;

    /**
     * Constructs a {@link ReflectionAccessor} targeting the given definition.
     * 
     * @param target
     */
    public ReflectionAccessor(ParamDefinition target) {
        super();
        this.target = target;
    }

    @Override
    public Set<Identifier<Field>> identifiers() {
        return streamAutomatableFields().map(Identifier::create)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    /**
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws IllegalAutomationException
     *             {@inheritDoc}
     * @throws IllegalStateException
     *             if associated field could not be accessed
     */
    @Override
    public Object set(Identifier<?> identifier, Object value) {
        Field field = createAccessibleField(identifier);
        try {
            Object oldValue = field.get(target);
            field.set(target, value);
            return oldValue;
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format(ILLEGAL_ACCESS_MESSAGE_FORMAT_STRING, field), e);
        }

    }

    /**
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws IllegalAutomationException
     *             {@inheritDoc}
     */
    @Override
    public Object get(Identifier<?> identifier) {
        Field field = createAccessibleField(identifier);
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format(ILLEGAL_ACCESS_MESSAGE_FORMAT_STRING, field), e);
        }
    }

    /**
     * @see #streamAutomatableFields(Class)
     * @return all automatable fields from this class including inherited ones
     */
    private Stream<Field> streamAutomatableFields() {
        return streamAutomatableFields(target.getClass());
    }

    /**
     * Gets all automatable fields from the given class including inherited
     * ones.
     * 
     * @param clazz
     * @return all automatable fields from given class including inherited ones
     */
    private static Stream<Field> streamAutomatableFields(Class<?> clazz) {
        Stream<Field> fields = Arrays.stream(clazz.getDeclaredFields())
                // skip fields that should not be automated
                .filter(field -> isFieldAutomatable(field));

        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            fields = Stream.concat(fields, streamAutomatableFields(superclass));
        }

        return fields;
    }

    /**
     * Creates an accessible field from an identifier after validating it.
     * 
     * @see #isFieldAutomatable(Field)
     * @param identifier
     *            the identifier to use
     * @return the accessible {@link Field} from identifier
     */
    private Field createAccessibleField(Identifier<?> identifier) {
        Object object = identifier.get();
        if (!(object instanceof Field && ((Field) object).getDeclaringClass().isAssignableFrom(target.getClass()))) {
            throw new IllegalArgumentException(
                    String.format(NO_MATCHING_FIELD_MESSAGE_FORMAT_STRING, identifier, identifiers()));

        }
        Field field = (Field) object;
        if (!isFieldAutomatable(field)) {
            throw new NotAutomatable.IllegalAutomationException("Automation not allowed for field: " + field);
        }
        field.setAccessible(true);

        return field;
    }

    /**
     * Return <code>true</code> if given field can be automated.
     * 
     * @param field
     *            the field to check
     * @return <code>true</code> if the given field can be automated.
     */
    private static boolean isFieldAutomatable(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())
                && field.getAnnotation(NotAutomatable.class) == null
                && field.getAnnotation(XStreamOmitField.class) == null
                && field.getAnnotation(XmlTransient.class) == null;
    }

}

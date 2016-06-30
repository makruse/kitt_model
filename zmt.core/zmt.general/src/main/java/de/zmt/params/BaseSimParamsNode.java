package de.zmt.params;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ParamsInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Abstract implementation of a {@link SimParams} node. Provides an inspector
 * that gets populated from added definitions. Added definitions are checked
 * against allowed classes that can be specified by implementing classes.
 * 
 * @author mey
 *
 */
public abstract class BaseSimParamsNode extends BaseParamsNode implements SimParams, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /** The inspector showing definitions in GUI. */
    private transient Optional<ParamsInspector> inspector = Optional.empty();

    /**
     * Implementing classes need to handle adding an allowed definition here.
     * 
     * @param definition
     *            the definition
     * @return <code>true</code> if definition could be added
     */
    protected abstract boolean addDefinitionInternal(ParamDefinition definition);

    /**
     * Implementing classes need to handle removing a definition here.
     * 
     * @param definition
     *            the definition
     * @return <code>true</code> if definition could be removed
     */
    protected abstract boolean removeDefinitionInternal(ParamDefinition definition);

    /**
     * Returns allowed types which definitions are checked on when added.
     * Default behavior is to not allow any definition. Implementing classes
     * need to specify the types allowed.
     * 
     * @return the set of allowed definition types
     */
    protected Collection<Class<? extends ParamDefinition>> getAllowedDefinitionTypes() {
        return Collections.emptySet();
    }

    /**
     * Adds a {@link ParamDefinition} if allowed internally and to the
     * inspector.
     * 
     * @throws IllegalArgumentException
     *             if the definition's type is not allowed
     */
    @Override
    public final boolean addDefinition(ParamDefinition definition) {
        if (!getAllowedDefinitionTypes().contains(definition.getClass())) {
            throw new IllegalArgumentException("Cannot add definition of type " + definition.getClass()
                    + ". Only instances of " + getAllowedDefinitionTypes() + " allowed.");
        }

        if (addDefinitionInternal(definition)) {
            inspector.ifPresent(inspector -> inspector.addDefinitionTab(definition));
            return true;
        }
        return false;
    }

    /**
     * Removes a {@link ParamDefinition} internally and from inspector if
     * present.
     */
    @Override
    public final boolean removeDefinition(ParamDefinition definition) {
        if (removeDefinitionInternal(definition)) {
            inspector.ifPresent(inspector -> inspector.removeDefinitionTab(definition));
            return true;
        }
        return false;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        if (!inspector.isPresent()) {
            inspector = Optional.of(new ParamsInspector(state, this::removeDefinitionInternal, getDefinitions()));
        }
        return inspector.get();
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getDefinitions() + "]";
    }
}

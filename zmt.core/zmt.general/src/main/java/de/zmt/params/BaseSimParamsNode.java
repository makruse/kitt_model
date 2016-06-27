package de.zmt.params;

import java.util.Optional;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ParamsInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Abstract implementation of a {@link SimParams} node. Provides an inspector
 * that gets populated from added definitions.
 * 
 * @author mey
 *
 */
public abstract class BaseSimParamsNode extends BaseParamsNode implements SimParams, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /** The inspector showing definitions in GUI. */
    private transient Optional<ParamsInspector> inspector = Optional.empty();

    /**
     * Implementing classes need to handle adding a definition here.
     * 
     * @param definition
     *            the optional definition
     * @return <code>true</code> if definition could be added
     */
    protected abstract boolean addDefinitionInternal(ParamDefinition definition);

    /**
     * Implementing classes need to handle removing a definition here.
     * 
     * @param definition
     *            the optional definition
     * @return <code>true</code> if definition could be removed
     */
    protected abstract boolean removeDefinitionInternal(ParamDefinition definition);

    @Override
    public boolean addDefinition(ParamDefinition definition) {
        if (addDefinitionInternal(definition)) {
            inspector.ifPresent(inspector -> inspector.addDefinitionTab(definition));
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDefinition(ParamDefinition definition) {
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

package de.zmt.params.def;

/**
 * {@link ParamDefinition} skeletal implementation.
 * 
 * @author mey
 * 
 */
public abstract class AbstractParamDefinition implements ParamDefinition {
    private static final long serialVersionUID = 1L;

    public boolean hideTitle() {
	return true;
    }

    @Override
    public String toString() {
	return getTitle();
    }
}
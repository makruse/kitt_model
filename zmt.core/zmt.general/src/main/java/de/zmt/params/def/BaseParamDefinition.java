package de.zmt.params.def;

/**
 * {@link ParamDefinition} skeletal implementation.
 * 
 * @author mey
 * 
 */
public class BaseParamDefinition implements ParamDefinition {
    private static final long serialVersionUID = 1L;

    public boolean hideTitle() {
	return true;
    }

    @Override
    public String getTitle() {
	return getClass().getSimpleName();
    }

    @Override
    public String toString() {
	return getTitle();
    }
}
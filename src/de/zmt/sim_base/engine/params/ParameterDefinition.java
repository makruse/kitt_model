package de.zmt.sim_base.engine.params;

//TODO property descriptions
public abstract class ParameterDefinition {
    public abstract String getTitle();

    public boolean hideTitle() {
	return true;
    }
}
package de.zmt.sim_base.engine.params;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlTransient;

public abstract class ParameterDefinition {
    @XmlTransient
    private boolean initialized = false;

    /**
     * Called by the {@link Unmarshaller} after the process is complete.
     * 
     * @param unmarshaller
     * @param parent
     */
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	initialized = true;
    }

    /**
     * True after the unmarshalling process is complete and all values have been
     * initialized
     */
    protected boolean isInitialized() {
	return initialized;
    }

    public abstract String getTitle();

    public boolean hideTitle() {
	return true;
    }
}
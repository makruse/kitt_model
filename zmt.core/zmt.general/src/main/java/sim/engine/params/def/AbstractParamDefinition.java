package sim.engine.params.def;

import javax.xml.bind.Unmarshaller;

/**
 * A {@link ParamDefinition} providing feedback about the unmarshal process.
 * 
 * @author cmeyer
 * 
 */
public abstract class AbstractParamDefinition implements
	ParamDefinition {
    private static final long serialVersionUID = 1L;

    private boolean unmarshalling = false;

    /**
     * This method is called immediately after the object is created and before
     * the unmarshalling of this object begins.
     * 
     * @see Unmarshaller
     * @param unmarshaller
     * @param parent
     */
    protected void beforeUnmarshal(Unmarshaller unmarshaller, Object parent) {
	unmarshalling = true;
    }

    /**
     * This method is called after all the properties (except IDREF) are
     * unmarshalled for this object, but before this object is set to the parent
     * object.
     * 
     * @see Unmarshaller
     * @param unmarshaller
     * @param parent
     */
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	unmarshalling = false;
    }

    /** @return true during the unmarshalling process */
    protected boolean isUnmarshalling() {
	return unmarshalling;
    }

    public boolean hideTitle() {
	return true;
    }

    @Override
    public String toString() {
	return getTitle();
    }
}
package de.zmt.params;

/**
 * Abstract base class for {@link ParamsNode} implementing {@link #hashCode()}
 * and {@link #equals(Object)} based on the return value of
 * {@link #getDefinitions()}.
 * 
 * @author mey
 *
 */
public abstract class BaseParamsNode extends BaseParamDefinition implements ParamsNode {
    private static final long serialVersionUID = 1L;

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getDefinitions() == null) ? 0 : getDefinitions().hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	ParamsNode other = (ParamsNode) obj;
	if (getDefinitions() == null) {
	    if (other.getDefinitions() != null) {
		return false;
	    }
	} else if (!getDefinitions().equals(other.getDefinitions())) {
	    return false;
	}
	return true;
    }


    @Override
    public String toString() {
        return super.toString() + "[" + getDefinitions() + "]";
    }

}

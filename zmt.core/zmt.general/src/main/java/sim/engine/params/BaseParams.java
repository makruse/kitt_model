package sim.engine.params;

/**
 * Abstract base class for {@code Params} implementing {@link #hashCode()} and
 * {@link #equals(Object)} based on return value of {@link #getDefinitions()}.
 * 
 * @author mey
 *
 */
public abstract class BaseParams implements Params {
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
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Params other = (Params) obj;
	if (getDefinitions() == null) {
	    if (other.getDefinitions() != null)
		return false;
	} else if (!getDefinitions().equals(other.getDefinitions()))
	    return false;
	return true;
    }

}

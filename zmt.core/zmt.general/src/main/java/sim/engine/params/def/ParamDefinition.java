package sim.engine.params.def;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import sim.engine.params.Params;
import sim.portrayal.inspector.ParamsInspector;

/**
 * A set of parameter definitions which is stored within a {@link Params}
 * object.
 * 
 * @author mey
 *
 */
public interface ParamDefinition extends Serializable {
    /**
     * Title appearing in {@link ParamsInspector}'s tab list, also used in
     * automation to discriminate between objects if there are several of one
     * class.
     * 
     * @return the title
     */
    String getTitle();

    /**
     * Prevents a property from being automated by zmt.automation.
     * 
     * @author mey
     * 
     */
    @Retention(RUNTIME)
    @Target({ FIELD, METHOD })
    public static @interface NotAutomatable {
	/**
	 * Thrown if a property carrying {@link NotAutomatable} annotation is
	 * tried to be automated.
	 * 
	 * @author mey
	 * 
	 */
	public static class IllegalAutomationException extends RuntimeException {
	    private static final long serialVersionUID = 1L;

	    public IllegalAutomationException(String message) {
		super(message);
	    }
	}
    }
}
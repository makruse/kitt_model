package de.zmt.sim.engine.params.def;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.Serializable;
import java.lang.annotation.*;

import de.zmt.sim.portrayal.inspector.ParamsInspector;

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
     * @author cmeyer
     * 
     */
    @Retention(RUNTIME)
    @Target({ FIELD, METHOD })
    public static @interface NotAutomatable {
	/**
	 * Thrown if a property carrying {@link NotAutomatable} annotation is
	 * tried to be automated.
	 * 
	 * @author cmeyer
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
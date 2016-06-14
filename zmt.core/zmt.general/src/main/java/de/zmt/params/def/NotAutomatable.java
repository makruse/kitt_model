package de.zmt.params.def;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to prevent a parameter from being changed via
 * {@link DefinitionAccessor}.
 * 
 * @author mey
 * 
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface NotAutomatable {
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
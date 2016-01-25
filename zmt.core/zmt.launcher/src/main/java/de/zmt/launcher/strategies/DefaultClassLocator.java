package de.zmt.launcher.strategies;

import sim.display.*;
import sim.engine.ZmtSimState;

class DefaultClassLocator implements ClassLocator {
    private static final String BASE_PACKAGE_SUFFIX = "sim.";
    private static final String SIM_PACKAGE_SUFFIX = BASE_PACKAGE_SUFFIX + "engine.";
    private static final String GUI_PACKAGE_SUFFIX = BASE_PACKAGE_SUFFIX + "display.";
    private static final String GUI_CLASS_NAME_SUFFIX = "WithUI";

    /**
     * Find {@link ZmtSimState} class by following convention:<br>
     * 
     * <pre>
     * {@value #SIM_PACKAGE_SUFFIX}{@code <simName>}
     * </pre>
     * 
     * {@code simName}'s first character is capitalized.
     * <p>
     * For example de.zmt.sim.engine.Example if example was given.
     */
    @Override
    public Class<? extends ZmtSimState> findSimStateClass(String simName) throws ClassNotFoundException {
	return findClass(SIM_PACKAGE_SUFFIX + capitalizeFirstCharacter(simName), ZmtSimState.class);
    }

    /**
     * Find {@link GUIState} class by following convention:<br>
     * {@code simName}.{@value #GUI_PACKAGE_SUFFIX}{@code <simName>}
     * {@value #GUI_CLASS_NAME_SUFFIX}
     * <p>
     * For example de.zmt.example.sim.display.ExampleWithUI if example was
     * given.
     */
    @Override
    public Class<? extends ZmtGUIState> findGuiStateClass(String simName) throws ClassNotFoundException {
	return findClass(GUI_PACKAGE_SUFFIX + capitalizeFirstCharacter(simName) + GUI_CLASS_NAME_SUFFIX,
		ZmtGUIState.class);
    }

    /**
     * @param string
     * @return {@code s} with upper case first character
     */
    private static String capitalizeFirstCharacter(String string) {
	return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /**
     * Finds class from {@code className} and provide an additional check if
     * child of {@code parentType}.
     * 
     * @param className
     * @param parentType
     * @return checked class literal
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> findClass(String className, Class<T> parentType)
	    throws ClassNotFoundException {
	Class<?> clazz = Class.forName(className);

	if (parentType.isAssignableFrom(clazz)) {
	    return (Class<T>) clazz;
	}
	throw new IllegalArgumentException(clazz + " must be child of " + parentType);
    }
}

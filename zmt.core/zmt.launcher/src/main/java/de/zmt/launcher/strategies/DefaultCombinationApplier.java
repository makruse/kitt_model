package de.zmt.launcher.strategies;

import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.*;

import de.zmt.launcher.strategies.CombinationCompiler.Combination;
import de.zmt.util.ParamsUtil;
import sim.engine.params.SimParams;
import sim.engine.params.def.*;
import sim.engine.params.def.ParamDefinition.NotAutomatable;

class DefaultCombinationApplier implements CombinationApplier {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationApplier.class.getName());

    @Override
    public <T extends SimParams> Iterable<T> applyCombinations(Iterable<Combination> combinations,
	    final T defaultSimParams) {
	final Iterator<Combination> combinationsIterator = combinations.iterator();
	return new Iterable<T>() {

	    @Override
	    public Iterator<T> iterator() {
		return new Iterator<T>() {

		    @Override
		    public boolean hasNext() {
			return combinationsIterator.hasNext();
		    }

		    @Override
		    public T next() {
			Combination combination = combinationsIterator.next();
			logger.fine("Applying combination: " + combination);
			return applyCombination(combination, defaultSimParams);
		    }
		};
	    }
	};
    }

    /**
     * Set values of given {@code combination} in corresponding fields of an
     * automatable parameters object. The original object is not modified.
     * 
     * @param combination
     * @param params
     * @return modified {@code params} with combination applied
     */
    private static <T extends SimParams> T applyCombination(Combination combination, T params) {
	T clonedParams = ParamsUtil.clone(params);
	Map<Class<? extends ParamDefinition>, Collection<ParamDefinition>> clonedParamsMap = createParamsMap(
		clonedParams);
	for (AutoDefinition.FieldLocator locator : combination.keySet()) {
	    try {
		applyCombinationValue(locator, combination.get(locator), clonedParamsMap);
	    } catch (NoSuchFieldException | IllegalAccessException e) {
		DefaultSimulationLooper.logger.log(Level.WARNING, "Could not access field for locator " + locator, e);
	    }
	}
	return clonedParams;
    }

    /**
     * Create a map where each {@link ParamDefinition} class points to the
     * objects of that class contained in the parameters object.
     * 
     * @param params
     *            parameters object to make the map from
     * @return map map of definition classes pointing to objects of this class
     */
    private static Map<Class<? extends ParamDefinition>, Collection<ParamDefinition>> createParamsMap(
	    SimParams params) {
	Map<Class<? extends ParamDefinition>, Collection<ParamDefinition>> paramsMap = new HashMap<>();

	for (ParamDefinition definition : params.getDefinitions()) {
	    Class<? extends ParamDefinition> definitionClass = definition.getClass();
	    Collection<ParamDefinition> definitionsOfClass = paramsMap.get(definitionClass);

	    if (definitionsOfClass == null) {
		definitionsOfClass = new ArrayList<>(1);
		paramsMap.put(definitionClass, definitionsOfClass);
	    }

	    definitionsOfClass.add(definition);
	}

	return paramsMap;
    }

    /**
     * Set one combination value to the corresponding field of an automatable
     * parameters object.
     * 
     * @see #applyCombinations(Iterable, SimParams)
     * @param locator
     * @param automationValue
     * @param paramsMap
     *            map of definition classes pointing to objects of this class
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(AutoDefinition.FieldLocator locator, Object automationValue,
	    Map<Class<? extends ParamDefinition>, Collection<ParamDefinition>> paramsMap)
		    throws NoSuchFieldException, IllegalAccessException {
	Class<? extends ParamDefinition> targetClass = locator.getClassContaining();
	Field targetField = targetClass.getDeclaredField(locator.getFieldName());

	// check for exclusion
	if (targetField.getAnnotation(NotAutomatable.class) != null) {
	    throw new NotAutomatable.IllegalAutomationException(locator + ": automation not allowed, annotated with @"
		    + NotAutomatable.class.getSimpleName() + ".");
	}

	// ensure field is accessible, private fields are not by default
	targetField.setAccessible(true);

	Collection<ParamDefinition> definitionsOfTargetClass = paramsMap.get(targetClass);
	if (definitionsOfTargetClass == null) {
	    throw new IllegalArgumentException(
		    targetClass + " not contained in definitions within parameter object. Valid definitions are: "
			    + paramsMap.keySet());
	}
	// traverse all objects of locator's class
	for (ParamDefinition definition : definitionsOfTargetClass) {
	    // only continue if title matches
	    if (locator.getObjectTitle() != null && !definition.getTitle().equals(locator.getObjectTitle())) {
		continue;
	    }
	    // set automation value to field
	    targetField.set(definition, automationValue);
	}
    }
}

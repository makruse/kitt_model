package de.zmt.launcher.strategies;

import java.lang.reflect.Field;
import java.util.Iterator;
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
	for (AutoDefinition.FieldLocator locator : combination.keySet()) {
	    try {
		applyCombinationValue(locator, combination.get(locator), clonedParams);
	    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
		DefaultSimulationLooper.logger.log(Level.WARNING, "Could not access field for locator " + locator, e);
	    }
	}
	return clonedParams;
    }

    /**
     * Set one combination value to the corresponding field of an automatable
     * parameters object.
     * 
     * @see #applyCombinations(Iterable, SimParams)
     * @param locator
     * @param automationValue
     * @param params
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(AutoDefinition.FieldLocator locator, Object automationValue,
	    SimParams params)
		    throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
	Class<? extends ParamDefinition> targetClass = locator.getClazz();
	Field targetField = targetClass.getDeclaredField(locator.getFieldName());

	// check for exclusion
	if (targetField.getAnnotation(NotAutomatable.class) != null) {
	    throw new NotAutomatable.IllegalAutomationException(locator + ": automation not allowed, annotated with @"
		    + NotAutomatable.class.getSimpleName() + ".");
	}

	// ensure field is accessible, private fields are not by default
	targetField.setAccessible(true);

	// traverse all objects of locator's class
	for (ParamDefinition definition : params.getDefinitions(targetClass)) {
	    // only continue if title matches
	    if (locator.getObjectTitle() != null && !definition.getTitle().equals(locator.getObjectTitle())) {
		continue;
	    }
	    // set automation value to field
	    targetField.set(definition, automationValue);
	}
    }
}

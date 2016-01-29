package de.zmt.launcher.strategies;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zmt.util.ParamsUtil;
import sim.engine.params.SimParams;
import sim.engine.params.def.FieldLocator;
import sim.engine.params.def.ParamDefinition;
import sim.engine.params.def.ParamDefinition.NotAutomatable;

class DefaultCombinationApplier implements CombinationApplier {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DefaultCombinationApplier.class.getName());

    @Override
    public Iterable<AppliedCombination> applyCombinations(Iterable<Combination> combinations,
	    final SimParams defaultSimParams) {
	final Iterator<Combination> combinationsIterator = combinations.iterator();
	return new Iterable<AppliedCombination>() {

	    @Override
	    public Iterator<AppliedCombination> iterator() {
		return new Iterator<AppliedCombination>() {

		    @Override
		    public boolean hasNext() {
			return combinationsIterator.hasNext();
		    }

		    @Override
		    public AppliedCombination next() {
			Combination combination = combinationsIterator.next();
			logger.fine("Applying combination: " + combination);
			SimParams resultingParams = applyCombination(combination, defaultSimParams);
			return new AppliedCombination(combination, resultingParams);
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
	for (FieldLocator locator : combination.keySet()) {
	    try {
		applyCombinationValue(locator, combination.get(locator), clonedParams.getDefinitions());
	    } catch (NoSuchFieldException | IllegalAccessException e) {
		logger.log(Level.WARNING, "Could not access field for locator " + locator, e);
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
     * @param definitions
     *            map of definition classes pointing to objects of this class
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(FieldLocator locator, Object automationValue,
	    Collection<? extends ParamDefinition> definitions) throws NoSuchFieldException, IllegalAccessException {
	Class<?> targetClass = locator.getDeclaringClass();
	Field targetField = targetClass.getDeclaredField(locator.getFieldName());

	// check for exclusion
	if (targetField.getAnnotation(NotAutomatable.class) != null) {
	    throw new NotAutomatable.IllegalAutomationException(locator + ": automation not allowed, annotated with @"
		    + NotAutomatable.class.getSimpleName() + ".");
	}

	// ensure field is accessible, private fields are not by default
	targetField.setAccessible(true);

	List<ParamDefinition> assignableDefinitions = new ArrayList<>();
	// collect definitions of assignable classes if their title matches
	for (ParamDefinition definition : definitions) {
	    if (targetClass.isAssignableFrom(definition.getClass())
		    && ((locator.getObjectTitle() == null) || locator.getObjectTitle().equals(definition.getTitle()))) {
		assignableDefinitions.add(definition);
	    }
	}

	// only one definition should match
	if (assignableDefinitions.isEmpty()) {
	    throw new IllegalArgumentException(
		    locator + " does not match to any of the classes in parameters object. Valid classes are "
			    + getClasses(definitions));
	} else if (assignableDefinitions.size() > 1) {
	    throw new IllegalArgumentException(
		    locator + " is ambiguous. Several definitions match: " + assignableDefinitions);
	}
	// only a single matching definition: set value there
	targetField.set(assignableDefinitions.get(0), automationValue);
    }

    /**
     * 
     * @param collection
     * @return set of class literals within {@code collection}
     */
    private static Set<Class<?>> getClasses(Collection<?> collection) {
	Set<Class<?>> classes = new HashSet<>();
	for (Object element : collection) {
	    classes.add(element.getClass());
	}
	return classes;
    }
}

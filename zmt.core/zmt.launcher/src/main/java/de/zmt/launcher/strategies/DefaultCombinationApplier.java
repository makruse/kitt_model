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

import de.zmt.params.Params;
import de.zmt.params.SimParams;
import de.zmt.params.def.FieldLocator;
import de.zmt.params.def.ParamDefinition;
import de.zmt.params.def.ParamDefinition.NotAutomatable;
import de.zmt.util.ParamsUtil;

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
		applyCombinationValue(locator, combination.get(locator), clonedParams);
	    } catch (NoSuchFieldException | IllegalAccessException e) {
		logger.log(Level.WARNING, "Could not access field for locator " + locator, e);
	    }
	}
	return clonedParams;
    }

    /**
     * Sets one combination value to the corresponding field of an automatable
     * parameters object.
     * 
     * @see #applyCombinations(Iterable, SimParams)
     * @param locator
     * @param automationValue
     * @param params
     *            {@link Params} object containing the definition with the
     *            corresponding field
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(FieldLocator locator, Object automationValue, Params params)
	    throws NoSuchFieldException, IllegalAccessException {
	Field targetField = locator.getDeclaringClass().getDeclaredField(locator.getFieldName());

	// check for exclusion
	if (targetField.getAnnotation(NotAutomatable.class) != null) {
	    throw new NotAutomatable.IllegalAutomationException(locator + ": automation not allowed, annotated with @"
		    + NotAutomatable.class.getSimpleName() + ".");
	}

	// ensure field is accessible, private fields are not by default
	targetField.setAccessible(true);

	List<ParamDefinition> matchingDefinitions = collectMatchingDefinitions(params, locator);

	// only one definition should match
	if (matchingDefinitions.isEmpty()) {
	    throw new IllegalArgumentException(
		    locator + " does not match to any of the classes in parameters object. Valid classes are "
			    + getClasses(params.getDefinitions()));
	} else if (matchingDefinitions.size() > 1) {
	    throw new IllegalArgumentException(
		    locator + " is ambiguous. Several definitions match: " + matchingDefinitions);
	}
	// only a single matching definition: set value there
	targetField.set(matchingDefinitions.get(0), automationValue);
    }

    /**
     * Collects matching definitions with assignable classes and matching titles
     * specified in given {@link FieldLocator}.
     * 
     * @param params
     *            the {@link Params} object containing the candidate definitions
     * @param locator
     *            the {@link FieldLocator} specifying class and title
     * @return {@link List} of definitions matching with the given locator
     */
    private static List<ParamDefinition> collectMatchingDefinitions(Params params, FieldLocator locator) {
	List<ParamDefinition> matchingDefinitions = new ArrayList<>();
	Class<?> targetClass = locator.getDeclaringClass();
	String targetTitle = locator.getObjectTitle();

	for (ParamDefinition definition : params.getDefinitions()) {
	    if (targetClass.isAssignableFrom(definition.getClass())
		    && ((targetTitle == null) || targetTitle.equals(definition.getTitle()))) {
		matchingDefinitions.add(definition);
	    }

	    // definitions inside a definition
	    if (definition instanceof Params) {
		matchingDefinitions.addAll(collectMatchingDefinitions((Params) definition, locator));
	    }
	}

	return matchingDefinitions;
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

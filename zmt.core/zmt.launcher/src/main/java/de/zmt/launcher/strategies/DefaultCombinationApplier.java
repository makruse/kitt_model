package de.zmt.launcher.strategies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zmt.params.ParamDefinition;
import de.zmt.params.ParamsNode;
import de.zmt.params.SimParams;
import de.zmt.params.def.Locator;
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
	for (Locator locator : combination.keySet()) {
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
     * @param paramsNode
     *            {@link ParamsNode} object containing the definition with the
     *            corresponding field
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(Locator locator, Object automationValue, ParamsNode paramsNode)
	    throws NoSuchFieldException, IllegalAccessException {
	List<ParamDefinition> matchingDefinitions = collectMatchingDefinitions(paramsNode, locator);

	// only one definition should match
	if (matchingDefinitions.isEmpty()) {
	    throw new IllegalArgumentException(
		    locator + " does not match to any of the classes in parameters object. Valid classes are "
			    + getClasses(paramsNode.getDefinitions()));
	} else if (matchingDefinitions.size() > 1) {
	    throw new IllegalArgumentException(
		    locator + " is ambiguous. Several definitions match: " + matchingDefinitions);
	}

	matchingDefinitions.get(0).accessor().set(locator.getIdentifier(), automationValue);
    }

    /**
     * Collects matching definitions with assignable classes and matching titles
     * specified in given {@link Locator}.
     * 
     * @param paramsNode
     *            the {@link ParamsNode} object containing the candidate definitions
     * @param locator
     *            the {@link Locator} specifying class and title
     * @return {@link List} of definitions matching with the given locator
     */
    private static List<ParamDefinition> collectMatchingDefinitions(ParamsNode paramsNode, Locator locator) {
	List<ParamDefinition> matchingDefinitions = new ArrayList<>();
	Class<?> targetClass = locator.getTargetClass();
	String targetTitle = locator.getObjectTitle();

	for (ParamDefinition definition : paramsNode.getDefinitions()) {
	    if (targetClass.isAssignableFrom(definition.getClass())
		    && ((targetTitle == null) || targetTitle.equals(definition.getTitle()))) {
		matchingDefinitions.add(definition);
	    }

	    // definitions inside a definition
	    if (definition instanceof ParamsNode) {
		matchingDefinitions.addAll(collectMatchingDefinitions((ParamsNode) definition, locator));
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

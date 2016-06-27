package de.zmt.launcher.strategies;

import java.util.Iterator;
import java.util.logging.Logger;

import de.zmt.params.ParamDefinition;
import de.zmt.params.SimParams;
import de.zmt.params.accessor.DefinitionAccessor;
import de.zmt.params.accessor.DefinitionAccessor.Identifier;
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
            applyCombinationValue(locator, combination.get(locator), clonedParams);
        }
        return clonedParams;
    }

    /**
     * Sets one combination value to the corresponding automatable parameter.
     * 
     * @see #applyCombinations(Iterable, SimParams)
     * @param locator
     *            the locator to locate the parameter to be automated
     * @param automationValue
     *            the value to set the parameter to
     * @param rootDefinition
     *            the root {@link ParamDefinition} from which the parameter is
     *            accessible
     */
    private static void applyCombinationValue(Locator locator, Object automationValue, ParamDefinition rootDefinition) {
        if (locator.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot locate parameter: locator for value " + automationValue + " is empty.");
        }

        Iterator<? extends Identifier<?>> iterator = locator.getIdentifiers().iterator();
        DefinitionAccessor<?> currentAccessor = rootDefinition.accessor();
        while (true) {
            Identifier<?> identifier = iterator.next();
            // current identifier should point to another accessor
            if (iterator.hasNext()) {
                Object value = currentAccessor.get(identifier);
                if (value instanceof ParamDefinition) {
                    currentAccessor = ((ParamDefinition) value).accessor();
                } else {
                    throw new IllegalArgumentException(
                            "Cannot locate parameter: " + value + " is not a " + ParamDefinition.class.getSimpleName()
                                    + " although " + locator + " contains another identifier after " + identifier);
                }
            }
            // last identifier, should point to parameter that is automated
            else {
                currentAccessor.set(identifier, automationValue);
                break;
            }
        }
    }
}

package de.zmt.util;

import java.io.*;
import java.lang.reflect.Field;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import sim.engine.SimState;
import de.zmt.sim.engine.params.Params;
import de.zmt.sim.engine.params.def.*;
import de.zmt.sim.engine.params.def.AutoDefinition.NotAutomatable;

/**
 * Utility methods for running and automating simulations.
 * 
 * @author cmeyer
 * 
 */
public final class AutomationUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(AutomationUtil.class
	    .getName());

    public static final String DEFAULT_INPUT_DIR = "parameters"
	    + File.separator;
    private static final Format SIMTIME_FORMAT = new SimpleDateFormat(
	    "hh:mm:ss.SSS");
    private static final double DEFAULT_SIMTIME = 1000;

    public static final String ZERO_PADDED_FORMAT_STRING = "%04d";

    private AutomationUtil() {

    }

    /**
     * Loads logging.properties file from working directory for setting up the
     * logger.
     */
    public static void setupLogger() {
	try {
	    System.setProperty("java.util.logging.config.file",
		    "logging.properties");
	    LogManager logManager = LogManager.getLogManager();
	    logManager.readConfiguration();
	} catch (IOException e) {
	    Logger.getAnonymousLogger().log(Level.WARNING,
		    "Failed to load file logging.properties", e);
	}
    }

    /**
     * Run a simulation for {@value #DEFAULT_SIMTIME} simulation time.
     * 
     * @param sim
     */
    public static void runSimulation(SimState sim) {
	runSimulation(sim, DEFAULT_SIMTIME);
    }

    /**
     * Run a simulation for a given time.
     * 
     * @param sim
     * @param simTime
     */
    public static void runSimulation(SimState sim, double simTime) {
	runSimulation(sim, simTime, null);
    }

    /**
     * Run a simulation for a given time.
     * 
     * @param sim
     * @param simTime
     * @param index
     *            optional simulation index used in output messages
     */
    public static void runSimulation(SimState sim, double simTime, Integer index) {
	long startTime = System.currentTimeMillis();

	// run the simulation
	sim.start();

	while (sim.schedule.step(sim) && sim.schedule.getTime() < simTime) {
	}

	sim.finish();
	long runTime = System.currentTimeMillis() - startTime;

	String runTimeString = SIMTIME_FORMAT.format(new Date(runTime));

	logger.info("Simulation " + (index != null ? index : "")
		+ " finished with " + sim.schedule.getSteps() + " steps in "
		+ runTimeString);
    }

    /**
     * Combines all auto definitions given in the argument according to their
     * specifications.
     * 
     * @param autoDefinitions
     * @return all possible combinations as a collection of maps having field
     *         locators pointing to a value
     * 
     */
    public static Collection<Map<AutoDefinition.FieldLocator, Object>> combineAutoDefs(
	    Collection<? extends AutoDefinition> autoDefinitions) {
	// map of field locators pointing to their set of automation values
	Map<AutoDefinition.FieldLocator, Collection<Object>> valuesPerParam = new HashMap<>();

	// iterate through all autoParams and collect values
	for (AutoDefinition autoDef : autoDefinitions) {
	    Collection<Object> paramValues = autoDef.getValues();
	    valuesPerParam.put(autoDef.getLocator(), paramValues);
	}

	// compute all combinations
	return combineDefsRecursive(valuesPerParam);
    }

    /**
     * Helper method generating parameters for
     * {@link #combineDefsRecursive(Map, Map, Queue, Collection)}.
     * 
     * @see #combineDefsRecursive(Map, Map, Queue, Collection)
     * @param collections
     * @return resulting combinations
     */
    private static <K, V> Collection<Map<K, V>> combineDefsRecursive(
	    Map<K, Collection<V>> collections) {
	return combineDefsRecursive(collections, new HashMap<K, V>(),
		new ArrayDeque<>(collections.keySet()),
		new ArrayList<Map<K, V>>());
    }

    /**
     * Computes all possible combinations between {@code collections}, resulting
     * in a collection of maps containing one mapping for each key to a value
     * from a collection. Use {@link #combineDefsRecursive(Map)} as entry point.
     * 
     * @param collections
     *            map of keys pointing to a collection of values
     * @param item
     *            current item
     * @param remainingKeys
     *            keys remaining, recursion ends when empty
     * @param result
     * @return result
     */
    private static <K, V> Collection<Map<K, V>> combineDefsRecursive(
	    Map<K, Collection<V>> collections, Map<K, V> item,
	    Queue<K> remainingKeys, Collection<Map<K, V>> result) {
	// leaf: combination done, add it to result
	if (remainingKeys.isEmpty()) {
	    result.add(new HashMap<>(item));
	    return result;
	}

	// key queue decreases in size for every ongoing recursion
	K key = remainingKeys.poll();
	for (V value : collections.get(key)) {
	    item.put(key, value);
	    combineDefsRecursive(collections, item, new ArrayDeque<>(
		    remainingKeys), result);
	    // go one level up the tree and keep the elements before
	    item.remove(key);
	}

	return result;
    }

    /**
     * Set values of given {@code combination} in corresponding fields of an
     * automatable parameters object.
     * 
     * @param combination
     * @param params
     */
    public static void applyCombination(Map<AutoDefinition.FieldLocator, Object> combination,
	    Params params) {
	for (AutoDefinition.FieldLocator locator : combination.keySet()) {
	    try {
		applyCombinationValue(locator, combination.get(locator), params);
	    } catch (NoSuchFieldException | SecurityException
		    | IllegalArgumentException | IllegalAccessException e) {
		logger.log(Level.WARNING, "Could not access field for locator "
			+ locator, e);
	    }
	}
    }

    /**
     * Set one combination value to the corresponding field of an automatable
     * parameters object.
     * 
     * @see #applyCombination(Map, Automatable)
     * @param locator
     * @param automationValue
     * @param params
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    private static void applyCombinationValue(AutoDefinition.FieldLocator locator,
	    Object automationValue, Params params)
	    throws NoSuchFieldException, SecurityException,
	    IllegalArgumentException, IllegalAccessException {
	Class<? extends ParamDefinition> targetClass = locator.getClazz();
	Field targetField = targetClass
		.getDeclaredField(locator.getFieldName());

	// check for exclusion
	if (targetField.getAnnotation(NotAutomatable.class) != null) {
	    throw new NotAutomatable.IllegalAutomationException(locator
		    + ": automation not allowed, annotated with @"
		    + NotAutomatable.class.getSimpleName() + ".");
	}

	// ensure field is accessible, private fields are not by default
	targetField.setAccessible(true);

	// traverse all objects of locator's class
	for (ParamDefinition definition : params.getDefinitions(targetClass)) {
	    // only continue if title matches
	    if (locator.getObjectTitle() != null
		    && !definition.getTitle().equals(locator.getObjectTitle())) {
		continue;
	    }
	    // set automation value to field
	    targetField.set(definition, automationValue);
	}
    }
}

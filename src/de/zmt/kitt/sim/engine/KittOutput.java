package de.zmt.kitt.sim.engine;

import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import de.zmt.io.CsvWriter;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.kitt.util.*;
import de.zmt.sim.engine.params.def.ParameterDefinition;
import de.zmt.sim.portrayal.inspector.CombinedInspector;

/**
 * Provides continuous output within the GUI via {@link Inspector} and file.
 * 
 * @author cmeyer
 * 
 */
// TODO file output
public class KittOutput implements Steppable, ProvidesInspector, Proxiable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(KittOutput.class
	    .getName());

    private final Environment environment;
    private final CsvWriter writer;
    private final StayDurations stayDurations = new StayDurations();

    public KittOutput(Environment environment, String outputPath) {
	this.environment = environment;
	writer = createWriter(outputPath);
    }

    private CsvWriter createWriter(String path) {
	// TODO
	return null;
    }

    @Override
    public void step(SimState state) {
	KittSim sim = (KittSim) state;

	accumulateHabitatStayDurations();

	if (writer == null) {
	    return;
	}

	EnvironmentDefinition def = sim.getParams().getEnvironmentDefinition();
	long steps = sim.schedule.getSteps();
	if (steps % def.getOutputAgeInterval() == 0) {

	}

	if (steps % def.getOutputPopulationInterval() == 0) {

	}
    }

    /** Accumulate habitat stay durations for all agents. */
    private void accumulateHabitatStayDurations() {
	Bag agents = environment.getAgents();

	for (Object obj : agents) {
	    if (!(obj instanceof Agent)) {
		continue;
	    }

	    Agent agent = (Agent) obj;
	    Habitat habitat = environment.obtainHabitat(agent.getPosition());
	    stayDurations.registerStay(agent.getDefinition(), habitat);
	}
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name),
		Inspector.getInspector(environment, state,
			Environment.class.getSimpleName()));
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Map<ParameterDefinition, Map<Habitat, Long>> getStayDurations() {
	    return stayDurations.getPrimitiveMap();
	}

	public String nameStayDurations() {
	    return "StayDurations_" + UnitConstants.SIMULATION_TIME;
	}
    }

    /**
     * Accumulates habitat stay durations by agent class.
     * 
     * @see de.zmt.kitt.sim.engine.KittOutput.StayDurations.HabitatStayDurations
     * @author cmeyer
     * 
     */
    private static class StayDurations implements Serializable,
	    MaintainsPrimitiveMap<Map<ParameterDefinition, Map<Habitat, Long>>> {
	private static final long serialVersionUID = 1L;
	/** Stay durations accumulated for every agent class. */

	private final Map<ParameterDefinition, HabitatStayDurations> map = new HashMap<ParameterDefinition, HabitatStayDurations>();
	private final Map<ParameterDefinition, Map<Habitat, Long>> primitiveMap = new HashMap<ParameterDefinition, Map<Habitat, Long>>();

	public void registerStay(ParameterDefinition agentClassDef,
		Habitat habitat) {
	    HabitatStayDurations stayDurations = map.get(agentClassDef);

	    if (stayDurations == null) {
		stayDurations = new HabitatStayDurations();
	    }

	    stayDurations.registerStay(habitat);
	    map.put(agentClassDef, stayDurations);
	    primitiveMap.put(agentClassDef, stayDurations.getPrimitiveMap());
	}

	/**
	 * 
	 * @param agentClassDef
	 * @param habitat
	 * @return accumulated stay duration of agent class with
	 *         {@code agentClassDef} in {@code habitat}
	 */
	@SuppressWarnings("unused")
	// TODO use in file output
	public Amount<Duration> getStayDuration(
		ParameterDefinition agentClassDef, Habitat habitat) {
	    HabitatStayDurations inner = map.get(agentClassDef);

	    if (inner == null) {
		return AmountUtil.zero(UnitConstants.SIMULATION_TIME);
	    } else {
		return inner.getStayDuration(habitat);
	    }
	}

	@Override
	public Map<ParameterDefinition, Map<Habitat, Long>> getPrimitiveMap() {
	    return primitiveMap;
	}

	/**
	 * Accumulates habitat stay durations for one agent class.
	 * 
	 * @author cmeyer
	 * 
	 */
	private static class HabitatStayDurations implements Serializable,
		MaintainsPrimitiveMap<Map<Habitat, Long>> {
	    private static final long serialVersionUID = 1L;
	    private static final int HABITAT_COUNT = Habitat.values().length;

	    private final Map<Habitat, Amount<Duration>> map = new HashMap<Habitat, Amount<Duration>>(
		    HABITAT_COUNT);
	    private final Map<Habitat, Long> primitiveMap = new HashMap<Habitat, Long>(
		    HABITAT_COUNT);

	    /**
	     * Register stay for given habitat.
	     * 
	     * @param habitat
	     */
	    public void registerStay(Habitat habitat) {
		Amount<Duration> stepDuration = EnvironmentDefinition.STEP_DURATION;
		Amount<Duration> oldDuration = map.get(habitat);
		Amount<Duration> newDuration = oldDuration == null ? stepDuration
			: oldDuration.plus(stepDuration);
		map.put(habitat, newDuration);
		primitiveMap.put(habitat, newDuration.getExactValue());
	    }

	    /**
	     * 
	     * @param habitat
	     * @return accumulated stay duration in {@code habitat}
	     */
	    public Amount<Duration> getStayDuration(Habitat habitat) {
		Amount<Duration> stayDuration = map.get(habitat);

		if (stayDuration == null) {
		    return AmountUtil.zero(UnitConstants.SIMULATION_TIME);
		} else {
		    return stayDuration;
		}
	    }

	    /**
	     * 
	     * @return {@link Map} with exact duration values as {@link Long} to
	     *         be well-inspected and charted in the MASON GUI.
	     */
	    @Override
	    public Map<Habitat, Long> getPrimitiveMap() {
		return primitiveMap;
	    }
	}
    }

    /**
     * Class maintains a map that contains primitive values to be used instead
     * of {@link Amount}s in MASON GUI.
     * 
     * @author cmeyer
     * 
     * @param <M>
     */
    private static interface MaintainsPrimitiveMap<M extends Map<?, ?>> {
	/**
	 * 
	 * @return {@link Map} with primitive values that can be displayed /
	 *         charted in MASON GUI.
	 */
	M getPrimitiveMap();
    }
}

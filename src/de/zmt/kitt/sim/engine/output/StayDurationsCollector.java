package de.zmt.kitt.sim.engine.output;

import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.sim.Habitat;
import de.zmt.kitt.sim.engine.agent.Agent;
import de.zmt.kitt.sim.engine.output.StayDurationsCollector.HabitatStayDurations;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.kitt.util.*;
import de.zmt.sim.engine.params.def.ParameterDefinition;

/**
 * Accumulates habitat stay durations by agent class.
 * 
 * @see de.zmt.kitt.sim.engine.KittOutput.StayDurations.HabitatStayDurations
 * @author cmeyer
 * 
 */
public class StayDurationsCollector extends
	EncapsulatedClearableMap<ParameterDefinition, HabitatStayDurations>
	implements Collector {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @param agentClassDef
     * @param habitat
     * @return accumulated stay duration of agent class with
     *         {@code agentClassDef} in {@code habitat}
     */
    @SuppressWarnings("unused")
    // TODO use in file output
    public Amount<Duration> getStayDuration(ParameterDefinition agentClassDef,
	    Habitat habitat) {
	HabitatStayDurations inner = map.get(agentClassDef);

	if (inner == null) {
	    return AmountUtil.zero(UnitConstants.SIMULATION_TIME);
	} else {
	    return inner.getStayDuration(habitat);
	}
    }

    @Override
    public void collect(Agent agent, Object message) {
	ParameterDefinition agentClassDef = agent.getDefinition();
	HabitatStayDurations stayDurations = map.get(agentClassDef);

	if (stayDurations == null) {
	    stayDurations = new HabitatStayDurations();
	}

	stayDurations
		.registerStay(((StayDurationsCollector.HabitatMessage) message).habitat);
	map.put(agentClassDef, stayDurations);
    }

    /**
     * Accumulates habitat stay durations for one agent class.
     * 
     * @author cmeyer
     * 
     */
    public static class HabitatStayDurations extends
	    EncapsulatedMap<Habitat, Long> implements Clearable {
	private static final long serialVersionUID = 1L;

	private static final Habitat[] HABITATS = Habitat.values();

	private final Map<Habitat, Amount<Duration>> amountMap = new HashMap<Habitat, Amount<Duration>>(
		HABITATS.length);

	private HabitatStayDurations() {
	    super(HABITATS.length);

	    // initialize maps with zero durations
	    clear();
	}

	/**
	 * Register stay for given habitat.
	 * 
	 * @param habitat
	 */
	public void registerStay(Habitat habitat) {
	    Amount<Duration> stepDuration = EnvironmentDefinition.STEP_DURATION;
	    Amount<Duration> oldDuration = amountMap.get(habitat);
	    Amount<Duration> newDuration = oldDuration.plus(stepDuration);
	    amountMap.put(habitat, newDuration);
	    map.put(habitat, newDuration.getExactValue());
	}

	/**
	 * 
	 * @param habitat
	 * @return accumulated stay duration in {@code habitat}
	 */
	public Amount<Duration> getStayDuration(Habitat habitat) {
	    Amount<Duration> stayDuration = amountMap.get(habitat);

	    if (stayDuration == null) {
		return AmountUtil.zero(UnitConstants.SIMULATION_TIME);
	    } else {
		return stayDuration;
	    }
	}

	/** Fill maps with zero durations. */
	@Override
	public void clear() {
	    for (Habitat habitat : HABITATS) {
		amountMap.put(habitat,
			AmountUtil.zero(UnitConstants.SIMULATION_TIME));
		map.put(habitat, 0l);
	    }
	}
    }

    public static class HabitatMessage {
	public final Habitat habitat;

	public HabitatMessage(Habitat habitat) {
	    this.habitat = habitat;
	}
    }
}
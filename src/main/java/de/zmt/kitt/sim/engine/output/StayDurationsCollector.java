package de.zmt.kitt.sim.engine.output;

import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.kitt.sim.Habitat;
import de.zmt.kitt.sim.engine.output.StayDurationsCollector.HabitatStayDurations;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.util.*;
import de.zmt.sim.engine.output.*;
import de.zmt.sim.engine.params.def.ParamDefinition;
import de.zmt.util.AmountUtil;

/**
 * Accumulates habitat stay durations by agent class.
 * 
 * @see de.zmt.kitt.sim.engine.KittOutput.StayDurations.HabitatStayDurations
 * @author cmeyer
 * 
 */
public class StayDurationsCollector extends
	AbstractCollector<ParamDefinition, HabitatStayDurations> {
    private static final long serialVersionUID = 1L;

    public StayDurationsCollector(
	    Collection<? extends ParamDefinition> agentClassDefs) {
	super(agentClassDefs);
    }

    @Override
    public void collect(CollectMessage message) {
	SpeciesDefinition definition = message.getAgent().get(
		SpeciesDefinition.class);

	if (definition == null) {
	    return;
	}

	HabitatStayDurations stayDurations = map.get(definition);

	if (stayDurations == null) {
	    stayDurations = new HabitatStayDurations();
	}

	stayDurations.registerStay(((HabitatMessage) message).getHabitat());
	map.put(definition, stayDurations);
    }

    @Override
    protected int getColumnCount() {
	return map.size() * HabitatStayDurations.HEADERS.length;
    }

    @Override
    protected HabitatStayDurations createCollectable(ParamDefinition definition) {
	return new HabitatStayDurations();
    }

    /**
     * Accumulates habitat stay durations for one agent class.
     * 
     * @author cmeyer
     * 
     */
    public static class HabitatStayDurations extends AbstractCollectable<Long> {
	private static final long serialVersionUID = 1L;

	private static final Habitat[] HABITATS = Habitat.values();
	private static final String HEADER_FORMAT_STRING = "%s_stay_"
		+ UnitConstants.SIMULATION_TIME;
	private static final String[] HEADERS = new String[HABITATS.length];

	{
	    // generate header names from format string
	    for (Habitat habitat : HABITATS) {
		HEADERS[habitat.ordinal()] = String.format(
			HEADER_FORMAT_STRING, habitat);
	    }
	}

	private final List<Amount<Duration>> amounts = new ArrayList<>(
		Collections.nCopies(HABITATS.length, (Amount<Duration>) null));

	private HabitatStayDurations() {
	    super(new ArrayList<Long>(Collections.nCopies(HABITATS.length,
		    (Long) null)));
	    clear();
	}

	/**
	 * Register stay for given habitat.
	 * 
	 * @param habitat
	 */
	public void registerStay(Habitat habitat) {
	    Amount<Duration> stepDuration = EnvironmentDefinition.STEP_DURATION;
	    int index = habitat.ordinal();
	    Amount<Duration> oldDuration = amounts.get(index);

	    Amount<Duration> newDuration = oldDuration.plus(stepDuration);
	    amounts.set(index, newDuration);
	    data.set(index, newDuration.getExactValue());
	}

	/** Fill maps with zero durations. */
	@Override
	public void clear() {
	    super.clear();

	    Collections.fill(amounts,
		    AmountUtil.zero(UnitConstants.SIMULATION_TIME));
	}

	@Override
	public List<String> obtainHeaders() {
	    return Arrays.asList(HEADERS);
	}

	@Override
	protected Long obtainInitialValue() {
	    return 0l;
	}
    }

    public static interface HabitatMessage extends CollectMessage {
	Habitat getHabitat();
    }
}
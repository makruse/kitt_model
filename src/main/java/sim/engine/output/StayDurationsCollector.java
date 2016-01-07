package sim.engine.output;

import java.util.*;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.*;
import de.zmt.util.*;
import sim.engine.*;
import sim.engine.output.StayDurationsCollector.HabitatStayDurations;
import sim.engine.output.message.*;
import sim.engine.params.def.ParamDefinition;
import sim.params.def.*;
import sim.util.Double2D;

/**
 * Accumulates habitat stay durations for every species.
 * 
 * @see HabitatStayDurations
 * @see SpeciesDefinition
 * @author mey
 * 
 */
class StayDurationsCollector extends CategoryCollector<ParamDefinition, HabitatStayDurations, Long>
	implements CreatesCollectMessages {
    private static final long serialVersionUID = 1L;

    public StayDurationsCollector(Set<? extends ParamDefinition> agentClassDefs) {
	super(agentClassDefs);
    }

    @Override
    public void collect(CollectMessage message) {
	SpeciesDefinition definition = ((HabitatMessage) message).getEntity().get(SpeciesDefinition.class);

	if (definition == null) {
	    return;
	}

	HabitatStayDurations stayDurations = getCollectable(definition);

	if (stayDurations == null) {
	    stayDurations = new HabitatStayDurations();
	}

	stayDurations.registerStay(((HabitatMessage) message).getHabitat());
    }

    @Override
    protected HabitatStayDurations createCollectable(ParamDefinition definition) {
	return new HabitatStayDurations();
    }

    @Override
    public Iterable<HabitatMessage> createCollectMessages(final SimState state,
	    Iterable<? extends CollectMessage> defaultMessages) {
	final Iterator<? extends CollectMessage> iterator = defaultMessages.iterator();
	return new Iterable<HabitatMessage>() {

	    @Override
	    public Iterator<HabitatMessage> iterator() {
		return new Iterator<HabitatMessage>() {

		    @Override
		    public HabitatMessage next() {
			return createHabitatMessage(((EntityCollectMessage) iterator.next()).getSimObject(),
				((Kitt) state).getEnvironment());
		    }

		    @Override
		    public boolean hasNext() {
			return iterator.hasNext();
		    }
		};
	    }

	};
    }

    /**
     * Creates a {@link HabitatMessage} with the given data.
     * 
     * @param agent
     * @param environment
     * @return {@link HabitatMessage} with given data
     */
    private static HabitatMessage createHabitatMessage(final Entity agent, final Entity environment) {
	return new StayDurationsCollector.HabitatMessage() {

	    @Override
	    public Entity getEntity() {
		return agent;
	    }

	    @Override
	    public Habitat getHabitat() {
		Double2D position = agent.get(Moving.class).getPosition();
		HabitatMap habitatMap = environment.get(HabitatMap.class);
		WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
		return habitatMap.obtainHabitat(position, converter);
	    }
	};
    }

    /**
     * Accumulates the stay durations for every habitat.
     * 
     * @author mey
     * 
     */
    // TODO make enum collectable
    static class HabitatStayDurations extends AbstractCollectable<Long> {
	private static final long serialVersionUID = 1L;

	private static final Habitat[] HABITATS = Habitat.values();
	private static final String HEADER_FORMAT_STRING = "%s_stay_" + UnitConstants.SIMULATION_TIME;
	private static final String[] HEADERS = new String[HABITATS.length];

	{
	    // generate header names from format string
	    for (Habitat habitat : HABITATS) {
		HEADERS[habitat.ordinal()] = String.format(HEADER_FORMAT_STRING, habitat);
	    }
	}

	private final List<Amount<Duration>> amounts = new ArrayList<>(
		Collections.nCopies(HABITATS.length, (Amount<Duration>) null));

	private HabitatStayDurations() {
	    super(new ArrayList<>(Collections.nCopies(HABITATS.length, (Long) null)));
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
	    getValues().set(index, newDuration.getExactValue());
	}

	/** Fill maps with zero durations. */
	@Override
	public void clear() {
	    super.clear();

	    Collections.fill(amounts, AmountUtil.zero(UnitConstants.SIMULATION_TIME));
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
	Entity getEntity();

	Habitat getHabitat();
    }
}
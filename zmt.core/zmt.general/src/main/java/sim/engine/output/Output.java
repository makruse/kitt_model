package sim.engine.output;

import java.io.*;
import java.util.*;

import sim.display.GUIState;
import sim.engine.*;
import sim.engine.output.message.*;
import sim.portrayal.*;
import sim.portrayal.inspector.*;
import sim.util.Propertied;
import sim.util.Properties;

/**
 * Class for organizing simulation output.
 * <p>
 * All added {@link Collector}s are processed every step or in their associated
 * interval. Each of them will go through a data collection cycle sending the
 * appropriate messages. Those messages can either be created by the collector
 * itself if it implements the related interface or a default message is sent.
 * <p>
 * This class also implements {@link ProvidesInspector} to provide an
 * {@link Inspector} which displays each added {@code Collector} together with
 * other objects to be inspected that can be added separately.
 * <p>
 * <b>NOTE:</b> Already provided inspectors are not updated with collectors /
 * attachments added later.
 * 
 * @see CreatesBeforeMessage
 * @see CreatesCollectMessages
 * @see CreatesAfterMessage
 * 
 * @author mey
 *
 */
public class Output implements Steppable, ProvidesInspector, Propertied, Closeable {
    private static final long serialVersionUID = 1L;

    private final List<Collector> collectors = new ArrayList<>();
    private final Map<Collector, Integer> intervals = new HashMap<>();

    /**
     * Adds {@code collector} without associating it with an interval, i.e. it
     * its data is collected on every step.
     * 
     * @param collector
     *            the collector to be added
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addCollector(Collector collector) {
	return collectors.add(collector);
    }

    /**
     * Adds {@code collector} and associates it with {@code interval}.
     * 
     * @param collector
     *            the collector to be added
     * @param stepInterval
     *            the step interval {@code collector} is associated with
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addCollector(Collector collector, int stepInterval) {
	if (stepInterval <= 0) {
	    throw new IllegalArgumentException("Step intervals must be greater than zero.");
	}

	if (addCollector(collector)) {
	    intervals.put(collector, stepInterval);
	    return true;
	}
	return false;
    }

    @Override
    public final void step(SimState state) {
	for (Collector collector : collectors) {
	    // only perform collection in intervals, if there is one set
	    if (intervals.containsKey(collector) && state.schedule.getSteps() % intervals.get(collector) != 0) {
		continue;
	    }

	    collector.beforeCollect(createBeforeMessage(collector, state));

	    for (CollectMessage message : createCollectMessages(collector, state)) {
		collector.collect(message);
	    }

	    collector.afterCollect(createAfterMessage(collector, state));
	}
    }

    /**
     * Creates a {@link BeforeMessage} sent to the recipient collector. If
     * recipient implements {@link CreatesBeforeMessage}, the created message is
     * returned, otherwise {@link #createBeforeMessage(Collector, SimState)}
     * will be used.
     * 
     * @param recipient
     * @param state
     * @return {@link BeforeMessage}
     */
    private BeforeMessage createBeforeMessage(Collector recipient, SimState state) {
	BeforeMessage defaultMessage = createDefaultBeforeMessage(state);
	if (recipient instanceof CreatesBeforeMessage) {
	    return ((CreatesBeforeMessage) recipient).createBeforeMessage(state, defaultMessage);
	}
	return defaultMessage;
    }

    /**
     * Creates a {@link CollectMessage} iterable. Each created message will be
     * sent to the recipient between {@link BeforeMessage} and
     * {@link AfterMessage}. If recipient implements
     * {@link CreatesCollectMessages}, the created messages are returned,
     * otherwise {@link #createDefaultCollectMessages(SimState)} is used.
     * 
     * @param recipient
     * @param state
     * @return {@link CollectMessage} iterable
     */
    private Iterable<? extends CollectMessage> createCollectMessages(Collector recipient, SimState state) {
	Iterable<? extends CollectMessage> defaultMessages = createDefaultCollectMessages(state);
	if (recipient instanceof CreatesCollectMessages) {
	    return ((CreatesCollectMessages) recipient).createCollectMessages(state, defaultMessages);
	}
	return defaultMessages;
    }

    /**
     * Creates an {@link AfterMessage} sent to the recipient collector. The
     * default message only include the step number. If recipient implements
     * {@link CreatesAfterMessage}, the created message is returned, otherwise
     * {@link #createDefaultAfterMessage(SimState)} is used.
     * 
     * @param recipient
     * @param state
     * @return {@link AfterMessage}
     */
    private AfterMessage createAfterMessage(Collector recipient, SimState state) {
	AfterMessage defaultMessage = createDefaultAfterMessage(state);
	if (recipient instanceof CreatesAfterMessage) {
	    return ((CreatesAfterMessage) recipient).createAfterMessage(state, defaultMessage);
	}
	return defaultMessage;
    }

    protected BeforeMessage createDefaultBeforeMessage(SimState state) {
	return new BeforeMessage() {
	};
    }

    protected Iterable<? extends CollectMessage> createDefaultCollectMessages(SimState state) {
	return Collections.singleton(new DefaultCollectMessage(state));
    }

    protected AfterMessage createDefaultAfterMessage(SimState state) {
	return new DefaultAfterMessage(state.schedule.getSteps());
    }

    @Override
    public Properties properties() {
	return new MyProperties();
    }

    /** {@link CombinedInspector} displaying collectors. */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new CombinedInspector(new SimpleInspector(this, state, name));
    }

    @Override
    public void close() throws IOException {
	for (Collector collector : collectors) {
	    if (collector instanceof Closeable) {
		((Closeable) collector).close();
	    }
	}
    }

    /**
     * Properties consisting of collectors with names obtained from their
     * classes.
     * 
     * @author mey
     * 
     */
    public class MyProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isVolatile() {
	    return false;
	}

	@Override
	public int numProperties() {
	    return collectors.size();
	}

	@Override
	public Object getValue(int index) {
	    return unwrapIfNecessary(index);
	}

	@Override
	public boolean isReadWrite(int index) {
	    return false;
	}

	@Override
	public String getName(int index) {
	    return unwrapIfNecessary(index).getClass().getSimpleName();
	}

	@Override
	public Class<?> getType(int index) {
	    return collectors.get(index).getClass();
	}

	@Override
	protected Object _setValue(int index, Object value) {
	    throw new UnsupportedOperationException("access is read-only");
	}

	/**
	 * Unwraps the collector at given index if it is a
	 * {@link WritingCollector}.
	 * 
	 * @param index
	 * @return unwrapped collector
	 */
	private Collector unwrapIfNecessary(int index) {
	    Collector collector = collectors.get(index);
	    // unwrap the writing collector
	    if (collector instanceof WritingCollector) {
		return ((WritingCollector) collector).getCollector();
	    }
	    return collector;
	}
    }
}

package sim.engine.output;

import java.io.*;
import java.util.*;

import sim.display.GUIState;
import sim.engine.*;
import sim.engine.output.Collector.*;
import sim.portrayal.*;
import sim.portrayal.inspector.*;
import sim.util.Propertied;
import sim.util.Properties;

/**
 * Abstract class for organizing simulation output. All added {@link Collector}s
 * are processed every step, or in their interval, if associated.
 * <p>
 * This class also implements {@link ProvidesInspector} to provide an
 * {@link Inspector} which displays each added {@code Collector} together with
 * other objects to be inspected that can be added separately.
 * <p>
 * <b>NOTE:</b> Already provided inspectors are not updated with collectors /
 * attachments added later.
 * 
 * @author mey
 *
 */
public abstract class Output implements Steppable, ProvidesInspector, Propertied, Closeable {
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
     * @param interval
     *            the interval {@code collector} is associated with
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addCollector(Collector collector, int interval) {
	if (addCollector(collector)) {
	    intervals.put(collector, interval);
	    return true;
	}
	return false;
    }

    @Override
    public void step(SimState state) {
	for (Collector collector : collectors) {
	    // only perform collection in intervals, if there is one set
	    if (intervals.containsKey(collector) && state.schedule.getSteps() % intervals.get(collector) != 0) {
		continue;
	    }

	    collector.beforeCollect(obtainBeforeMessage(collector, state));

	    for (Object simObject : obtainSimObject()) {
		collector.collect(obtainCollectMessage(collector, simObject, state));
	    }

	    collector.afterCollect(obtainAfterMessage(collector, state));
	}

    }

    /**
     * Implementing classes need to provide the simulation objects to collect
     * data from.
     * 
     * @return the simulation objects to collect data from
     */
    protected abstract Iterable<?> obtainSimObject();

    /**
     * Override to send {@link BeforeMessage} to given {@link Collector}.
     * Otherwise an empty message is returned.
     * 
     * @param recipient
     * @param state
     * @return {@link BeforeMessage}
     */
    protected BeforeMessage obtainBeforeMessage(Collector recipient, SimState state) {
	return new BeforeMessage() {
	};
    }

    /**
     * Override to send {@link CollectMessage} to given {@link Collector}.
     * Otherwise null is returned.
     * 
     * @param recipient
     * @param simObject
     * @param state
     * @return {@link CollectMessage} or null
     */
    protected CollectMessage obtainCollectMessage(Collector recipient, final Object simObject, SimState state) {
	return new CollectMessage() {

	    @Override
	    public Object getSimObject() {
		return simObject;
	    }
	};
    }

    /**
     * Override to send {@link AfterMessage} to given {@link Collector}.
     * Otherwise null is returned.
     * 
     * @param recipient
     * @param state
     * @return {@link AfterMessage} or null
     */
    protected AfterMessage obtainAfterMessage(Collector recipient, final SimState state) {
	return new AfterMessage() {

	    @Override
	    public long getSteps() {
		return state.schedule.getSteps();
	    }
	};
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
	    return collectors.get(index);
	}

	@Override
	public boolean isReadWrite(int index) {
	    return false;
	}

	@Override
	public String getName(int index) {
	    return collectors.get(index).getClass().getSimpleName();
	}

	@Override
	public Class<?> getType(int index) {
	    return collectors.get(index).getClass();
	}

	@Override
	protected Object _setValue(int index, Object value) {
	    throw new UnsupportedOperationException("access is read-only");
	}
    }
}

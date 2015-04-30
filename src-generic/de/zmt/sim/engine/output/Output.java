package de.zmt.sim.engine.output;

import java.io.*;
import java.util.*;

import sim.display.GUIState;
import sim.engine.*;
import sim.portrayal.*;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.*;
import sim.util.Properties;
import de.zmt.sim.engine.ParamAgent;
import de.zmt.sim.engine.output.Collector.AfterMessage;
import de.zmt.sim.engine.output.Collector.BeforeMessage;
import de.zmt.sim.engine.output.Collector.CollectMessage;
import de.zmt.sim.portrayal.inspector.CombinedInspector;

public abstract class Output implements Steppable, ProvidesInspector,
	Propertied, Closeable {
    private static final long serialVersionUID = 1L;

    private final List<Collector> collectors;
    private final Collection<?> inspectorAttachments;
    private final Map<Collector, Integer> intervals;

    public Output(List<Collector> collectors,
	    Collection<?> inspectorAttachments,
	    Map<Collector, Integer> intervals) {
	this.collectors = collectors;
	this.inspectorAttachments = inspectorAttachments;
	this.intervals = intervals;
    }

    @Override
    public void step(SimState state) {
	for (Collector collector : collectors) {
	    // only perform collection in intervals, if there is one set
	    if (intervals.containsKey(collector)
		    && state.schedule.getSteps() % intervals.get(collector) != 0) {
		continue;
	    }

	    collector.beforeCollect(obtainBeforeMessage(collector, state));

	    for (Object obj : obtainAgents()) {
		if (!(obj instanceof ParamAgent)) {
		    continue;
		}

		ParamAgent agent = (ParamAgent) obj;
		collector
			.collect(obtainCollectMessage(collector, agent, state));
	    }

	    collector.afterCollect(obtainAfterMessage(collector, state));
	}

    }

    protected abstract Collection<?> obtainAgents();

    /**
     * Override to send {@link BeforeMessage} to given {@link Collector}.
     * Otherwise null is returned.
     * 
     * @param recipient
     * @param state
     * @return {@link BeforeMessage} or null
     */
    protected BeforeMessage obtainBeforeMessage(Collector recipient,
	    SimState state) {
	return null;
    }

    /**
     * Override to send {@link CollectMessage} to given {@link Collector}.
     * Otherwise null is returned.
     * 
     * @param recipient
     * @param agent
     * @param state
     * @return {@link CollectMessage} or null
     */
    protected CollectMessage obtainCollectMessage(Collector recipient,
	    final ParamAgent agent, SimState state) {
	return new CollectMessage() {

	    @Override
	    public ParamAgent getAgent() {
		return agent;
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
    protected AfterMessage obtainAfterMessage(Collector recipient,
	    SimState state) {
	return null;
    }

    @Override
    public Properties properties() {
	return new MyProperties();
    }

    /**
     * Combined inspector displaying collectors first and
     * {@link #inspectorAttachments} thereafter.
     */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Collection<Inspector> inspectors = new ArrayList<>(
		1 + inspectorAttachments.size());
	inspectors.add(new SimpleInspector(this, state, name));

	for (Object obj : inspectorAttachments) {
	    inspectors.add(Inspector.getInspector(obj, state, obj.getClass()
		    .getSimpleName()));
	}
	return new CombinedInspector(inspectors);
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
     * @author cmeyer
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

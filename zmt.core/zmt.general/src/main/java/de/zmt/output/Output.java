package de.zmt.output;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.zmt.output.message.AfterMessage;
import de.zmt.output.message.BeforeMessage;
import de.zmt.output.message.CollectMessage;
import de.zmt.output.message.CreatesAfterMessage;
import de.zmt.output.message.CreatesBeforeMessage;
import de.zmt.output.message.CreatesCollectMessages;
import de.zmt.output.message.DefaultAfterMessage;
import de.zmt.output.message.DefaultCollectMessage;
import de.zmt.output.writing.CollectorWriter;
import de.zmt.output.writing.CollectorWriterFactory;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.ZmtSimState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Propertied;
import sim.util.Properties;

/**
 * Class for organizing simulation output.
 * <p>
 * All added {@link Collector}s are processed every step or in their associated
 * interval, as well as the {@link CollectorWriter} referring to a collector
 * with an associated interval. Each of them will go through a data collection
 * cycle sending the appropriate messages. Those messages can either be created
 * by the collector itself if it implements the related interface or a default
 * message is sent.
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
public class Output implements Steppable, Propertied, Closeable {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Output.class.getName());
    private static final long serialVersionUID = 1L;

    /** Separator in output file names between different parts. */
    private static final String FILENAME_SEPARATOR = "_";
    /** Number of digits used in output file names for numbers. */
    private static final int DIGITS_COUNT = 5;
    /** Format string for numbers in file names. */
    private static final String NUMBER_FORMAT_STRING = "%0" + DIGITS_COUNT + "d";

    /** {@link Collector} list. Combined display in inspector. */
    private final List<Collector<?>> collectors = new ArrayList<>();
    /** Writers for collectors. */
    private final Collection<CollectorWriter> writers = new ArrayList<>();
    /** Step intervals for collectors. Default is to collect on each step. */
    private final Map<Collector<?>, Integer> intervals = new HashMap<>();
    /** Factories of type {@link CreatesBeforeMessage} for collectors. */
    private final Map<Collector<?>, CreatesBeforeMessage> beforeMessageFactories = new HashMap<>();
    /** Factories of type {@link CreatesCollectMessages} for collectors. */
    private final Map<Collector<?>, CreatesCollectMessages> collectMessageFactories = new HashMap<>();
    /** Factories of type {@link CreatesAfterMessage} for collectors. */
    private final Map<Collector<?>, CreatesAfterMessage> afterMessageFactories = new HashMap<>();

    /** Directory where the output gets written to. */
    private final Path outputPath;

    /**
     * Constructs a new {@code Output} instance without creating a directory.
     * Use only for testing.
     */
    Output() {
	outputPath = ZmtSimState.DEFAULT_OUTPUT_DIR;
    }

    /**
     * Constructs a new {@code Output} instance. Creates a directory at given
     * path.
     * 
     * @param outputPath
     *            an empty directory where the output gets written to
     */
    public Output(Path outputPath) {
	try {
	    Files.createDirectories(outputPath);
	} catch (IOException e) {
	    throw new RuntimeException("Unable to create directory " + outputPath, e);
	}

	this.outputPath = outputPath;
    }

    /**
     * Generates a file name with each given string separated from the other.
     * 
     * @param first
     *            the first part
     * @param other
     *            the other parts
     * @return generated file name from parts
     */
    public static String generateFileName(String first, String... other) {
	String result = first;

	for (String part : other) {
	    result += FILENAME_SEPARATOR + part;
	}

	return result;
    }

    /**
     * Generates a file name from given string and an index number.
     * 
     * @param index
     *            the index number, added last
     * @param first
     *            the first part
     * @param other
     *            the other parts added after the first part
     * @return generated file name
     */
    public static String generateFileName(int index, String first, String... other) {
	return generateFileName(generateFileName(first, other), formatFileIndex(index));
    }

    /**
     * Formats an index number to be used in a file name.
     * 
     * @param index
     *            the index number
     * @return {@code String} with formatted index number
     */
    private static String formatFileIndex(int index) {
	return String.format(NUMBER_FORMAT_STRING, index);
    }

    /**
     * Adds {@code collector}. If the collector implements a message creation
     * interface, it will create its own messages of that type.
     * 
     * @param collector
     *            the collector to be added
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addCollector(Collector<?> collector) {
	if (collectors.add(collector)) {
	    if (collector instanceof CreatesBeforeMessage) {
		putFactory(collector, (CreatesBeforeMessage) collector);
	    }
	    if (collector instanceof CreatesCollectMessages) {
		putFactory(collector, (CreatesCollectMessages) collector);
	    }
	    if (collector instanceof CreatesAfterMessage) {
		putFactory(collector, (CreatesAfterMessage) collector);
	    }
	    return true;
	}
	return false;
    }

    /**
     * Adds {@code collector} and a writer.
     * 
     * @see CollectorWriterFactory#create(Collector, Path)
     * @param collector
     *            the collector to be added
     * @param dataTitle
     *            the title used in the name of the file created for writing the
     *            data
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addCollectorAndWriter(Collector<?> collector, String dataTitle) {
	return addCollector(collector)
		&& addWriter(CollectorWriterFactory.create(collector, outputPath.resolve(dataTitle)));
    }

    /**
     * Adds a {@link CollectorWriter}.
     * 
     * @param writer
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean addWriter(CollectorWriter writer) {
	return writers.add(writer);
    }

    /**
     * Associates {@code collector} with {@code interval}. Collectors without
     * associated interval collect on every step.
     * 
     * @param collector
     *            the collector to be added
     * @param stepInterval
     *            the step interval {@code collector} is associated with
     * @return interval set previously for this {@code collector} or
     *         <code>null</code>
     */
    public Integer putInterval(Collector<?> collector, int stepInterval) {
	if (stepInterval <= 0) {
	    throw new IllegalArgumentException("Step intervals must be greater than zero.");
	}

	return intervals.put(collector, stepInterval);
    }

    /**
     * Associates {@code collector} with {@code factory}. The factory will then
     * be used to create {@link BeforeMessage} objects for the collector.
     * 
     * @param collector
     * @param factory
     *            the factory {@code collector} is associated with
     * @return {@link CreatesBeforeMessage} factory set previously for this
     *         {@code collector} or <code>null</code>
     */
    public CreatesBeforeMessage putFactory(Collector<?> collector, CreatesBeforeMessage factory) {
	return beforeMessageFactories.put(collector, factory);
    }

    /**
     * Associates {@code collector} with {@code factory}. The factory will then
     * be used to create {@link CollectMessage} objects for the collector.
     * 
     * @param collector
     * @param factory
     *            the factory {@code collector} is associated with
     * @return {@link CreatesCollectMessages} factory set previously for this
     *         {@code collector} or <code>null</code>
     */
    public CreatesCollectMessages putFactory(Collector<?> collector, CreatesCollectMessages factory) {
	return collectMessageFactories.put(collector, factory);
    }

    /**
     * Associates {@code collector} with {@code factory}. The factory will then
     * be used to create {@link AfterMessage} objects for the collector.
     * 
     * @param collector
     * @param factory
     *            the factory {@code collector} is associated with
     * @return {@link CreatesAfterMessage} factory set previously for this
     *         {@code collector} or <code>null</code>
     */
    public CreatesAfterMessage putFactory(Collector<?> collector, CreatesAfterMessage factory) {
	return afterMessageFactories.put(collector, factory);
    }

    @Override
    public final void step(SimState state) {
	long steps = state.schedule.getSteps();

	for (Collector<?> collector : collectors) {
	    if (!inTurn(collector, steps)) {
		continue;
	    }

	    collector.beforeCollect(createBeforeMessage(collector, state));

	    for (CollectMessage message : createCollectMessages(collector, state)) {
		collector.collect(message);
	    }

	    collector.afterCollect(createAfterMessage(collector, state));
	}

	for (CollectorWriter writer : writers) {
	    if (!inTurn(writer.getCollector(), steps)) {
		continue;
	    }

	    try {
		writer.writeValues(steps);
	    } catch (IOException e) {
		logger.log(Level.WARNING, "I/O error while writing data from " + writer, e);
	    }
	}
    }

    /**
     * Checks if given {@code collector} is between intervals and in turn to
     * collect this step.
     * 
     * @param collector
     * @param steps
     * @return <code>true</code> if collector is to collect this step
     */
    private boolean inTurn(Collector<?> collector, long steps) {
	// only perform collection in intervals, if there is one set
	Integer interval = intervals.get(collector);
	return interval == null || steps % interval == 0;
    }

    /**
     * Creates a {@link BeforeMessage} sent to the recipient collector. If a
     * factory of type {@code CreatesBeforeMessage} is associated with it, the
     * created message is returned, otherwise the message from
     * {@link #createDefaultBeforeMessage(SimState)} will be used.
     * 
     * @param recipient
     *            the collector that will receive the returned message
     * @param state
     *            the simulation state object
     * @return {@link BeforeMessage}
     */
    private BeforeMessage createBeforeMessage(Collector<?> recipient, SimState state) {
	BeforeMessage defaultMessage = createDefaultBeforeMessage(state);
	CreatesBeforeMessage beforeMessageFactory = beforeMessageFactories.get(recipient);
	if (beforeMessageFactory != null) {
	    return beforeMessageFactory.createBeforeMessage(state, defaultMessage);
	}
	return defaultMessage;
    }

    /**
     * Creates a {@link CollectMessage} iterable. Each created message will be
     * sent to the recipient between {@link BeforeMessage} and
     * {@link AfterMessage}. If the recipient is associated with a factory of
     * type {@link CreatesBeforeMessage}, the created messages are returned,
     * otherwise {@link #createDefaultCollectMessages(SimState)} is used.
     * 
     * @param recipient
     *            the collector that will receive the returned message
     * @param state
     *            the simulation state object
     * @return {@link CollectMessage} iterable
     */
    private Iterable<? extends CollectMessage> createCollectMessages(Collector<?> recipient, SimState state) {
	Iterable<? extends CollectMessage> defaultMessages = createDefaultCollectMessages(state);
	CreatesCollectMessages collectMessagesFactory = collectMessageFactories.get(recipient);
	if (collectMessagesFactory != null) {
	    return collectMessagesFactory.createCollectMessages(state, defaultMessages);
	}
	return defaultMessages;
    }

    /**
     * Creates an {@link AfterMessage} sent to the recipient collector. The
     * default message only include the step number. If the recipient is
     * associated with a factory of type {@link CreatesAfterMessage}, the
     * created message from there is returned, otherwise
     * {@link #createDefaultAfterMessage(SimState)} is used.
     * 
     * @param recipient
     *            the collector that will receive the returned message
     * @param state
     *            the simulation state object
     * @return {@link AfterMessage}
     */
    private AfterMessage createAfterMessage(Collector<?> recipient, SimState state) {
	AfterMessage defaultMessage = createDefaultAfterMessage(state);
	CreatesAfterMessage afterMessageFactory = afterMessageFactories.get(recipient);
	if (afterMessageFactory != null) {
	    return afterMessageFactory.createAfterMessage(state, defaultMessage);
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

    @Override
    public void close() throws IOException {
	for (Collector<?> collector : collectors) {
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

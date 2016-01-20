package sim.engine.output.writing;

import java.nio.file.Path;

import sim.engine.output.*;

/**
 * Factory class for creating {@link WritingCollector} instances and generating
 * file names for output.
 * 
 * @author mey
 *
 */
public final class WritingCollectorFactory {

    private WritingCollectorFactory() {

    }

    /**
     * Wraps given collector in a fitting {@link WritingCollector}.
     * 
     * @param collector
     *            the collector which data needs to be written
     * @param outputPath
     *            the output path, if several are needed the given one acts a
     *            base and is extended
     * @return a {@link WritingCollector} around given collector
     */
    public static <T extends Collectable<?>> WritingCollector<T> wrap(Collector<T> collector, Path outputPath) {
	if (collector instanceof WritingCollector) {
	    throw new IllegalArgumentException(
		    collector + " is already a " + WritingCollector.class.getSimpleName() + ".");
	}
	if (collector.getCollectable() instanceof OneShotCollectable) {
	    return new OneShotWritingCollector<>(collector, outputPath);
	}
	return new LineWritingCollector<>(collector, outputPath);
    }
}

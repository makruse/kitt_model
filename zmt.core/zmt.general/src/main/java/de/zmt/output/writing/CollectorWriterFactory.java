package de.zmt.output.writing;

import java.nio.file.Path;

import de.zmt.output.Collectable;
import de.zmt.output.Collector;
import de.zmt.output.OneShotCollectable;

/**
 * Factory class for creating {@link CollectorWriter} instances and generating
 * file names for output.
 * 
 * @author mey
 *
 */
public final class CollectorWriterFactory {

    private CollectorWriterFactory() {

    }

    /**
     * Returns fitting {@link CollectorWriter} for given collector, depending on
     * the type of {@link Collectable}.
     * 
     * @param collector
     *            the collector which data needs to be written
     * @param outputPath
     *            the output path, if several are needed the given one acts a
     *            base and is extended
     * @return a {@link CollectorWriter} for given collector
     */
    public static CollectorWriter create(Collector<?> collector, Path outputPath) {
	if (collector.getCollectable() instanceof OneShotCollectable) {
	    return new OneShotCollectorWriter(collector, outputPath);
	}
	return new LineCollectorWriter(collector, outputPath);
    }
}

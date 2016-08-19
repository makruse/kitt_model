package de.zmt.output;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.output.collectable.Collectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.message.SimpleCollectMessage;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.output.writing.LineOutputWriter;
import de.zmt.output.writing.OutputWriter;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.util.TimeOfDay;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.field.grid.LongGrid2D;
import sim.util.Int2D;
import sim.util.Int2DCache;

/**
 * Accumulates the stay durations for every map location.
 * 
 * @author mey
 * 
 */
public class LocationStayDurations implements Collectable<Long> {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link StrategyCollector} to collect stay durations.
     * 
     * @param width
     *            the map width
     * @param height
     *            the map height
     * @param timeOfDay
     *            the time of day to collect durations
     * @return the {@link StrategyCollector} for collecting habitat stay
     *         durations
     */
    public static StrategyCollector<LocationStayDurations> createCollector(int width, int height, TimeOfDay timeOfDay) {
        return StrategyCollector.create(new LocationStayDurations(width, height), new MyCollectStrategy(timeOfDay));
    }

    private LocationStayDurations(int width, int height) {
        super();
        this.durationGrid = new LongGrid2D(width, height);
    }

    /** The grid where durations are stored. */
    private final LongGrid2D durationGrid;

    /**
     * Register a stay.
     * 
     * @param location
     *            the location to register a stay for
     */
    private void registerStay(Int2D location) {
        Amount<Duration> stepDuration = EnvironmentDefinition.STEP_DURATION;
        long currentValue = durationGrid.get(location.x, location.y);
        durationGrid.set(location.x, location.y, currentValue + stepDuration.getExactValue());
    }

    @Override
    public List<String> obtainHeaders() {
        int width = durationGrid.getWidth();
        int height = durationGrid.getHeight();
        List<String> headers = new ArrayList<>(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Int2D location = Int2DCache.get(x, y);
                headers.add(location.x + "x" + location.y);
            }
        }

        return headers;
    }

    @Override
    public Iterable<? extends Long> obtainValues() {
        int width = durationGrid.getWidth();
        int height = durationGrid.getHeight();
        List<Long> values = new ArrayList<>(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                values.add(durationGrid.get(x, y));
            }
        }

        return values;
    }

    @Override
    public int getSize() {
        return durationGrid.getWidth() * durationGrid.getHeight();
    }

    /** Fill maps with zero durations. */
    @Override
    public void clear() {
        durationGrid.setTo(0);
    }

    /**
     * Returns a {@link LineOutputWriter} that clears the collectable after
     * writing.
     */
    @Override
    public OutputWriter createWriter(Path outputPath) {
        return new ClearingLineOutputWriter(this, outputPath);
    }

    private static class MyCollectStrategy
            extends MessageCollectStrategy<LocationStayDurations, SimpleCollectMessage<Int2D>> {
        private static final long serialVersionUID = 1L;
        private static final MyCollectMessageFactory MESSAGE_FACTORY = new MyCollectMessageFactory();

        private final TimeOfDay timeOfDay;

        public MyCollectStrategy(TimeOfDay timeOfDay) {
            super();
            this.timeOfDay = timeOfDay;
        }

        @Override
        protected void collect(SimpleCollectMessage<Int2D> message, LocationStayDurations collectable) {
            collectable.registerStay(message.getSimObject());
        }

        @Override
        protected CollectMessageFactory<SimpleCollectMessage<Int2D>> getCollectMessageFactory() {
            return MESSAGE_FACTORY;
        }

        @Override
        public void process(SimState state, LocationStayDurations collectable) {
            // if time of day is not right, skip
            if (((Kitt) state).getEnvironment().get(SimulationTime.class).getTimeOfDay() != timeOfDay) {
                return;
            }
            super.process(state, collectable);
        }
    }

    private static class MyCollectMessageFactory
            implements CollectMessageFactory<SimpleCollectMessage<Int2D>>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public Stream<SimpleCollectMessage<Int2D>> createCollectMessages(SimState state) {
            // if not our time of day, do not generate messages
            return AgentCollectMessage.FACTORY.createCollectMessages(state).map(message -> {
                Int2D location = message.getSimObject().get(Moving.class).getMapPosition();
                return new SimpleCollectMessage<>(location);
            });
        }
    }

    /**
     * {@link LineOutputWriter} that clears its {@link Collectable} after
     * writing.
     * 
     * @author mey
     *
     */
    private static class ClearingLineOutputWriter extends LineOutputWriter {
        private static final long serialVersionUID = 1L;

        public ClearingLineOutputWriter(Collectable<?> collectable, Path outputPath) {
            super(collectable, outputPath);
        }

        @Override
        public void writeValues(long steps) throws IOException {
            super.writeValues(steps);
            getCollectable().clear();
        }

    }
}
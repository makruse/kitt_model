package de.zmt.output;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.output.collectable.OneShotCollectable;
import de.zmt.output.collector.StrategyCollector;
import de.zmt.output.message.CollectMessage;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.strategy.MessageCollectStrategy;
import de.zmt.util.TimeOfDay;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.field.grid.LongGrid2D;
import sim.util.Int2D;

/**
 * Accumulates the stay durations for every map location. Durations are reset to
 * zero after obtaining values.
 * <p>
 * Depending on map size, output can be huge and consume a considerable amount
 * of disk space.
 * 
 * @author mey
 * 
 */
class LocationStayDurations implements OneShotCollectable<Object, Collection<Object>> {
    private static final long serialVersionUID = 1L;

    /** The grids where durations are stored, one for each {@link TimeOfDay}. */
    private final Map<TimeOfDay, LongGrid2D> durationGrids = new EnumMap<>(TimeOfDay.class);

    /** The duration of one simulation step. */
    private final Amount<Duration> stepDuration;
    private final HabitatMap habitatMap;
    private final FoodMap foodMap;

    /**
     * Creates a {@link StrategyCollector} to collect stay durations.
     * 
     * @param stepDuration
     *            the duration of one simulation step
     * @param habitatMap
     *            the {@link HabitatMap}
     * @param foodMap
     *            the {@link FoodMap}
     * @return the {@link StrategyCollector} for collecting habitat stay
     *         durations
     */
    public static StrategyCollector<LocationStayDurations> createCollector(Amount<Duration> stepDuration,
            HabitatMap habitatMap, FoodMap foodMap) {
        return StrategyCollector.create(new LocationStayDurations(stepDuration, habitatMap, foodMap),
                new MyCollectStrategy());
    }

    LocationStayDurations(Amount<Duration> stepDuration, HabitatMap habitatMap, FoodMap foodMap) {
        super();
        assert habitatMap.getWidth() == foodMap.getWidth() && habitatMap.getHeight() == foodMap.getHeight();

        Stream.of(TimeOfDay.values()).forEach(timeOfDay -> durationGrids.put(timeOfDay,
                new LongGrid2D(habitatMap.getWidth(), habitatMap.getHeight())));
        this.stepDuration = stepDuration;
        this.habitatMap = habitatMap;
        this.foodMap = foodMap;
    }

    /**
     * Register a stay.
     * 
     * @param location
     *            the location to register a stay for
     * @param timeOfDay
     *            the time of day the stay was happening
     */
    void registerStay(Int2D location, TimeOfDay timeOfDay) {
        LongGrid2D grid = durationGrids.get(timeOfDay);
        long currentValue = grid.get(location.x, location.y);
        grid.set(location.x, location.y, currentValue + stepDuration.getExactValue());
    }

    @Override
    public List<String> obtainHeaders() {
        return Headers.LIST;
    }

    @Override
    public Iterable<Collection<Object>> obtainValues() {
        int width = habitatMap.getWidth();
        int height = habitatMap.getHeight();
        Table<Integer, String, Object> valuesTable = ArrayTable
                .create(IntStream.range(0, getColumnSize()).boxed()::iterator, Headers.LIST);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // set the row for the current cell
                valuesTable.row(y * width + x).putAll(assembleValuesRow(x, y));
            }
        }
        clear();

        // return iterable over table by headers (columns)
        return Headers.LIST.stream().map(header -> valuesTable.column(header).values())::iterator;
    }

    /**
     * Assembles the value row for the given cell coordinates.
     * 
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @return a {@link Map} representing the row with headers as keys
     */
    private Map<String, Object> assembleValuesRow(int x, int y) {
        return Maps.asMap(new HashSet<>(Headers.LIST), header -> {
            switch (header) {
            case Headers.CELL_X:
                return x;
            case Headers.CELL_Y:
                return y;
            case Headers.HABITAT:
                return habitatMap.obtainHabitat(x, y);
            case Headers.STAY_DURATION_TOTAL:
                return durationGrids.values().stream().mapToLong(grid -> grid.get(x, y)).sum();
            default:
                if (header.equals(Headers.FOOD_DENSITY)) {
                    return foodMap.getFoodDensityValue(x, y);
                }
                // stay duration by time of day
                else {
                    TimeOfDay timeOfDay = Headers.STAY_DURATIONS.get(header);
                    return durationGrids.get(timeOfDay).get(x, y);
                }
            }
        });
    }

    @Override
    public int getSize() {
        return Headers.LIST.size();
    }

    @Override
    public int getColumnSize() {
        return habitatMap.getWidth() * habitatMap.getHeight();
    }

    /** Fill maps with zero durations. */
    @Override
    public void clear() {
        durationGrids.values().stream().forEach(grid -> grid.setTo(0));
    }

    /**
     * Contains the String constants used as headers.
     * 
     * @author mey
     *
     */
    static class Headers {
        private static final String STAY_DURATION_ = "stay_duration_";

        public static final String CELL_X = "cell_x";
        public static final String CELL_Y = "cell_y";
        public static final String HABITAT = "habitat";
        public static final String STAY_DURATION_TOTAL = STAY_DURATION_ + "total";
        /** Map containing header string for every {@link TimeOfDay}. */
        public static final Map<String, TimeOfDay> STAY_DURATIONS;
        public static final String FOOD_DENSITY = "food_density_" + UnitConstants.FOOD_DENSITY;

        static {
            Map<String, TimeOfDay> stayDurationHeaders = new LinkedHashMap<>();
            for (TimeOfDay timeOfDay : TimeOfDay.values()) {
                stayDurationHeaders.put(STAY_DURATION_ + timeOfDay.name().toLowerCase(), timeOfDay);
            }
            STAY_DURATIONS = Collections.unmodifiableMap(stayDurationHeaders);
        }

        private Headers() {

        }

        /** {@link List} containing all headers in order. */
        public static final List<String> LIST = Stream
                .of(Stream.of(CELL_X, CELL_Y, HABITAT, STAY_DURATION_TOTAL), STAY_DURATIONS.keySet().stream(),
                        Stream.of(FOOD_DENSITY))
                .flatMap(Function.identity())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    /**
     * {@link MessageCollectStrategy} implementation that filters for a given
     * {@link TimeOfDay}.
     * 
     * @author mey
     *
     */
    private static class MyCollectStrategy extends MessageCollectStrategy<LocationStayDurations, MyCollectMessage> {
        private static final long serialVersionUID = 1L;
        private static final MyCollectMessageFactory MESSAGE_FACTORY = new MyCollectMessageFactory();

        @Override
        protected void collect(MyCollectMessage message, LocationStayDurations collectable) {
            collectable.registerStay(message.location, message.timeOfDay);
        }

        @Override
        protected CollectMessageFactory<MyCollectMessage> getCollectMessageFactory() {
            return MESSAGE_FACTORY;
        }
    }

    /**
     * {@link CollectMessageFactory} that creates message with a location.
     * 
     * @author mey
     *
     */
    private static class MyCollectMessageFactory implements CollectMessageFactory<MyCollectMessage>, Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public Stream<MyCollectMessage> createCollectMessages(SimState state) {
            TimeOfDay timeOfDay = ((Kitt) state).getEnvironment().get(SimulationTime.class).getTimeOfDay();

            return AgentCollectMessage.FACTORY.createCollectMessages(state).map(message -> {
                Int2D location = message.getSimObject().get(Moving.class).getMapPosition();
                return new MyCollectMessage(location, timeOfDay);
            });
        }
    }

    static class MyCollectMessage implements CollectMessage {
        public final Int2D location;
        public final TimeOfDay timeOfDay;

        public MyCollectMessage(Int2D location, TimeOfDay timeOfDay) {
            super();
            this.location = location;
            this.timeOfDay = timeOfDay;
        }

    }
}
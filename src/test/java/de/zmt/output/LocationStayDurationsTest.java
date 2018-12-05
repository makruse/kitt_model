package de.zmt.output;

import static javax.measure.unit.SI.SECOND;
import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Multimap;

import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.output.LocationStayDurations.Headers;
import de.zmt.util.Habitat;
import de.zmt.util.TimeOfDay;
import de.zmt.util.UnitConstants;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.Int2D;

public class LocationStayDurationsTest {
    private static final Habitat HABITAT = Habitat.DEFAULT;
    private static final double FOOD_DENSITY = 1;
    private static final long STEP_DURATION_SECOND = 1;
    /**
     * Inverse map of {@link LocationStayDurations.Headers#STAY_DURATIONS} to
     * make headers accessible via {@link TimeOfDay}.
     */
    private static final Map<TimeOfDay, String> STAY_DURATION_HEADERS = Headers.STAY_DURATIONS.entrySet().stream()
            .collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    @Before
    public void setUp() throws Exception {

    }

    /*
    MKR: test goal unclear
    seemingly assert/method used wrongly?
    */
    @SuppressWarnings("unchecked")
    @Test @Ignore
    public void obtainValues() {
        Multimap<String, ?> valuesMap = create(2, 2).toMultimap();

        assertThat(valuesMap.get(Headers.CELL_X), containsInAnyOrder(0, 0, 1, 1));
        assertThat(valuesMap.get(Headers.CELL_Y), containsInAnyOrder(0, 0, 1, 1));
        assertThat((Collection<Habitat>) valuesMap.get(Headers.HABITAT), everyItem(is(HABITAT)));
        assertThat((Collection<Double>) valuesMap.get(Headers.FOOD_DENSITY), everyItem(
                closeTo(FOOD_DENSITY + HABITAT.getFoodDensityMin().doubleValue(UnitConstants.FOOD_DENSITY), 1E-14d)));
    }

    /*
    MKR: test goal unclear
    seemingly want to check actual value in a string, but method used in the wrong way maybe?
     */
    @Test @Ignore
    public void registerStay() {
        LocationStayDurations locationStayDurations = create(1, 1);

        locationStayDurations.registerStay(new Int2D(0, 0), TimeOfDay.DAY);
        locationStayDurations.registerStay(new Int2D(0, 0), TimeOfDay.NIGHT);
        Multimap<String, Object> values = locationStayDurations.toMultimap();

        assertThat(values.get(Headers.STAY_DURATION_TOTAL), contains(2 * STEP_DURATION_SECOND));
        assertThat(values.get(STAY_DURATION_HEADERS.get(TimeOfDay.DAY)), contains(STEP_DURATION_SECOND));
        assertThat(values.get(STAY_DURATION_HEADERS.get(TimeOfDay.NIGHT)), contains(STEP_DURATION_SECOND));
    }

    private static LocationStayDurations create(int width, int height) {
        HabitatMap habitatMap = new HabitatMap(new IntGrid2D(width, height, HABITAT.ordinal()));
        FoodMap foodMap = new FoodMap(new DoubleGrid2D(width, height, FOOD_DENSITY), null);
        return new LocationStayDurations(Amount.valueOf(STEP_DURATION_SECOND, SECOND), habitatMap, foodMap);
    }

}

package de.zmt.ecs.component.environment;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.IntGrid2D;
import sim.util.Int2D;

public class HabitatMapTest {
    private static final int ORDINAL_CORALREEF = Habitat.CORALREEF.ordinal();
    private static final int ORDINAL_SEAGRASS = Habitat.SEAGRASS.ordinal();
    private static final IntGrid2D HABITAT_MAP_GRID = new IntGrid2D(
            new int[][] { { ORDINAL_CORALREEF, ORDINAL_CORALREEF }, { ORDINAL_SEAGRASS, ORDINAL_SEAGRASS } });
    private static final Set<Int2D> CORALREEF_POSITIONS = new HashSet<>(
            Arrays.asList(new Int2D(0, 0), new Int2D(0, 1)));
    private static final Set<Int2D> SEAGRASS_POSITIONS = new HashSet<>(Arrays.asList(new Int2D(1, 0), new Int2D(1, 1)));
    private static final Set<Int2D> POSITIONS;

    static {
        POSITIONS = new HashSet<>();
        POSITIONS.addAll(CORALREEF_POSITIONS);
        POSITIONS.addAll(SEAGRASS_POSITIONS);
    }

    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();
    private static final int ITERATIONS = 10;

    private HabitatMap habitatMap;

    @Before
    public void setUp() throws Exception {
        habitatMap = new HabitatMap(HABITAT_MAP_GRID);
    }

    @Test
    public void generateRandomPosition() {
        for (int i = 0; i < ITERATIONS; i++) {
            assertThat(POSITIONS, hasItem(habitatMap.generateRandomPosition(RANDOM, EnumSet.allOf(Habitat.class))));
            assertThat(CORALREEF_POSITIONS,
                    hasItem(habitatMap.generateRandomPosition(RANDOM, EnumSet.of(Habitat.CORALREEF))));
            assertThat(SEAGRASS_POSITIONS,
                    hasItem(habitatMap.generateRandomPosition(RANDOM, EnumSet.of(Habitat.SEAGRASS))));
        }
    }

}

package de.zmt.ecs.component.environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.zmt.ecs.Component;
import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.IntGrid2D;
import sim.util.Int2D;

/**
 * Stores a {@link Habitat} for every grid cell in discrete map space. This is
 * done via {@link IntGrid2D} containing {@link Habitat} ordinal numbers.
 * 
 * @author mey
 *
 */
public class HabitatMap extends EncapsulatedGrid<IntGrid2D> implements Component {
    private static final long serialVersionUID = 1L;

    /**
     * Calling values() creates an array each time and is called quite often. To
     * avoid that, this constant is used instead.
     */
    private static final Habitat[] HABITAT_VALUES = Habitat.values();

    /**
     * Habitats associated with positions to speed up generating random
     * positions within certain habitats.
     * 
     * @see #generateRandomPosition(MersenneTwisterFast, Set)
     */
    private final Map<Habitat, List<Int2D>> habitatPositions;

    public HabitatMap(IntGrid2D habitatField) {
        super(habitatField);
        this.habitatPositions = buildHabitatPositions(habitatField);
    }

    private static Map<Habitat, List<Int2D>> buildHabitatPositions(IntGrid2D habitatField) {
        Map<Habitat, List<Int2D>> habitatPositions = new HashMap<>();

        // create a position list for every habitat
        for (Habitat habitat : HABITAT_VALUES) {
            // use ArrayList here for faster copying in generateRandomPosition
            habitatPositions.put(habitat, new ArrayList<Int2D>());
        }

        // populate lists with data from habitat field
        for (int y = 0; y < habitatField.getHeight(); y++) {
            for (int x = 0; x < habitatField.getWidth(); x++) {
                Habitat habitat = HABITAT_VALUES[habitatField.get(x, y)];
                habitatPositions.get(habitat).add(new Int2D(x, y));
            }
        }

        return habitatPositions;
    }

    /**
     * Direct access to habitat field.
     * 
     * @param mapPosition
     *            the map position
     * @return habitat
     */
    public Habitat obtainHabitat(Int2D mapPosition) {
        return obtainHabitat(mapPosition.getX(), mapPosition.getY());
    }

    /**
     * Direct access to habitat field.
     * 
     * @param mapX
     *            map X coordinate
     * @param mapY
     *            map Y coordinate
     * @return habitat
     */
    public Habitat obtainHabitat(int mapX, int mapY) {
        return HABITAT_VALUES[getGrid().get(mapX, mapY)];
    }

    /**
     * Generate random map position within given {@code habitats}.
     * 
     * @param random
     * @param habitats
     * @return Random position within given habitats
     * @throws IllegalArgumentException
     *             if habitats are not found within current map
     */
    public Int2D generateRandomPosition(MersenneTwisterFast random, Set<Habitat> habitats) {
        // collect lists of positions for given habitats
        List<Int2D> possiblePositions = new ArrayList<>();
        for (Habitat habitat : habitats) {
            possiblePositions.addAll(habitatPositions.get(habitat));
        }

        if (possiblePositions.isEmpty()) {
            throw new IllegalArgumentException("Current map does not contain " + habitats);
        }

        // generate index within collected list
        int randomIndex = random.nextInt(possiblePositions.size());
        // ... and return the position associated with that index
        return possiblePositions.get(randomIndex);
    }
}

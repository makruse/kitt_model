package de.zmt.ecs.component.environment;

import java.util.*;

import de.zmt.ecs.Component;
import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.IntGrid2D;
import sim.portrayal.portrayable.*;
import sim.util.*;

/**
 * Stores a {@link Habitat} for every grid cell in discrete map space.
 * 
 * @author mey
 *
 */
public class HabitatMap implements Component, ProvidesPortrayable<FieldPortrayable<IntGrid2D>> {
    private static final long serialVersionUID = 1L;

    /**
     * Calling values() creates an array each time and is called quite often. To
     * avoid that, this constant is used instead.
     */
    private static final Habitat[] HABITAT_VALUES = Habitat.values();
    /**
     * Stores {@link Habitat} ordinal for every location (immutable, loaded from
     * image).
     */
    final IntGrid2D habitatField;

    /**
     * Habitats associated with positions to speed up generating random
     * positions within certain habitats.
     * 
     * @see #generateRandomPosition(MersenneTwisterFast, Set)
     */
    private final Map<Habitat, List<Int2D>> habitatPositions;

    public HabitatMap(IntGrid2D habitatField) {
	this.habitatField = habitatField;
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
     * @param mapX
     *            map X coordinate
     * @param mapY
     *            map Y coordinate
     * @return habitat
     */
    public Habitat obtainHabitat(int mapX, int mapY) {
	return HABITAT_VALUES[habitatField.get(mapX, mapY)];
    }

    /**
     * Obtains habitat at given {@code worldPosition} by transforming it via
     * {@link WorldToMapConverter}.
     * 
     * @param worldPosition
     * @param converter
     *            {@link WorldToMapConverter}
     * @return habitat at given {@code worldPosition}
     */
    public Habitat obtainHabitat(Double2D worldPosition, WorldToMapConverter converter) {
	Int2D mapPosition = converter.worldToMap(worldPosition);
	return obtainHabitat(mapPosition.x, mapPosition.y);
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
	int possiblePositionsCount = 0;

	// collect lists of positions for given habitats
	List<List<Int2D>> possiblePositionsInHabitats = new ArrayList<>(habitats.size());
	for (Habitat habitat : habitats) {
	    List<Int2D> possibleHabitatPositions = habitatPositions.get(habitat);
	    possiblePositionsInHabitats.add(possibleHabitatPositions);
	    possiblePositionsCount += possibleHabitatPositions.size();
	}

	// generate index within collected lists
	int randomIndex = random.nextInt(possiblePositionsCount);
	int currentCount = 0;

	// iterate until list with randomIndex is found
	for (Iterator<List<Int2D>> iterator = possiblePositionsInHabitats.iterator(); iterator.hasNext();) {
	    List<Int2D> positions = iterator.next();
	    currentCount += positions.size();
	    if (randomIndex < currentCount) {
		return positions.get(randomIndex);
	    }
	}

	throw new IllegalArgumentException("Current map does not contain " + habitats);
    }

    @Override
    public FieldPortrayable<IntGrid2D> providePortrayable() {
	return new FieldPortrayable<IntGrid2D>() {

	    @Override
	    public IntGrid2D getField() {
		return habitatField;
	    }
	};
    }
}

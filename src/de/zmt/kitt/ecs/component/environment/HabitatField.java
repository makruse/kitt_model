package de.zmt.kitt.ecs.component.environment;

import java.util.*;

import sim.field.grid.IntGrid2D;
import sim.util.*;
import de.zmt.kitt.sim.Habitat;
import ec.util.MersenneTwisterFast;
import ecs.Component;

public class HabitatField implements Component {
    private static final long serialVersionUID = 1L;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatField;

    /**
     * Habitats associated with positions to speed up generating random
     * positions within certain habitats.
     * 
     * @see #generateRandomPosition(MersenneTwisterFast, Habitat...)
     */
    private final Map<Habitat, List<Int2D>> habitatPositions;

    public HabitatField(IntGrid2D habitatField) {
	this.habitatField = habitatField;
	this.habitatPositions = buildHabitatPositions(habitatField);
    }

    private static Map<Habitat, List<Int2D>> buildHabitatPositions(
	    IntGrid2D habitatField) {
	Map<Habitat, List<Int2D>> habitatPositions = new HashMap<>();

	// create a position list for every habitat
	for (Habitat habitat : Habitat.values()) {
	    // use ArrayList here for faster copying in generateRandomPosition
	    habitatPositions.put(habitat, new ArrayList<Int2D>());
	}

	// populate lists with data from habitat field
	for (int y = 0; y < habitatField.getHeight(); y++) {
	    for (int x = 0; x < habitatField.getWidth(); x++) {
		Habitat habitat = Habitat.values()[habitatField.get(x, y)];
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
	return Habitat.values()[habitatField.get(mapX, mapY)];
    }

    /**
     * Obtains habitat at given {@code worldPosition} by transforming it via
     * {@link EnvironmentDefinition}.
     * 
     * @param worldPosition
     * @param converter
     *            {@link WorldToMapConverter}
     * @return habitat at given {@code worldPosition}
     */
    public Habitat obtainHabitat(Double2D worldPosition,
	    WorldToMapConverter converter) {
	Double2D mapPosition = converter.worldToMap(worldPosition);
	return obtainHabitat((int) mapPosition.x, (int) mapPosition.y);
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
    public Int2D generateRandomPosition(MersenneTwisterFast random,
	    Habitat... habitats) {
	int possiblePositionsCount = 0;

	// collect lists of positions for given habitats
	List<List<Int2D>> possiblePositionsInHabitats = new ArrayList<>(
		habitats.length);
	for (int i = 0; i < habitats.length; i++) {
	    Habitat habitat = habitats[i];
	    List<Int2D> possibleHabitatPositions = habitatPositions
		    .get(habitat);
	    possiblePositionsInHabitats.add(possibleHabitatPositions);
	    possiblePositionsCount += possibleHabitatPositions.size();
	}

	// generate index within collected lists
	int randomIndex = random.nextInt(possiblePositionsCount);
	int currentCount = 0;

	// iterate until list with randomIndex is found
	for (Iterator<List<Int2D>> iterator = possiblePositionsInHabitats
		.iterator(); iterator.hasNext();) {
	    List<Int2D> positions = iterator.next();
	    currentCount += positions.size();
	    if (randomIndex < currentCount) {
		return positions.get(randomIndex);
	    }
	}

	throw new IllegalArgumentException("Current map does not contain "
		+ habitats);
    }

    /**
     * Field object getter for portrayal in GUI.
     * 
     * @return habitat field
     */
    public IntGrid2D getField() {
	return habitatField;
    }

    public static interface WorldToMapConverter {
	/**
	 * Convert from world to map coordinates (pixel).
	 * 
	 * 
	 * @param worldCoordinates
	 * @return map coordinates
	 */
	Double2D worldToMap(Double2D worldCoordinates);
    }
}

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

    /**
     * Map scale in pixel per meter. Needed to translate from field positions
     * (meter) to habitats from grid.
     */
    private final double mapScale;

    public HabitatField(IntGrid2D habitatField, double mapScale) {
	this.habitatField = habitatField;
	this.mapScale = mapScale;
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
     * 
     * @param position
     *            in meter
     * @return {@link Habitat} on given position
     */
    public Habitat obtainHabitat(Double2D position) {
	// habitat is different from field size if mapScale != 1
	return obtainHabitat((int) (position.x * mapScale),
		(int) (position.y * mapScale));
    }

    /**
     * Direct access to habitat field.
     * 
     * @param x
     * @param y
     * @return habitat
     */
    private Habitat obtainHabitat(int x, int y) {
	return Habitat.values()[habitatField.get(x, y)];
    }

    /**
     * Generate random position within given {@code habitats}.
     * 
     * @param random
     * @param habitats
     * @return Random position within given habitats
     * @throws IllegalArgumentException
     *             if habitats are not found within current map
     */
    public Double2D generateRandomPosition(MersenneTwisterFast random,
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
		Int2D randomPosition = positions.get(randomIndex);
		return new Double2D(randomPosition.x / mapScale,
			randomPosition.y / mapScale);
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

}

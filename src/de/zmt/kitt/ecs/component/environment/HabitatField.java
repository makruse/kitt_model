package de.zmt.kitt.ecs.component.environment;

import java.util.Arrays;

import sim.field.grid.IntGrid2D;
import sim.util.Double2D;
import de.zmt.kitt.sim.Habitat;
import ec.util.MersenneTwisterFast;
import ecs.Component;

public class HabitatField implements Component {
    private static final long serialVersionUID = 1L;
    /** Stores habitat ordinal for every location (immutable, loaded from image) */
    private final IntGrid2D habitatField;
    /**
     * Map scale in pixel per meter. Needed to translate from field positions
     * (meter) to habitats from grid.
     */
    private final double mapScale;

    public HabitatField(IntGrid2D habitatField, double mapScale) {
	this.habitatField = habitatField;
	this.mapScale = mapScale;
    }

    /**
     * 
     * @param position
     *            in meter
     * @return {@link Habitat} on given position
     */
    public Habitat obtainHabitat(Double2D position) {
	// habitat is different from field size if mapScale != 1
	return Habitat.values()[habitatField.get((int) (position.x * mapScale),
		(int) (position.y * mapScale))];
    }

    /**
     * 
     * @param habitat
     * @return Random position within given habitats
     */
    public Double2D generateRandomPosition(MersenneTwisterFast random,
	    Habitat... inHabitats) {
	int x, y;
	do {
	    x = random.nextInt(habitatField.getWidth());
	    y = random.nextInt(habitatField.getWidth());
	} while (Arrays.asList(inHabitats).contains(habitatField.get(x, y)));
	return new Double2D(x / mapScale, y / mapScale);
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

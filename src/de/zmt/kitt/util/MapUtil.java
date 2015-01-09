package de.zmt.kitt.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import sim.field.grid.*;
import de.zmt.kitt.sim.HabitatHerbivore;
import ec.util.MersenneTwisterFast;

/**
 * Utility functions for creating habitat and food fields.
 * 
 * @author cmeyer
 * 
 */
public class MapUtil {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MapUtil.class
	    .getName());

    /**
     * Creates habitat field from given image map. Colors are associated to
     * habitats. If an invalid color is encountered,
     * {@link HabitatHerbivore#SANDYBOTTOM} is used as default.
     * 
     * @see HabitatHerbivore#getColor()
     * @param random
     * @param mapImage
     * @return populated habitat field
     */
    public static ObjectGrid2D createHabitatFieldFromMap(
	    MersenneTwisterFast random, BufferedImage mapImage) {
	logger.fine("creating habitat field from image.");

	ObjectGrid2D habitatField = new ObjectGrid2D(mapImage.getWidth(),
		mapImage.getHeight());

	// traverse habitat field and populate from map image
	for (int y = 0; y < habitatField.getHeight(); y++) {
	    for (int x = 0; x < habitatField.getWidth(); x++) {
		HabitatHerbivore curHabitat = null;
		Color color = new Color(mapImage.getRGB(x, y));
		for (HabitatHerbivore habitat : HabitatHerbivore.values()) {
		    if (habitat.getColor().equals(color)) {
			curHabitat = habitat;
			break;
		    }
		}
		if (curHabitat == null) {
		    logger.warning("Color " + color + " in image " + mapImage
			    + " is not associated to a habitat type. "
			    + "Using sandy bottom.");
		    curHabitat = HabitatHerbivore.SANDYBOTTOM;
		}

		habitatField.set(x, y, curHabitat);
	    }
	}

	return habitatField;
    }

    /**
     * Creates food field populated by random values between min and max values
     * from {@link HabitatHerbivore} defintions.
     * 
     * @see HabitatHerbivore#getInitialFoodMin()
     * @see HabitatHerbivore#getInitialFoodMax()
     * @param habitatField
     * @param random
     * @return populated food field
     */
    public static DoubleGrid2D createFoodFieldFromHabitats(
	    ObjectGrid2D habitatField, MersenneTwisterFast random) {
	logger.fine("creating food field from habitat field");

	DoubleGrid2D foodField = new DoubleGrid2D(habitatField.getWidth(),
		habitatField.getHeight());
	// traverse food grid and populate from habitat rules
	for (int y = 0; y < foodField.getHeight(); y++) {
	    for (int x = 0; x < foodField.getWidth(); x++) {
		HabitatHerbivore currentHabitat = (HabitatHerbivore) habitatField
			.get(x, y);

		double minFood = currentHabitat.getInitialFoodMin();
		double maxFood = currentHabitat.getInitialFoodMax();
		double foodVal = random.nextDouble() * (maxFood - minFood)
			+ minFood;
		foodField.set(x, y, foodVal);
	    }
	}

	return foodField;
    }
}

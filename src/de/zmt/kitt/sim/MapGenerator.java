package de.zmt.kitt.sim;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import sim.field.grid.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.engine.Environment;
import ec.util.MersenneTwisterFast;

public class MapGenerator {

    protected MersenneTwisterFast random;
    BufferedImage mapImage = null;

    public MapGenerator(MersenneTwisterFast random) {
	this.random = random;
	try {

	    mapImage = ImageIO.read(new File(Sim.DEFAULT_INPUT_DIR
		    + "CoralEyeHabitatMapGUI.jpg"));

	} catch (IOException e) {

	    e.printStackTrace();
	}
    }

    public int getMapWidth() {
	return mapImage.getWidth();
    }

    public int getMapHeight() {
	return mapImage.getHeight();
    }

    public void createFieldsByMap(DoubleGrid2D foodField,
	    IntGrid2D habitatField, Environment env) {

	System.out.println(mapImage.getWidth() + " " + mapImage.getHeight());
	for (int y = 0; y < mapImage.getHeight(); y++) {
	    for (int x = 0; x < mapImage.getWidth(); x++) {

		HabitatHerbivore curHabitat = HabitatHerbivore.SANDYBOTTOM;

		int clr = mapImage.getRGB(x, y);
		int r = (clr & 0x00ff0000) >> 16;
		int g = (clr & 0x0000ff00) >> 8;
		int b = clr & 0x000000ff;

		// if( HabitatHerbivore.ROCK.getColor().getRed() == r &&
		// HabitatHerbivore.ROCK.getColor().getGreen() == g &&
		// HabitatHerbivore.ROCK.getColor().getBlue() == b)
		// curHabitat = HabitatHerbivore.ROCK;
		if (HabitatHerbivore.SANDYBOTTOM.getColor().getRed() == r
			&& HabitatHerbivore.SANDYBOTTOM.getColor().getGreen() == g
			&& HabitatHerbivore.SANDYBOTTOM.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.SANDYBOTTOM;
		else if (HabitatHerbivore.CORALREEF.getColor().getRed() == r
			&& HabitatHerbivore.CORALREEF.getColor().getGreen() == g
			&& HabitatHerbivore.CORALREEF.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.CORALREEF;
		else if (HabitatHerbivore.SEAGRASS.getColor().getRed() == r
			&& HabitatHerbivore.SEAGRASS.getColor().getGreen() == g
			&& HabitatHerbivore.SEAGRASS.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.SEAGRASS;
		else if (HabitatHerbivore.ROCK.getColor().getRed() == r
			&& HabitatHerbivore.ROCK.getColor().getGreen() == g
			&& HabitatHerbivore.ROCK.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.ROCK;
		else if (HabitatHerbivore.MANGROVE.getColor().getRed() == r
			&& HabitatHerbivore.MANGROVE.getColor().getGreen() == g
			&& HabitatHerbivore.MANGROVE.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.MANGROVE;
		else if (HabitatHerbivore.MAINLAND.getColor().getRed() == r
			&& HabitatHerbivore.MAINLAND.getColor().getGreen() == g
			&& HabitatHerbivore.MAINLAND.getColor().getBlue() == b)
		    curHabitat = HabitatHerbivore.MAINLAND;

		habitatField.set(x, y, curHabitat.id);

		// initialize foodfield by habitat rules
		double foodVal = random.nextDouble()
			* (curHabitat.initialFoodMax - curHabitat.initialFoodMin)
			+ curHabitat.initialFoodMin;
		env.setFoodOnPosition(new Double2D(x, y), foodVal);
	    }
	}
	System.out.println("created fields..");
    }
}

package de.zmt.ecs.factory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.component.environment.FoodMap;
import de.zmt.ecs.component.environment.GlobalFlowMap;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.NormalMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.SpeciesFlowMap;
import de.zmt.pathfinding.ConvolvingPotentialMap;
import de.zmt.pathfinding.PotentialMap;
import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.pathfinding.filter.Kernel;
import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.ObjectGrid2D;
import sim.params.def.EnvironmentDefinition;
import sim.util.Double2D;
import sim.util.Int2D;

/**
 * Factory for creating the environment entity.
 * 
 * @author mey
 *
 */
class EnvironmentFactory implements EntityFactory<EnvironmentDefinition> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EnvironmentFactory.class.getName());

    private static final String ENVIRONMENT_ENTITY_NAME = "Environment";

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random, EnvironmentDefinition definition) {
	return new Entity(manager, ENVIRONMENT_ENTITY_NAME, createComponents(random, definition));
    }

    /**
     * 
     * @param random
     * @param definition
     * @return components for the environment entity
     */
    private static Collection<Component> createComponents(MersenneTwisterFast random,
	    EnvironmentDefinition definition) {
	BufferedImage mapImage = loadMapImage(EnvironmentDefinition.RESOURCES_DIR + definition.getMapImageFilename());

	// create fields
	IntGrid2D habitatGrid = createHabitatGrid(random, mapImage);
	// no normals needed at the moment
	int mapWidth = habitatGrid.getWidth();
	int mapHeight = habitatGrid.getHeight();
	ObjectGrid2D normalGrid = new ObjectGrid2D(mapWidth, mapHeight);
	DoubleGrid2D foodGrid = createFoodGrid(habitatGrid, random);
	Double2D worldBounds = definition.mapToWorld(new Int2D(mapWidth, mapHeight));

	ConvolvingPotentialMap foodPotentialMap = createFoodPotentialMap(foodGrid);
	GlobalFlowMap globalFlowMap = createGlobalFlowMap(foodPotentialMap);

	// gather components
	Collection<Component> components = Arrays.asList(definition, new AgentWorld(worldBounds.x, worldBounds.y),
		new FoodMap(foodGrid, foodPotentialMap), globalFlowMap, new HabitatMap(habitatGrid),
		new NormalMap(normalGrid), new SimulationTime(EnvironmentDefinition.START_INSTANT),
		new SpeciesFlowMap.Container());

	return components;
    }

    /**
     * 
     * @param imagePath
     * @return image loaded from {@code imagePath}
     */
    private static BufferedImage loadMapImage(String imagePath) {
	BufferedImage mapImage = null;
	logger.fine("Loading map image from " + imagePath);
	try {
	    mapImage = ImageIO.read(new File(imagePath));
	} catch (IOException e) {
	    logger.log(Level.WARNING, "Could not load map image from " + imagePath);
	}
	return mapImage;
    }

    /**
     * Creates habitat field from given image map. Colors are associated to
     * habitats. If an invalid color is encountered, {@link Habitat#DEFAULT} is
     * used.
     * 
     * @see Habitat#getColor()
     * @param random
     * @param mapImage
     * @return populated habitat field
     */
    private static IntGrid2D createHabitatGrid(MersenneTwisterFast random, BufferedImage mapImage) {
	IntGrid2D habitatField = new IntGrid2D(mapImage.getWidth(), mapImage.getHeight());

	// traverse habitat field and populate from map image
	for (int y = 0; y < habitatField.getHeight(); y++) {
	    for (int x = 0; x < habitatField.getWidth(); x++) {
		Color color = new Color(mapImage.getRGB(x, y));
		Habitat curHabitat = Habitat.valueOf(color);

		if (curHabitat == null) {
		    logger.warning("Color " + color + " in image " + mapImage + " is not associated to a habitat type. "
			    + "Using default.");
		    curHabitat = Habitat.DEFAULT;
		}

		habitatField.set(x, y, curHabitat.ordinal());
	    }
	}

	return habitatField;
    }

    /**
     * Creates food field populated by random values of available food within
     * range from {@link Habitat} definitions.
     * 
     * @see Habitat#getFoodDensityRange()
     * @param habitatField
     * @param random
     * @return populated food field
     */
    private static DoubleGrid2D createFoodGrid(IntGrid2D habitatField, MersenneTwisterFast random) {
	DoubleGrid2D foodField = new DoubleGrid2D(habitatField.getWidth(), habitatField.getHeight());
	// traverse food grid and populate from habitat rules
	for (int y = 0; y < foodField.getHeight(); y++) {
	    for (int x = 0; x < foodField.getWidth(); x++) {
		Habitat currentHabitat = Habitat.values()[habitatField.get(x, y)];

		double foodRange = currentHabitat.getFoodDensityRange().getEstimatedValue();
		// random value between 0 and range
		double foodVal = random.nextDouble() * foodRange;
		foodField.set(x, y, foodVal);
	    }
	}

	return foodField;
    }

    /**
     * Creates food potential map from {@code foodGrid}. A {@link ConvolveOp} is
     * involved to scale the food density values into the range of 0 - 1.
     * 
     * @param foodGrid
     * @return food potential map component
     */
    private static ConvolvingPotentialMap createFoodPotentialMap(DoubleGrid2D foodGrid) {
	// create kernel that blurs into values ranging from 0 - 1
	Kernel foodPotentialMapKernel = Kernel.getNeutral()
		.multiply(PotentialMap.MAX_ATTRACTIVE_VALUE / Habitat.MAX_FOOD_RANGE);
	ConvolvingPotentialMap foodPotentialMap = new ConvolvingPotentialMap(new ConvolveOp(foodPotentialMapKernel),
		foodGrid);
	return foodPotentialMap;
    }

    /**
     * Creates a {@link GlobalFlowMap} with influences from food availability.
     * 
     * @param foodPotentialMap
     * @return {@code GlobalFlowMap} component
     */
    private static GlobalFlowMap createGlobalFlowMap(PotentialMap foodPotentialMap) {
	GlobalFlowMap globalFlowMap = new GlobalFlowMap(foodPotentialMap.getWidth(), foodPotentialMap.getHeight());
	globalFlowMap.setFoodPotentialMap(foodPotentialMap);
	return globalFlowMap;
    }

}

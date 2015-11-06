package de.zmt.ecs.factory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.imageio.ImageIO;

import de.zmt.ecs.*;
import de.zmt.ecs.component.environment.*;
import de.zmt.pathfinding.*;
import de.zmt.pathfinding.filter.*;
import de.zmt.util.*;
import ec.util.MersenneTwisterFast;
import sim.field.grid.*;
import sim.params.def.EnvironmentDefinition;
import sim.util.*;

/**
 * Factory for creating the environment entity.
 * 
 * @author mey
 *
 */
class EnvironmentFactory implements EntityFactory {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EnvironmentFactory.class.getName());

    private static final String ENVIRONMENT_ENTITY_NAME = "Environment";

    private final EnvironmentDefinition definition;

    /**
     * Construct factory for environments from {@code definition}.
     * 
     * @param definition
     */
    public EnvironmentFactory(EnvironmentDefinition definition) {
	super();
	this.definition = definition;
    }

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random) {
	return new Entity(manager, ENVIRONMENT_ENTITY_NAME, createComponents(random));
    }

    /**
     * 
     * @param random
     * @return components for the environment entity
     */
    private Collection<Component> createComponents(MersenneTwisterFast random) {
	BufferedImage mapImage = loadMapImage(EnvironmentDefinition.RESOURCES_DIR + definition.getMapImageFilename());

	// create fields
	IntGrid2D habitatGrid = MapUtil.createHabitatGridFromMap(random, mapImage);
	// no normals needed at the moment
	int mapWidth = habitatGrid.getWidth();
	int mapHeight = habitatGrid.getHeight();
	ObjectGrid2D normalGrid = new ObjectGrid2D(mapWidth, mapHeight);
	// ObjectGrid2D normalGrid = MapUtil
	// .createNormalGridFromHabitats(habitatGrid);
	DoubleGrid2D foodGrid = MapUtil.createFoodFieldFromHabitats(habitatGrid, random);
	Double2D worldBounds = definition.mapToWorld(new Int2D(mapWidth, mapHeight));

	ConvolvingPotentialMap foodPotentialMap = createFoodPotentialMap(foodGrid);
	GlobalFlowMap globalFlowMap = createGlobalFlowMap(foodPotentialMap,
		new SimplePotentialMap(createFilteredRiskField(habitatGrid)));

	// gather components
	Collection<Component> components = Arrays.asList(definition, new AgentWorld(worldBounds.x, worldBounds.y),
		new FoodMap(foodGrid, foodPotentialMap), new HabitatMap(habitatGrid), new NormalMap(normalGrid),
		new SimulationTime(EnvironmentDefinition.START_INSTANT), globalFlowMap);

	return components;
    }

    /**
     * Creates food potential map from {@code foodGrid}. A {@link ConvolveOp} is
     * involved to blur the values and create a smoother flow. This makes agents
     * prefer regions with high food density instead of single locations.
     * 
     * @param foodGrid
     * @return food potential map component
     */
    private ConvolvingPotentialMap createFoodPotentialMap(DoubleGrid2D foodGrid) {
	// create kernel that blurs into values ranging from 0 - 1
	Kernel foodPotentialMapKernel = new NoTrapBlurKernel().multiply(1 / Habitat.MAX_FOOD_RANGE);
	ConvolvingPotentialMap foodPotentialMap = new ConvolvingPotentialMap(new ConvolveOp(foodPotentialMapKernel),
		foodGrid);
	return foodPotentialMap;
    }

    /**
     * Creates a {@link GlobalFlowMap} with influences from food availability
     * and predation risk.
     * 
     * @param foodPotentialMap
     * @param riskPotentialMap
     * @return {@code GlobalFlowMap} component
     */
    private GlobalFlowMap createGlobalFlowMap(PotentialMap foodPotentialMap, PotentialMap riskPotentialMap) {
	GlobalFlowMap globalFlowMap = new GlobalFlowMap(foodPotentialMap.getWidth(), foodPotentialMap.getHeight());
	globalFlowMap.addMap(foodPotentialMap);
	globalFlowMap.setRiskPotentialMap(riskPotentialMap);
	return globalFlowMap;
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
     * 
     * @param habitatMap
     * @return filtered predation risk field
     */
    private static DoubleGrid2D createFilteredRiskField(IntGrid2D habitatMap) {
	DoubleGrid2D riskFieldSrc = MapUtil.createPredationRiskFieldFromHabitats(habitatMap);
	// kernel creating negative values making high risks drive the fish away
	Kernel kernel = new NoTrapBlurKernel()
		.multiply(-1 / Habitat.MAX_PREDATION_RISK.doubleValue(UnitConstants.PER_STEP));
	ConvolveOp op = new ConvolveOp(kernel);
	return op.filter(riskFieldSrc, null);
    }

}
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
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.pathfinding.FilteringPotentialMap;
import de.zmt.pathfinding.EdgeHandler;
import de.zmt.pathfinding.MapChangeNotifier.UpdateMode;
import de.zmt.pathfinding.MapType;
import de.zmt.pathfinding.PotentialMap;
import de.zmt.pathfinding.SimplePotentialMap;
import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.pathfinding.filter.Kernel;
import de.zmt.util.Habitat;
import ec.util.MersenneTwisterFast;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
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

    @Override
    public Entity create(EntityManager manager, MersenneTwisterFast random, EnvironmentDefinition definition) {
	return new EnvironmentEntity(manager, createComponents(random, definition));
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
	DoubleGrid2D foodGrid = createFoodGrid(habitatGrid, random);
	Double2D worldBounds = definition.mapToWorld(new Int2D(mapWidth, mapHeight));

	HabitatMap habitatMap = new HabitatMap(habitatGrid);
	FilteringPotentialMap foodPotentialMap = createFoodPotentialMap(foodGrid);
	PotentialMap boundaryPotentialMap = createBoundaryPotentialMap(habitatMap);
	GlobalPathfindingMaps globalPathfindingMaps = new GlobalPathfindingMaps(foodPotentialMap, boundaryPotentialMap);

	// gather components
	Collection<Component> components = Arrays.asList(definition, new AgentWorld(worldBounds.x, worldBounds.y),
		new FoodMap(foodGrid, foodPotentialMap), globalPathfindingMaps, habitatMap,
		new SimulationTime(EnvironmentDefinition.START_INSTANT), new SpeciesPathfindingMaps.Container());

	return components;
    }

    /**
     * Constructs a potential map with repulsive values at map and
     * {@link Habitat#MAINLAND} boundaries.
     * 
     * @param habitatMap
     * @return {@link PotentialMap} with repulsion at boundaries
     */
    private static PotentialMap createBoundaryPotentialMap(HabitatMap habitatMap) {
	int width = habitatMap.getWidth();
	int height = habitatMap.getHeight();
	DoubleGrid2D boundaryPotentialGrid = new DoubleGrid2D(width, height);

	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		double boundaryValue = 0;
		// mark MAINLAND as repulsive
		if (habitatMap.obtainHabitat(x, y) == Habitat.MAINLAND) {
		    boundaryValue = -1;
		}
		boundaryPotentialGrid.set(x, y, boundaryValue);
	    }
	}

	EdgeHandler repulsiveEdgesHandler = new EdgeHandler(-1);
	SimplePotentialMap boundaryPotentialMap = new SimplePotentialMap(boundaryPotentialGrid, repulsiveEdgesHandler);
	boundaryPotentialMap.setName(MapType.BOUNDARY.getPotentialMapName());
	return boundaryPotentialMap;
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
    private static FilteringPotentialMap createFoodPotentialMap(DoubleGrid2D foodGrid) {
	// create kernel that blurs into values ranging from 0 - 1
	Kernel foodPotentialMapKernel = Kernel.getNeutral()
		.multiply(PotentialMap.MAX_ATTRACTIVE_VALUE / Habitat.MAX_FOOD_RANGE);
	FilteringPotentialMap foodPotentialMap = new FilteringPotentialMap(new ConvolveOp(foodPotentialMapKernel),
		foodGrid);
	foodPotentialMap.setUpdateMode(UpdateMode.EAGER);
	foodPotentialMap.setName(MapType.FOOD.getPotentialMapName());
	return foodPotentialMap;
    }

    private static class EnvironmentEntity extends Entity {
	private static final long serialVersionUID = 1L;

	private static final String ENTITY_NAME = "Environment";

	public EnvironmentEntity(EntityManager manager, Collection<Component> components) {
	    super(manager, ENTITY_NAME, components);
	}

	@Override
	protected Collection<? extends Component> getComponentsToInspect() {
	    return get(Arrays.asList(AgentWorld.class, SimulationTime.class, GlobalPathfindingMaps.class,
		    SpeciesPathfindingMaps.Container.class));
	}
    }
}

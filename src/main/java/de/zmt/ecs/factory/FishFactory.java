package de.zmt.ecs.factory;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Power;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Compartments;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.Sex;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.GlobalPathfindingMaps;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SpeciesPathfindingMaps;
import de.zmt.ecs.component.environment.WorldDimension;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.pathfinding.FlowMap;
import de.zmt.pathfinding.PathfindingMapType;
import de.zmt.pathfinding.PotentialMap;
import de.zmt.pathfinding.SimplePotentialMap;
import de.zmt.pathfinding.filter.BasicMorphOp;
import de.zmt.pathfinding.filter.ConvolveOp;
import de.zmt.pathfinding.filter.Kernel;
import de.zmt.pathfinding.filter.KernelFactory;
import de.zmt.storage.Compartment.Type;
import de.zmt.storage.ExcessStorage;
import de.zmt.storage.FatStorage;
import de.zmt.storage.Gut;
import de.zmt.storage.ProteinStorage;
import de.zmt.storage.ReproductionStorage;
import de.zmt.storage.ShorttermStorage;
import de.zmt.util.FormulaUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.field.grid.BooleanGrid2D;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Fixed2D;
import sim.portrayal.Oriented2D;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Rotation2D;;

/**
 * Factory for creating fish entities.
 * 
 * @author mey
 *
 */
class FishFactory implements EntityFactory<FishFactory.MyParam> {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FishFactory.class.getName());

    /** Fish can spawn everywhere but not in {@link Habitat#MAINLAND}. */
    private static final Set<Habitat> SPAWN_HABITATS = EnumSet.complementOf(EnumSet.of(Habitat.MAINLAND));

    @Override
    public Entity create(EntityManager manager, MyParam parameter) {
        Entity environment = parameter.environment;
        SpeciesDefinition definition = parameter.definition;
        MersenneTwisterFast random = parameter.random;

        SpeciesPathfindingMaps.Container speciesPathfindingMaps = environment
                .get(SpeciesPathfindingMaps.Container.class);
        if (speciesPathfindingMaps.get(definition) == null) {
            speciesPathfindingMaps.put(definition, createSpeciesFlowMaps(environment, definition));
        }

        Int2D randomHabitatPosition = environment.get(HabitatMap.class).generateRandomPosition(random, SPAWN_HABITATS);
        Double2D position = environment.get(EnvironmentDefinition.class).mapToWorld(randomHabitatPosition);

        final FishEntity fishEntity = new FishEntity(manager, definition.getName(),
                createComponents(random, position, parameter));

        return fishEntity;
    }

    @Override
    public Entity load(EntityManager manager, UUID uuid) {
        return new FishEntity(manager, uuid);
    }

    private static SpeciesPathfindingMaps createSpeciesFlowMaps(Entity environment, SpeciesDefinition definition) {
        HabitatMap habitatMap = environment.get(HabitatMap.class);

        DoubleGrid2D rawRiskGrid = createPredationRiskGrid(habitatMap, definition);
        DoubleGrid2D rawToForagingGrid = createHabitatAttractionGrid(
                definition.getPreferredHabitats(BehaviorMode.FORAGING), habitatMap);
        DoubleGrid2D rawToRestingGrid = createHabitatAttractionGrid(
                definition.getPreferredHabitats(BehaviorMode.RESTING), habitatMap);

        // even without blur the agent can perceive the adjacent cells
        double blurRadius = definition.getPerceptionRadius().doubleValue(UnitConstants.WORLD_DISTANCE) - 1;
        Kernel perceptionBlur = KernelFactory.createGaussianBlur(blurRadius);
        // make risk values range from -1 to 0
        double riskShift = -definition.getMinPredationRisk().doubleValue(UnitConstants.PER_STEP);
        double riskScale = PotentialMap.MAX_REPULSIVE_VALUE
                / definition.getMaxPredationRisk().doubleValue(UnitConstants.PER_STEP);

        // shrink mainland so that there is no influence on accessible areas
        rawRiskGrid = shrinkMainland(definition, habitatMap, perceptionBlur, rawRiskGrid);
        PotentialMap riskPotentialMap = createFilteredPotentialMap(rawRiskGrid.add(riskShift).multiply(riskScale),
                perceptionBlur, PathfindingMapType.RISK.getPotentialMapName());
        PotentialMap toForagingPotentialMap = createFilteredPotentialMap(rawToForagingGrid, perceptionBlur,
                PathfindingMapType.TO_FORAGE.getPotentialMapName());
        PotentialMap toRestingPotentialMap = createFilteredPotentialMap(rawToRestingGrid, perceptionBlur,
                PathfindingMapType.TO_REST.getPotentialMapName());

        return new SpeciesPathfindingMaps(environment.get(GlobalPathfindingMaps.class), riskPotentialMap,
                toForagingPotentialMap, toRestingPotentialMap, definition);
    }

    /**
     * Creates a grid containing predation risks.
     * 
     * @param habitatMap
     * @param definition
     * @return field of predation risks
     */
    private static DoubleGrid2D createPredationRiskGrid(HabitatMap habitatMap, SpeciesDefinition definition) {
        int width = habitatMap.getWidth();
        int height = habitatMap.getHeight();
        DoubleGrid2D riskGrid = new DoubleGrid2D(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Habitat habitat = habitatMap.obtainHabitat(x, y);
                double riskPerStep = definition.getPredationRisk(habitat).doubleValue(UnitConstants.PER_STEP);
                riskGrid.set(x, y, riskPerStep);
            }
        }
        return riskGrid;
    }

    /**
     * Shrinks mainland in given risk grid to remove influence on pathfinding
     * within accessible areas. The amount of shrinkage is proportional to the
     * applied blur.
     * 
     * @param definition
     * @param habitatMap
     * @param perceptionBlur
     *            the blur kernel to protect against
     * @param riskGrid
     * @return copy of risk grid with shrunken mainland areas
     */
    private static DoubleGrid2D shrinkMainland(SpeciesDefinition definition, HabitatMap habitatMap,
            Kernel perceptionBlur, DoubleGrid2D riskGrid) {
        int width = riskGrid.getWidth();
        int height = riskGrid.getHeight();
        BooleanGrid2D mainlandSelection = new BooleanGrid2D(width, height);
        double mainlandRiskValue = definition.getPredationRisk(Habitat.MAINLAND).doubleValue(UnitConstants.PER_STEP);

        // shrink mainland according to blur kernel
        for (int i = 0; i < perceptionBlur.getxOrigin() + 1; i++) {
            // re-select untouched mainland areas
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    boolean untouched = habitatMap.obtainHabitat(x, y) == Habitat.MAINLAND
                            && riskGrid.get(x, y) == mainlandRiskValue;
                    mainlandSelection.set(x, y, untouched);
                }
            }
            // ... and grow enclosing areas
            riskGrid = BasicMorphOp.getDefaultErode().filter(riskGrid, mainlandSelection);
        }
        return riskGrid;
    }

    /**
     * Creates a grid with maximum attractive potential for given habitats.
     * 
     * @param attractingHabitats
     *            the habitats that will attract the agent
     * @param habitatMap
     * @return grid that attracts towards {@code attractingHabitats}
     */
    private static DoubleGrid2D createHabitatAttractionGrid(Set<Habitat> attractingHabitats, HabitatMap habitatMap) {
        DoubleGrid2D attractionGrid = new DoubleGrid2D(habitatMap.getWidth(), habitatMap.getHeight());

        for (int y = 0; y < attractionGrid.getHeight(); y++) {
            for (int x = 0; x < attractionGrid.getWidth(); x++) {
                if (attractingHabitats.contains(habitatMap.obtainHabitat(x, y))) {
                    attractionGrid.set(x, y, PotentialMap.MAX_ATTRACTIVE_VALUE);
                }
            }
        }

        return attractionGrid;
    }

    /**
     * Creates a {@link PotentialMap} from given grid, filtered by a
     * {@link ConvolveOp} with given kernel.
     * 
     * @param grid
     *            the grid to filter
     * @param kernel
     *            the kernel used for the filtering
     * @param name
     *            the name set to the created potential map
     * @return a {@link PotentialMap} from the filtered grid
     */
    private static PotentialMap createFilteredPotentialMap(DoubleGrid2D grid, Kernel kernel, String name) {
        DoubleGrid2D filteredGrid = new ConvolveOp(kernel).filter(grid);
        SimplePotentialMap potentialMap = new SimplePotentialMap(filteredGrid);
        potentialMap.setName(name);
        return potentialMap;
    }

    private static Collection<Component> createComponents(MersenneTwisterFast random, Double2D position,
            MyParam parameter) {
        SpeciesDefinition definition = parameter.definition;
        Entity environment = parameter.environment;
        Amount<Duration> initialAge = parameter.initialAge;

        WorldDimension worldDimension = environment.get(WorldDimension.class);
        FlowMap boundaryFlowMap = environment.get(GlobalPathfindingMaps.class).getBoundaryFlowMap();
        Amount<Duration> maxAge = definition.determineMaxAge(random);

        // compute initial values
        Amount<Length> initialLength = FormulaUtil.expectedLength(definition.getAsymptoticLength(),
                definition.getGrowthCoeff(), initialAge, definition.getZeroSizeAge());
        Amount<Mass> initialBiomass = FormulaUtil.expectedMass(definition.getLengthMassCoeff(), initialLength,
                definition.getLengthMassExponent());
        Amount<Power> initialrestingMetabolicRate = FormulaUtil.restingMetabolicRate(initialBiomass);
        Sex sex = definition.determineSex(random);

        // create components
        Aging aging = new Aging(initialAge, maxAge);
        Metabolizing metabolizing = new Metabolizing(initialrestingMetabolicRate);
        Growing growing = new Growing(initialBiomass, initialLength);
        Memorizing memorizing = new Memorizing(worldDimension.getWidth(), worldDimension.getHeight());
        Moving moving = new Moving();
        moving.setPosition(position, environment.get(EnvironmentDefinition.class));
        moving.setVelocity(Rotation2D.fromAngle(random.nextDouble() * 2 * Math.PI).getVector(), 0);
        LifeCycling lifeCycling = new LifeCycling(sex);
        Flowing flowing = new Flowing(boundaryFlowMap);

        // update phase to match current length
        while (lifeCycling.canChangePhase(definition.canChangeSex())
                && initialLength.isGreaterThan(definition.getNextPhaseLength(lifeCycling.getPhase()))) {
            lifeCycling.enterNextPhase();
        }

        Compartments compartments = createCompartments(metabolizing, growing, aging, definition,
                lifeCycling.isReproductive(), random);

        return Arrays.asList(definition, aging, metabolizing, growing, memorizing, moving, lifeCycling, compartments,
                flowing);
    }

    /**
     * Creates {@link Compartments} component.
     * 
     * @param metabolizing
     * @param growing
     * @param aging
     * @param definition
     * @param reproductive
     * @param random
     * @return {@code Compartments} component
     */
    private static Compartments createCompartments(Metabolizing metabolizing, Growing growing, Aging aging,
            SpeciesDefinition definition, boolean reproductive, MersenneTwisterFast random) {
        ShorttermStorage shortterm = new ShorttermStorage(metabolizing, random.nextDouble());

        // short-term is full at startup: calculate mass
        Amount<Mass> shorttermBiomass = Type.SHORTTERM.toMass(shortterm.getAmount());
        Amount<Mass> remainingBiomass = growing.getBiomass().minus(shorttermBiomass);

        // remaining biomass is distributed in fat and protein storage
        Amount<Energy> initialFat = FormulaUtil.initialFat(remainingBiomass);
        Amount<Energy> initialProtein = FormulaUtil.initialProtein(remainingBiomass);

        Gut gut = new Gut(definition, growing, aging);
        FatStorage fat = new FatStorage(initialFat, growing);
        ProteinStorage protein = new ProteinStorage(initialProtein, growing);
        ReproductionStorage reproduction;
        // if reproductive: random fill
        if (reproductive) {
            reproduction = new ReproductionStorage(growing, random, random.nextDouble());
        } else {
            reproduction = new ReproductionStorage(growing, random);
        }
        ExcessStorage excess = new ExcessStorage(metabolizing);

        return new Compartments(gut, shortterm, fat, protein, reproduction, excess);
    }

    /**
     * Implements {@link Oriented2D} for display.
     * 
     * @author mey
     * 
     */
    private static class FishEntity extends Entity implements Fixed2D, Oriented2D {
        private static final long serialVersionUID = 1L;

        /** Component classes to be displayed when agent is inspected */
        private static final Collection<Class<? extends Component>> CLASSES_TO_INSPECT = Arrays
                .<Class<? extends Component>> asList(Moving.class, Flowing.class, Metabolizing.class, LifeCycling.class,
                        Aging.class, Growing.class, Compartments.class);

        /**
         * Constructs a new {@link FishEntity}.
         * 
         * @param manager
         *            the {@link EntityManager}
         * @param internalName
         *            the name for the entity
         * @param components
         *            the components to add
         */
        public FishEntity(EntityManager manager, String internalName, Collection<Component> components) {
            super(manager, internalName, components);
        }

        /**
         * Constructs a new {@link FishEntity} by loading it from the
         * {@link EntityManager}.
         * 
         * @param manager
         *            the {@link EntityManager} to load from
         * @param uuid
         *            the {@link UUID} of the entity to load from the manager
         */
        protected FishEntity(EntityManager manager, UUID uuid) {
            super(manager, uuid);
        }

        @Override
        protected Collection<? extends Component> getComponentsToInspect() {
            return get(CLASSES_TO_INSPECT);
        }

        @Override
        public double orientation2D() {
            return get(Moving.class).getDirection().angle();
        }

        @Override
        public boolean maySetLocation(Object field, Object newObjectLocation) {
            EnvironmentDefinition environmentDefinition = getParentEntityManager()
                    .getAllComponentsOfType(EnvironmentDefinition.class).stream().findAny().get();
            get(Moving.class).setPosition((Double2D) newObjectLocation, environmentDefinition);

            return true;
        }
    }

    /**
     * Parameter class for {@link FishFactory}.
     * 
     * @author mey
     *
     */
    static class MyParam implements EntityFactory.Parameter {
        private final SpeciesDefinition definition;
        private final Entity environment;
        private final Amount<Duration> initialAge;
        private final MersenneTwisterFast random;

        /**
         * Constructs a {@link FishFactory} parameter object with specified
         * initial age.
         * 
         * @param definition
         *            species definition of the fish
         * @param environment
         *            entity representing the environment the fish is placed
         *            into
         * @param initialAge
         *            the initial age of the created fish
         * @param random
         *            the random number generator to be used
         */
        public MyParam(SpeciesDefinition definition, Entity environment, Amount<Duration> initialAge,
                MersenneTwisterFast random) {
            super();
            if (initialAge.isLessThan(definition.getPostSettlementAge())) {
                throw new IllegalArgumentException("Initial age cannot be lower than post settlement age.");
            }
            this.definition = definition;
            this.environment = environment;
            this.initialAge = initialAge;
            this.random = random;
        }

        /**
         * Constructs a {@link FishFactory} parameter object at post settlement
         * age.
         * 
         * @see SpeciesDefinition#getPostSettlementAge()
         * @param definition
         *            species definition of the fish
         * @param environment
         *            entity representing the environment the fish is placed
         *            into
         * @param random
         */
        public MyParam(SpeciesDefinition definition, Entity environment, MersenneTwisterFast random) {
            this(definition, environment, definition.getPostSettlementAge(), random);
        }
    }
}

package de.zmt.ecs.system.agent.move;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.AgentWorld;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.BehaviorSystem;
import de.zmt.ecs.system.environment.FoodSystem;
import sim.engine.Kitt;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;

/**
 * Executes movement of simulation agents.
 * 
 * @author mey
 *
 */
public class MoveSystem extends AgentSystem {
    /** A {@link MovementStrategy} corresponding to every {@link MoveMode}. */
    private final Map<MoveMode, MovementStrategy> movementStrategies;

    public MoveSystem(Kitt sim) {
	super(sim);

	movementStrategies = new HashMap<>();
	movementStrategies.put(MoveMode.RANDOM, new RandomMovement(getEnvironment(), getRandom()));
	movementStrategies.put(MoveMode.MEMORY, new MemoryMovement(getEnvironment(), getRandom()));
	movementStrategies.put(MoveMode.PERCEPTION, new PerceptionMovement(getEnvironment(), getRandom()));
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Moving.class, SpeciesDefinition.class,
		Flowing.class, Growing.class);
    }

    /**
     * Executes a strategy based on the {@link MoveMode} of the species
     * currently updated.
     */
    @Override
    protected void systemUpdate(Entity entity) {
	// execute movement strategy for selected move mode
	movementStrategies.get(entity.get(SpeciesDefinition.class).getMoveMode()).move(entity);

	Double2D position = entity.get(Moving.class).getPosition();
	// update memory
	if (entity.has(Memorizing.class)) {
	    entity.get(Memorizing.class).increase(position);
	}
	// update field position
	getEnvironment().get(AgentWorld.class).setAgentPosition(entity, position);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
	return Arrays.<Class<? extends EntitySystem>> asList(
		// for food potentials in flow map
		FoodSystem.class,
		// for behavior mode
		BehaviorSystem.class);
    }

    /**
     * Move mode of an agent.
     * 
     * @see MoveSystem
     * @author mey
     *
     */
    public static enum MoveMode {
	/** Pure random walk */
	RANDOM,
	/**
	 * Moves towards areas with the highest food supply in perception range.
	 */
	PERCEPTION,
	/** Moves towards attraction center. */
	// TODO this should be based on Memorizing component
	MEMORY
    }
}

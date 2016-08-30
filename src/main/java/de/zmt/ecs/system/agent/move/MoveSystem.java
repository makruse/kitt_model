package de.zmt.ecs.system.agent.move;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Flowing;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.agent.StepSkipping;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.BehaviorSystem;
import de.zmt.ecs.system.agent.StepSkipSystem;
import de.zmt.ecs.system.environment.FoodSystem;
import de.zmt.params.SpeciesDefinition;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * Executes movement of simulation agents.
 * <p>
 * <img src="doc-files/gen/MoveSystem.svg" alt= "MoveSystem Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/MoveSystem.svg

start
partition "Compute Speed" {
    :current behavior mode<
    :get speed factor associated
    with current BehaviorMode;
    :multiply speed factor by body length
    to get the average speed;
    :apply random deviation to average speed;
    if (MoveMode.PERCEPTION) then (yes)
        :apply habitat speed factor;
    else (no)
    endif
}
partition "Compute Direction" {
    partition "Specify Flow" {
	        :flow: +BOUNDARY;
        if	(MoveMode.PERCEPTION?) then (yes)
            :flow: +RISK;
	        if (feeding?) then (yes)
		        :flow: +FOOD>
		    elseif (MIGRATING?) then (yes)
		        if (next BehaviorMode:\nFORAGING?) then (yes)
		            :flow: +TO_FORAGE>
		        elseif (next BehaviorMode:\nRESTING?) then (yes)
		            :flow: +TO_REST>
                endif
	        endif
	    else (no)
        endif
        :return combined flow>
    }
    :get desired direction
    from flow at current position>
    if (desired direction is neutral?) then (yes)
        :return random direction
        without exceeding
        maximum rotation>
    elseif (difference between\ncurrent and desired direction\nexceeds maximum rotation?) then (yes)
        :return current direction
        with maximum rotation added
        towards desired direction>
    else (no)
        :return desired direction>
    endif
}
:compute new position
from speed and direction;

if (new position is beyond\nmap boundaries?) then (yes)
    :reflect from border;
elseif (new position is in\n MAINLAND?) then (yes)
    :keep old position;
else (no)
endif
stop



@enduml
@formatter:on
 */
public class MoveSystem extends AgentSystem {
    /** A {@link MovementStrategy} corresponding to every {@link MoveMode}. */
    private final Map<MoveMode, MovementStrategy> movementStrategies = new HashMap<>();

    {
        movementStrategies.put(MoveMode.RANDOM, new RandomMovement());
        movementStrategies.put(MoveMode.PERCEPTION, new PerceptionMovement());
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.<Class<? extends Component>> asList(Metabolizing.class, Moving.class, SpeciesDefinition.class,
                Flowing.class, Growing.class, StepSkipping.class);
    }

    /**
     * Executes a strategy based on the {@link MoveMode} of the species
     * currently updated.
     */
    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Kitt kittState = (Kitt) state;
        // execute movement strategy for selected move mode
        movementStrategies.get(entity.get(SpeciesDefinition.class).getMoveMode()).move(entity, kittState);

        Double2D position = entity.get(Moving.class).getWorldPosition();
        // update memory
        if (entity.has(Memorizing.class)) {
            entity.get(Memorizing.class).increase(position);
        }
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(
                // for food potentials in flow map
                FoodSystem.class,
                // for behavior mode
                BehaviorSystem.class,
                // for updating the delta time
                StepSkipSystem.class);
    }

    /**
     * Move mode of an agent.
     * 
     * @see MoveSystem
     * @author mey
     *
     */
    @XStreamAlias("MoveMode")
    public static enum MoveMode {
        /** Pure random walk */
        RANDOM,
        /**
         * Moves towards areas with the highest food supply in perception range.
         */
        PERCEPTION,
    }
}

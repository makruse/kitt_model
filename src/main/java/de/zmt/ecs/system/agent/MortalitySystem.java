package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.ecs.component.environment.HabitatMap;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.ecs.system.AgentSystem;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.params.EnvironmentDefinition;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.util.Double2D;

/**
 * This system kills agents according to mortality risks.
 * <p>
 * <img src="doc-files/gen/MortalitySystem.svg" alt=
 * "MortalitySystem Activity Diagram">
 * 
 * @author mey
 *
 */
/*
@formatter:off
@startuml doc-files/gen/MortalitySystem.svg

start
if (coin flip successful\non habitat mortality risk?) then (yes)
	:agent dies;
	end
else if (first step in day **AND**\ncoin flip successful on\nrandom mortality risk?) then (yes)
    :agent dies;
    end
endif
stop

@enduml
@formatter:on
 */
public class MortalitySystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(MortalitySystem.class.getName());

    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Entity environment = ((Kitt) state).getEnvironment();
        // habitat mortality per step (because it changes all the time)
        Double2D position = entity.get(Moving.class).getPosition();
        WorldToMapConverter converter = environment.get(EnvironmentDefinition.class);
        Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(position, converter);
        Amount<Frequency> predationRisk = entity.get(SpeciesDefinition.class).getPredationRisk(habitat);

        if (state.random.nextBoolean(predationRisk.doubleValue(UnitConstants.PER_STEP))) {
            killAgent(entity, CauseOfDeath.HABITAT);
        }
        // check for natural mortality just once per day
        else if (environment.get(SimulationTime.class).isFirstStepInDay() && state.random.nextBoolean(
                entity.get(SpeciesDefinition.class).getNaturalMortalityRisk().doubleValue(UnitConstants.PER_DAY))) {
            killAgent(entity, CauseOfDeath.RANDOM);
        }
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.<Class<? extends Component>> asList(Moving.class, SpeciesDefinition.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.<Class<? extends EntitySystem>> asList(MoveSystem.class);
    }
}

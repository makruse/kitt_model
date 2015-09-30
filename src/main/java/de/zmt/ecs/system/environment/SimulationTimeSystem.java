package de.zmt.ecs.system.environment;

import static javax.measure.unit.SI.*;

import java.util.*;

import org.joda.time.Duration;

import de.zmt.ecs.*;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.ecs.system.KittSystem;
import de.zmt.sim.params.def.EnvironmentDefinition;

/**
 * Advances simulation time on each step.
 * 
 * @author cmeyer
 * 
 */
public class SimulationTimeSystem extends KittSystem {
    /** Converted {@link EnvironmentDefinition#STEP_DURATION} to yoda format */
    private static final Duration STEP_DURATION_YODA = new Duration(
	    EnvironmentDefinition.STEP_DURATION.to(MILLI(SECOND))
		    .getExactValue());

    @Override
    protected void systemUpdate(Entity entity) {
	entity.get(SimulationTime.class).addTime(STEP_DURATION_YODA);

    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Collections
		.<Class<? extends Component>> singleton(SimulationTime.class);
    }
}
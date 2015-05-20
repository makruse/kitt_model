package de.zmt.kitt.ecs.system.environment;

import static javax.measure.unit.SI.*;

import java.util.*;

import org.joda.time.Duration;

import de.zmt.kitt.ecs.component.environment.SimulationTime;
import de.zmt.kitt.ecs.system.AbstractKittSystem;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import ecs.*;

/**
 * Advances simulation time on each step.
 * 
 * @author cmeyer
 * 
 */
public class SimulationTimeSystem extends AbstractKittSystem {
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

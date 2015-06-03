package de.zmt.kitt.ecs.system.agent;

import static javax.measure.unit.SI.SECOND;

import java.util.*;

import sim.util.*;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.ecs.component.agent.Metabolizing.ActivityType;
import de.zmt.kitt.ecs.component.environment.*;
import de.zmt.kitt.ecs.system.AbstractAgentSystem;
import de.zmt.kitt.sim.*;
import de.zmt.kitt.sim.params.def.*;
import de.zmt.kitt.util.UnitConstants;
import ecs.*;

public class MoveSystem extends AbstractAgentSystem {
    public MoveSystem(KittSim sim) {
	super(sim);
    }

    @Override
    public void onAdd(EntityManager manager) {
    }

    @Override
    protected void systemUpdate(Entity entity) {
	Moving moving = entity.get(Moving.class);
	Metabolizing metabolizing = entity.get(Metabolizing.class);
	SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
	// attraction is optional
	AttractionCenters attractionCenters = entity
		.has(AttractionCenters.class) ? entity
		.get(AttractionCenters.class) : null;

	Double2D velocity = computeVelocity(metabolizing.getActivityType(),
		moving.getPosition(), definition, attractionCenters);
	moving.setPosition(computePosition(moving.getPosition(), velocity));
	moving.setVelocity(velocity);

	if (entity.has(Memorizing.class)) {
	    entity.get(Memorizing.class).increase(moving.getPosition());
	}

	// update field position
	environment.get(AgentWorld.class).setAgentPosition(entity,
		moving.getPosition());
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	return Arrays.<Class<? extends Component>> asList(Metabolizing.class,
		Moving.class, SpeciesDefinition.class);
    }

    /**
     * Calculate velocity to migrate towards attraction center with reducing
     * focus the closer the fish is, gradually changing to pure random walk
     * within the preferred area.
     * <p>
     * If {@link SpeciesDefinition#isAttractionEnabled()} is set to
     * <code>false</code> velocity for random walk will always be returned.
     * 
     * @return velocity in m/s
     * 
     */
    private Double2D computeVelocity(ActivityType activityType,
	    Double2D position, SpeciesDefinition definition,
	    AttractionCenters attractionCenters) {
	// TODO species-dependent foraging / resting cycle
	double baseSpeed = (activityType == ActivityType.FORAGING ? definition
		.getSpeedForaging() : definition.getSpeedResting()).to(
		UnitConstants.VELOCITY).getEstimatedValue();
	double speedDeviation = random.nextGaussian()
		* definition.getSpeedDeviation() * baseSpeed;

	Double2D attractionDir = new Double2D();
	Double2D randomDir = new Double2D(random.nextGaussian(),
		random.nextGaussian()).normalize();
	double willToMigrate = 0;

	if (attractionCenters != null) {
	    double distance;
	    if (activityType == ActivityType.FORAGING) {
		distance = position.distance(attractionCenters
			.getForagingCenter());
		attractionDir = attractionCenters.getForagingCenter()
			.subtract(position).normalize();
	    } else {
		distance = position.distance(attractionCenters
			.getRestingCenter());
		attractionDir = attractionCenters.getRestingCenter()
			.subtract(position).normalize();
	    }

	    // will to migrate towards attraction (0 - 1)
	    // tanh function to reduce bias as the fish moves closer
	    willToMigrate = Math.tanh(distance
		    / definition.getMaxAttractionDistance().doubleValue(
			    UnitConstants.MAP_DISTANCE) * Math.PI);
	}
	// weight directed and random walk according to migration willingness
	Double2D weightedAttractionDir = attractionDir.multiply(willToMigrate);
	Double2D weightedRandomDir = randomDir.multiply(1 - willToMigrate);

	return (weightedAttractionDir.add(weightedRandomDir))
		.multiply(baseSpeed + speedDeviation);
    }

    /**
     * Integrates velocity by adding it to position and reflect from obstacles.
     * The field is updated with the new position as well.
     */
    private Double2D computePosition(Double2D oldPosition, Double2D velocity) {
	double delta = EnvironmentDefinition.STEP_DURATION.to(SECOND)
		.getEstimatedValue();
	// multiply velocity with delta time (minutes) and add it to pos
	MutableDouble2D newPosition = new MutableDouble2D(
		oldPosition.add(velocity.multiply(delta)));

	// reflect on vertical border - invert horizontal velocity
	AgentWorld agentWorld = environment.get(AgentWorld.class);
	if (newPosition.x >= agentWorld.getWidth() || newPosition.x < 0) {
	    newPosition.x = oldPosition.x - velocity.x;
	}
	// reflect on horizontal border - invert vertical velocity
	if (newPosition.y >= agentWorld.getHeight() || newPosition.y < 0) {
	    newPosition.y = oldPosition.y - velocity.y;
	}

	Habitat habitat = environment.get(HabitatMap.class).obtainHabitat(
		new Double2D(newPosition),
		environment.get(EnvironmentDefinition.class));

	// stay away from main land // TODO reflect by using normals
	if (habitat == Habitat.MAINLAND) {
	    newPosition = new MutableDouble2D(oldPosition);
	}

	return new Double2D(newPosition);
    }
}

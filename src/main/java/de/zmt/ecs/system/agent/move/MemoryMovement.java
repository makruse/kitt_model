package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.AttractionCenters;
import de.zmt.ecs.component.agent.Metabolizing;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.util.DirectionUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.params.def.SpeciesDefinition;
import sim.util.Double2D;

/**
 * Strategy for moving the entity towards its attraction centers.
 * 
 * @author mey
 * 
 */
class MemoryMovement extends DesiredDirectionMovement {

    public MemoryMovement(Entity environment, MersenneTwisterFast random) {
	super(environment, random);
    }

    /**
     * Computes direction based on the center the entity is currently attracted
     * to. The less the distance from the center is, the more the resulting
     * direction will be randomized, making the movement become chaotic when in
     * proximity to the center.
     */
    @Override
    protected Double2D computeDesiredDirection(Entity entity) {
	BehaviorMode behaviorMode = entity.get(Metabolizing.class).getBehaviorMode();
	Double2D attractionCenter = entity.get(AttractionCenters.class).obtainCenter(behaviorMode);
	Double2D position = entity.get(Moving.class).getPosition();
	SpeciesDefinition definition = entity.get(SpeciesDefinition.class);

	double distance = position.distance(attractionCenter);
	Double2D attractionDir = attractionCenter.subtract(position).normalize();

	// will to migrate towards attraction (0 - 1)
	// tanh function to reduce bias as the fish moves closer
	double willToMigrate = Math.tanh(
		distance / definition.getMaxAttractionDistance().doubleValue(UnitConstants.WORLD_DISTANCE) * Math.PI);

	// weight influences according to migration willingness
	Double2D weightedAttractionDir = attractionDir.multiply(willToMigrate);
	Double2D weightedRandomDir = DirectionUtil.generate(getRandom()).multiply(1 - willToMigrate);

	return weightedAttractionDir.add(weightedRandomDir);
    }
}
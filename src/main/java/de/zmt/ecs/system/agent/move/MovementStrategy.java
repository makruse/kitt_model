package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;
import sim.engine.Kitt;

interface MovementStrategy {
    /**
     * Move the entity according to the strategy.
     * 
     * @param entity
     *            the entity to move
     * @param state
     *            the current simulation state
     */
    void move(Entity entity, Kitt state);
}
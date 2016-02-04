package de.zmt.ecs.system.agent.move;

import de.zmt.ecs.Entity;

interface MovementStrategy {
    /**
     * Move the entity according to the strategy.
     * 
     * @param entity
     */
    void move(Entity entity);
}
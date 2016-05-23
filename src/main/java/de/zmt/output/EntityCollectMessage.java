package de.zmt.output;

import de.zmt.ecs.Entity;
import de.zmt.output.message.CollectMessage;
import de.zmt.output.message.SimpleCollectMessage;

/**
 * A {@link CollectMessage} containing an {@link Entity}.
 * 
 * @author mey
 *
 */
class EntityCollectMessage extends SimpleCollectMessage<Entity> {

    public EntityCollectMessage(Entity simObject) {
	super(simObject);
    }
}

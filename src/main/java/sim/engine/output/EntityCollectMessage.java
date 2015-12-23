package sim.engine.output;

import de.zmt.ecs.Entity;
import sim.engine.output.message.*;

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

package de.zmt.output;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.output.message.CollectMessage;
import de.zmt.output.message.CollectMessageFactory;
import de.zmt.output.message.SimpleCollectMessage;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * A {@link CollectMessage} containing an agent, i.e. an {@link Entity} with a
 * {@link Moving} component.
 * 
 * @author mey
 *
 */
class AgentCollectMessage extends SimpleCollectMessage<Entity> {
    /** Factory for this message. */
    public static final CollectMessageFactory<AgentCollectMessage> FACTORY = new Factory();

    private AgentCollectMessage(Entity simObject) {
        super(simObject);
    }

    private static class Factory implements CollectMessageFactory<AgentCollectMessage> {
        /** Creates a message for every simulation agent. */
        @SuppressWarnings("unchecked")
        @Override
        public Stream<AgentCollectMessage> createCollectMessages(SimState state) {
            EntityManager manager = ((Kitt) state).getEntityCreationHandler().getManager();
            // get all entities with moving component - these are agents
            Set<UUID> agentUuids = manager.getAllEntitiesPossessingComponent(Moving.class);
            return agentUuids.stream().map(agentUuid -> Entity.loadFromEntityManager(manager, agentUuid))
                    .map(entity -> new AgentCollectMessage(entity));
        }
    }
}

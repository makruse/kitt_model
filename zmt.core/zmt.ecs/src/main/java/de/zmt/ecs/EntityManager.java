package de.zmt.ecs;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;

import sim.engine.Schedule;

/**
 * Manages systems as well to allow entities update themselves when stepped by
 * MASON's Schedule.
 * 
 * @author adam
 * @author mey
 * 
 * @see <a href="http://entity-systems.wikidot.com/rdbms-with-code-in-systems">
 *      Entity Systems Wiki: Standard Design</a>
 */

public class EntityManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean frozen;
    private final Collection<UUID> allEntities;
    private final HashMap<UUID, String> entityHumanReadableNames;

    private final HashMap<Class<? extends Component>, HashMap<UUID, ? extends Component>> componentStores;

    private final EntitySystems entitySystems = new EntitySystems();

    public EntityManager() {
        frozen = false;
        allEntities = new HashSet<>();
        entityHumanReadableNames = new HashMap<>();
        componentStores = new HashMap<>();
    }

    public <T extends Component> T getComponent(UUID entity, Class<T> componentType) {
        synchronized (componentStores) {
            HashMap<UUID, ? extends Component> store = componentStores.get(componentType);

            if (store == null) {
                throw new IllegalArgumentException(
                        "GET FAIL: there are no entities with a Component of class: " + componentType);
            }

            @SuppressWarnings("unchecked")
            T result = (T) store.get(entity);

            if (result == null) {
                /*
                 * DEFAULT: normal debug info:
                 */
                // throw new IllegalArgumentException("GET FAIL: " + entity
                // + "(name:" + nameFor(entity) + ")"
                // + " does not possess Component of class\n missing: "
                // + componentType);
                /*
                 * OPTIONAL: more detailed debug info:
                 */
                StringBuffer sb = new StringBuffer();
                for (UUID e : store.keySet()) {
                    sb.append("\nUUID: " + e + " === " + store.get(e));
                }

                throw new IllegalArgumentException("GET FAIL: " + entity + "(name:" + nameFor(entity) + ")"
                        + " does not possess Component of class\n   missing: " + componentType
                        + "\nTOTAL STORE FOR THIS COMPONENT CLASS : " + sb.toString());

            }

            return result;
        }
    }

    public void removeComponent(UUID entity, Component component) {
        synchronized (componentStores) {
            HashMap<UUID, ? extends Component> store = componentStores.get(component.getClass());

            if (store == null) {
                throw new IllegalArgumentException(
                        "REMOVE FAIL: there are no entities with a Component of class: " + component.getClass());
            }

            @SuppressWarnings("unchecked")
            Component result = store.remove(entity);
            if (result == null) {
                throw new IllegalArgumentException("REMOVE FAIL: " + entity + "(name:" + nameFor(entity) + ")"
                        + " does not possess Component of class\n   missing: " + component.getClass());
            }
        }
    }

    public boolean hasComponent(UUID entity, Class<?> componentType) {
        synchronized (componentStores) {
            HashMap<UUID, ? extends Component> store = componentStores.get(componentType);

            if (store == null) {
                return false;
            } else {
                return store.containsKey(entity);
            }
        }
    }

    /**
     * WARNING: low performance implementation!
     * 
     * @param entity
     *            as {@link UUID}
     * @return all components from {@code entity}
     */
    public Collection<Component> getAllComponentsOnEntity(UUID entity) {
        synchronized (componentStores) {
            LinkedList<Component> components = new LinkedList<>();

            for (HashMap<UUID, ? extends Component> store : componentStores.values()) {
                if (store == null) {
                    continue;
                }

                Component componentFromThisEntity = store.get(entity);

                if (componentFromThisEntity != null) {
                    components.addLast(componentFromThisEntity);
                }
            }

            return components;
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> Collection<T> getAllComponentsOfType(Class<T> componentType) {
        synchronized (componentStores) {
            HashMap<UUID, ? extends Component> store = componentStores.get(componentType);

            if (store == null) {
                return new LinkedList<>();
            }

            return (Collection<T>) store.values();
        }
    }

    public <T extends Component> Set<UUID> getAllEntitiesPossessingComponent(Class<T> componentType) {
        synchronized (componentStores) {
            HashMap<UUID, ? extends Component> store = componentStores.get(componentType);

            if (store == null) {
                return new HashSet<>();
            }

            return store.keySet();
        }
    }

    public <T extends Component> T addComponent(UUID entity, T component) {
        if (frozen) {
            return null;
        }

        synchronized (componentStores) {
            @SuppressWarnings("unchecked")
            HashMap<UUID, T> store = (HashMap<UUID, T>) componentStores.get(component.getClass());

            if (store == null) {
                store = new HashMap<>();
                componentStores.put(component.getClass(), store);
            }

            return store.put(entity, component);
        }
    }

    public UUID createEntity() {
        if (frozen) {
            return null;
        }

        final UUID uuid = UUID.randomUUID();
        allEntities.add(uuid);

        return uuid;
    }

    public UUID createEntity(String name) {
        UUID uuid = createEntity();

        if (uuid != null) {
            entityHumanReadableNames.put(uuid, name);
        }
        return uuid;
    }

    public void setEntityName(UUID entity, String name) {
        entityHumanReadableNames.put(entity, name);
    }

    public String nameFor(UUID entity) {
        return entityHumanReadableNames.get(entity);
    }

    /**
     * Remove entity and its components from manager.
     * 
     * @param entity
     */
    public void removeEntity(UUID entity) {
        if (frozen) {
            return;
        }

        synchronized (componentStores) {

            for (HashMap<UUID, ? extends Component> componentStore : componentStores.values()) {
                componentStore.remove(entity);
            }
            allEntities.remove(entity);
            entityHumanReadableNames.remove(entity);
        }
    }

    /** Removes all entities. */
    public void clearEntities() {
        if (frozen) {
            return;
        }

        synchronized (componentStores) {
            componentStores.clear();
            allEntities.clear();
            entityHumanReadableNames.clear();
        }
    }

    public void freeze() {
        frozen = true;
    }

    public void unFreeze() {
        frozen = false;
    }

    public boolean addSystem(EntitySystem entitySystem) {
        return entitySystems.add(entitySystem);
    }

    public boolean removeSystem(EntitySystem entitySystem) {
        return entitySystems.remove(entitySystem);
    }

    /** Removes all systems. */
    public void clearSystems() {
        entitySystems.clear();
    }

    /** Removes all entities and systems */
    public void clear() {
        clearEntities();
        clearSystems();
    }

    /**
     * To make use MASON's {@link Schedule} the update needs to be called from
     * the entities, which are stepped from the scheduler.
     * 
     * @param entity
     */
    // TODO parallelization in subclass with Futures
    void updateEntity(Entity entity) {
        for (EntitySystem entitySystem : entitySystems.getOrder()) {
            entitySystem.update(entity);
        }
    }
}
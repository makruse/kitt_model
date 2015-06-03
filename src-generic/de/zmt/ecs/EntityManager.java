package de.zmt.ecs;

import java.io.Serializable;
import java.util.*;

/**
 * Modified in java to use Generics: instead of having a "ComponentType" enum,
 * we use the class type of each subclass instead. This is safer.
 * <p>
 * Extended EntityManager that manages systems as well to allow
 * {@link SteppableEntity} update themselves when stepped by MASON's Schedule.
 * 
 * @author adam
 * @author cmeyer
 * 
 * @see <a
 *      href=http://entity-systems.wikidot.com/rdbms-with-code-in-systems>Entity
 *      Systems Wiki: Standard Design</a>
 */

public class EntityManager implements Serializable {
    private static final int SYSTEMS_INITIAL_CAPACITY = 10;

    private static final long serialVersionUID = 1L;

    private boolean frozen;
    private final List<UUID> allEntities;
    private final HashMap<UUID, String> entityHumanReadableNames;

    private final HashMap<Class<? extends Component>, HashMap<UUID, ? extends Component>> componentStores;

    // TODO parallelization
    private final Queue<EntitySystem> entitySystems = new PriorityQueue<>(
	    SYSTEMS_INITIAL_CAPACITY, new SystemsComparator());

    public EntityManager() {
	frozen = false;
	allEntities = new LinkedList<UUID>();
	entityHumanReadableNames = new HashMap<UUID, String>();
	componentStores = new HashMap<>();
    }

    public <T extends Component> T getComponent(UUID entity,
	    Class<T> componentType) {
	synchronized (componentStores) {
	    HashMap<UUID, ? extends Component> store = componentStores
		    .get(componentType);

	    if (store == null)
		throw new IllegalArgumentException(
			"GET FAIL: there are no entities with a Component of class: "
				+ componentType);

	    @SuppressWarnings("unchecked")
	    T result = (T) store.get(entity);

	    if (result == null) {
		/*
		 * DEFAULT: normal debug info:
		 */
		// throw new IllegalArgumentException("GET FAIL: " + entity
		// + "(name:" + nameFor(entity) + ")"
		// + " does not possess Component of class\n   missing: "
		// + componentType);
		/*
		 * OPTIONAL: more detailed debug info:
		 */
		StringBuffer sb = new StringBuffer();
		for (UUID e : store.keySet()) {
		    sb.append("\nUUID: " + e + " === " + store.get(e));
		}

		throw new IllegalArgumentException("GET FAIL: " + entity
			+ "(name:" + nameFor(entity) + ")"
			+ " does not possess Component of class\n   missing: "
			+ componentType
			+ "TOTAL STORE FOR THIS COMPONENT CLASS : "
			+ sb.toString());

	    }

	    return result;
	}
    }

    public void removeComponent(UUID entity, Component component) {
	synchronized (componentStores) {
	    HashMap<UUID, ? extends Component> store = componentStores
		    .get(component.getClass());

	    if (store == null)
		throw new IllegalArgumentException(
			"REMOVE FAIL: there are no entities with a Component of class: "
				+ component.getClass());

	    @SuppressWarnings("unchecked")
	    Component result = store.remove(entity);
	    if (result == null)
		throw new IllegalArgumentException("REMOVE FAIL: " + entity
			+ "(name:" + nameFor(entity) + ")"
			+ " does not possess Component of class\n   missing: "
			+ component.getClass());
	}
    }

    public boolean hasComponent(UUID entity, Class<?> componentType) {
	synchronized (componentStores) {
	    HashMap<UUID, ? extends Component> store = componentStores
		    .get(componentType);

	    if (store == null)
		return false;
	    else
		return store.containsKey(entity);
	}
    }

    /**
     * WARNING: low performance implementation!
     * 
     * @param entity
     * @return
     */
    public Collection<Component> getAllComponentsOnEntity(UUID entity) {
	synchronized (componentStores) {
	    LinkedList<Component> components = new LinkedList<>();

	    for (HashMap<UUID, ? extends Component> store : componentStores
		    .values()) {
		if (store == null)
		    continue;

		Component componentFromThisEntity = store.get(entity);

		if (componentFromThisEntity != null)
		    components.addLast(componentFromThisEntity);
	    }

	    return components;
	}
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> Collection<T> getAllComponentsOfType(
	    Class<T> componentType) {
	synchronized (componentStores) {
	    HashMap<UUID, ? extends Component> store = componentStores
		    .get(componentType);

	    if (store == null)
		return new LinkedList<T>();

	    return (Collection<T>) store.values();
	}
    }

    public <T extends Component> Set<UUID> getAllEntitiesPossessingComponent(
	    Class<T> componentType) {
	synchronized (componentStores) {
	    HashMap<UUID, ? extends Component> store = componentStores
		    .get(componentType);

	    if (store == null)
		return new HashSet<UUID>();

	    return store.keySet();
	}
    }

    public <T extends Component> void addComponent(UUID entity, T component) {
	if (frozen)
	    return;

	synchronized (componentStores) {
	    @SuppressWarnings("unchecked")
	    HashMap<UUID, T> store = (HashMap<UUID, T>) componentStores
		    .get(component.getClass());

	    if (store == null) {
		store = new HashMap<UUID, T>();
		componentStores.put(component.getClass(), store);
	    }

	    store.put(entity, component);
	}
    }

    public UUID createEntity() {
	if (frozen)
	    return null;

	final UUID uuid = UUID.randomUUID();
	allEntities.add(uuid);

	return uuid;
    }

    public UUID createEntity(String name) {
	if (frozen)
	    return null;

	final UUID uuid = UUID.randomUUID();
	allEntities.add(uuid);
	entityHumanReadableNames.put(uuid, name);

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
	if (frozen)
	    return;

	synchronized (componentStores) {

	    for (HashMap<UUID, ? extends Component> componentStore : componentStores
		    .values()) {
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

    public void addSystems(Collection<? extends EntitySystem> entitySystems) {
	this.entitySystems.addAll(entitySystems);
    }

    public boolean removeSystem(EntitySystem entitySystem) {
	return entitySystems.remove(entitySystem);
    }

    /** Removes all systems. */
    public void clearSystems() {
	entitySystems.clear();
    }

    public void updateEntity(Entity entity) {
	for (EntitySystem entitySystem : entitySystems) {
	    entitySystem.update(entity);
	}
    }

    /** Removes all entities and systems */
    public void clear() {
	clearEntities();
	clearSystems();
    }

    /**
     * Compare two systems by their ordering to place the system with the lower
     * ordering on top of queue.
     * 
     * @author cmeyer
     * 
     */
    private static class SystemsComparator implements Comparator<EntitySystem> {

	@Override
	public int compare(EntitySystem o1, EntitySystem o2) {
	    return Integer.compare(o1.getOrdering(), o2.getOrdering());
	}

    }
}
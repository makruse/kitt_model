package de.zmt.ecs;

import java.util.*;

import de.zmt.ecs.graph.*;

/**
 * Generates systems execution order with a dependency graph. A system is
 * updated by the {@link EntityManager} after all instances of its dependencies.
 * 
 * @author mey
 *
 */
class EntitySystems {
    private final List<EntitySystem> order;
    private final DependencyGraph<Class<? extends EntitySystem>> graph;
    /**
     * Map pointing from system class to instances of this class registered.
     */
    private final Map<Class<? extends EntitySystem>, Set<EntitySystem>> systems = new HashMap<>();
    private boolean dirty = false;

    public EntitySystems() {
	order = new ArrayList<>();
	graph = new DependencyGraph<>(new NodeValueListener<Class<? extends EntitySystem>>() {

	    @Override
	    public void evaluate(Class<? extends EntitySystem> nodeElement) {
		order.addAll(systems.get(nodeElement));
	    }
	});
    }

    /**
     * Add a system.
     * 
     * @param system
     * @return {@code false} if the system has been added before
     */
    public boolean add(EntitySystem system) {
	dirty = true;
	Class<? extends EntitySystem> type = system.getClass();
	Set<EntitySystem> systemsOfType = systems.get(type);

	// first system of this type: create new set and add to graph
	if (systemsOfType == null) {
	    systemsOfType = new HashSet<>();
	    systems.put(type, systemsOfType);
	    graph.add(type, system.getDependencies());
	}

	return systemsOfType.add(system);
    }

    public boolean remove(EntitySystem system) {
	dirty = true;
	Class<? extends EntitySystem> type = system.getClass();
	Set<EntitySystem> systemsOfType = systems.get(type);

	if (systemsOfType != null && systemsOfType.remove(system)) {
	    // only remove from graph if this was the only instance
	    if (systemsOfType.isEmpty()) {
		systems.remove(type);
		graph.remove(type);
	    }
	    return true;
	}
	return false;
    }

    public void clear() {
	dirty = true;
	graph.clear();
    }

    public List<EntitySystem> getOrder() {
	if (dirty) {
	    order.clear();
	    graph.resolve();
	}

	return Collections.unmodifiableList(order);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[dirty=" + dirty + ", order=" + order + "]";
    }

}
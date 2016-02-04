package de.zmt.ecs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import sim.display.GUIState;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.CombinedInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Steppable and Stoppable interfaces are implemented so that the entity can be
 * integrated into MASON's schedule and update itself by calling
 * {@link EntityManager#updateEntity(Entity)}.
 * 
 * @author adam
 * @author mey
 */
public class Entity implements Steppable, Stoppable, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_INTERNAL_NAME = "unnamed entity";

    /**
     * Initialized to null, signifying
     * "invalid; NOT registered in any EntityManager yet"
     */
    protected UUID entity = null;

    protected final EntityManager parentEntityManager;

    private final Collection<Stoppable> stoppables = new ArrayList<>(1);

    /**
     * Creates an empty component for given {@link EntityManager}.
     * 
     * @param manager
     */
    public Entity(EntityManager manager) {
	this(manager, Collections.<Component> emptyList());
    }

    /**
     * This is the main constructor for Entities - usually, you'll know which
     * Components you want them to have
     * 
     * NB: this is a NON-lazy way of instantiating Entities - in low-mem
     * situations, you may want to use an alternative constructor that accepts
     * the CLASS of each Component, rather than the OBJECT, and which only
     * instantiates / allocates the memory for the data of each component when
     * that component is (eventually) initialized.
     * 
     * @param manager
     *            the {@link EntityManager} for the entity that is created
     * @param internalName
     *            the internal name that will be attached to this entity, and
     *            reported in debugging info
     * @param components
     */
    public Entity(EntityManager manager, String internalName, Collection<Component> components) {
	this(manager, manager.createEntity(internalName), components);
    }

    /**
     * This is the main constructor for Entities - usually, you'll know which
     * Components you want them to have
     * 
     * NB: this is a NON-lazy way of instantiating Entities - in low-mem
     * situations, you may want to use an alternative constructor that accepts
     * the CLASS of each Component, rather than the OBJECT, and which only
     * instantiates / allocates the memory for the data of each component when
     * that component is (eventually) initialized.
     * 
     * @param manager
     *            the {@link EntityManager} for the entity that is created
     * @param components
     */
    public Entity(EntityManager manager, Collection<Component> components) {
	this(manager, DEFAULT_INTERNAL_NAME, components);
    }

    /**
     * This should NEVER be called by external classes - it's used by the static
     * method loadFromEntityManager
     * 
     * @param manager
     * @param entity
     *            from uuid
     */
    protected Entity(EntityManager manager, UUID entity) {
	this(manager, entity, Collections.<Component> emptyList());
    }

    protected Entity(EntityManager manager, UUID entity, Collection<Component> components) {
	parentEntityManager = manager;

	this.entity = entity;

	for (Component c : components) {
	    this.add(c);
	}
    }

    public static Entity loadFromEntityManager(EntityManager manager, UUID e) {
	Entity metaEntity = new Entity(manager, e);

	return metaEntity;
    }

    /**
     * CONVENIENCE METHOD: delegates to the source {@link EntityManager} to do
     * the add
     * 
     * @param c
     *            {@link Component} to add to this entity (only added within the
     *            {@link EntityManager}, does NOT modify "this" object!)
     */
    public void add(Component c) {
	parentEntityManager.addComponent(entity, c);
    }

    /**
     * CONVENIENCE METHOD: delegates to the source {@link EntityManager} to do
     * the get
     * 
     * @param <T>
     *            {@link Component} fetched from the {@link EntityManager}
     * @param type
     *            Class object representing the particular {@link Component} you
     *            need
     * @return component of class {@code type}
     */
    public <T extends Component> T get(Class<T> type) {
	return parentEntityManager.getComponent(entity, type);
    }

    /**
     * 
     * @param types
     * @return Collection of entity's component matching given {@code types}
     */
    public Collection<Component> get(Collection<Class<? extends Component>> types) {
	List<Component> components = new ArrayList<>(types.size());
	for (Class<? extends Component> type : types) {
	    components.add(get(type));
	}
	return components;
    }

    /**
     * This implementation has a low-performance simple check.
     * 
     * @param type
     *            Class object representing the particular {@link Component} you
     *            want to check the existence of
     * @return true if this entity has a component of given {@code type}
     */
    public boolean has(Class<? extends Component> type) {
	return parentEntityManager.hasComponent(entity, type);
    }

    /**
     * This implementation has a low-performance simple check.
     * 
     * @param types
     *            Class objects representing the particular {@link Component}s
     *            you want to check the existence of
     * @return true if this entity has components of all given {@code types}
     */
    public boolean has(Collection<Class<? extends Component>> types) {
	for (Class<? extends Component> type : types) {
	    if (!parentEntityManager.hasComponent(entity, type)) {
		return false;
	    }
	}
	return true;
    }

    /**
     * CONVENIENCE METHOD: delegates to the source {@link EntityManager} to do
     * the get.
     * 
     * @return collection of all components from this entity
     */
    public Collection<Component> getAll() {
	return parentEntityManager.getAllComponentsOnEntity(entity);
    }

    /**
     * WARNING: low-performance implementation! This fetches the components
     * using getAll(), and then deletes them one by one!
     * 
     */
    public void removeAll() {
	for (Component c : getAll()) {
	    remove(c);
	}
    }

    /**
     * CONVENIENCE METHOD: delegates to the source {@link EntityManager} to do
     * the remove
     * 
     * @param <T>
     *            {@link Component} fetched from the {@link EntityManager}
     * @param c
     *            The *actual* {@link Component} you want to remove from this
     *            entity
     */
    public <T extends Component> void remove(Component c) {
	parentEntityManager.removeComponent(entity, c);
    }

    /**
     * Add a stoppable to be called when the entity stops.
     * 
     * @param stoppable
     */
    public void addStoppable(Stoppable stoppable) {
	stoppables.add(stoppable);
    }

    /**
     * Returns a collection of components that should appear in the entity's
     * inspector. Default behavior is to return all added components.
     * 
     * @return the components that should appear when inspected
     */
    protected Collection<? extends Component> getComponentsToInspect() {
	return getAll();
    }

    @Override
    public final void step(SimState state) {
	parentEntityManager.updateEntity(this);
    }

    /** Stops all stoppables and removes the entity from its manager. */
    @Override
    public void stop() {
	for (Stoppable stoppable : stoppables) {
	    stoppable.stop();
	}
	parentEntityManager.removeEntity(entity);
    }

    /** Returns a {@link CombinedInspector} displaying added components. */
    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Collection<? extends Component> componentsToInspect = getComponentsToInspect();
	CombinedInspector inspector = new CombinedInspector();

	for (Component component : componentsToInspect) {
	    inspector.add(Inspector.getInspector(component, state, component.getClass().getSimpleName()));
	}

	return inspector;
    }

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (Component c : getAll()) {
	    if (sb.length() > 0) {
		sb.append(", ");
	    }
	    sb.append(c.getClass().getSimpleName());
	}
	return getClass().getSimpleName() + "[" + entity + ":" + parentEntityManager.nameFor(entity) + "]("
		+ sb.toString() + ")";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((entity == null) ? 0 : entity.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	Entity other = (Entity) obj;
	if (entity == null) {
	    if (other.entity != null) {
		return false;
	    }
	} else if (!entity.equals(other.entity)) {
	    return false;
	}
	return true;
    }

}
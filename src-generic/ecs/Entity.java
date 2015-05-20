package ecs;

import java.util.*;

import sim.engine.*;

/**
 * The MetaEntity is a smarter, more powerful way of creating and handling
 * Entities in an Entity System.
 * <p>
 * 
 * It takes a lot of the boilerplate code for creating, editing, and deleting
 * entities, and packages it up in a neat OOP class that's easy to use, and easy
 * to pass around from method to method, system to system.
 * <p>
 * 
 * NB: it is LESS EFFICIENT than manually handling your Entities - it adds the
 * overhead of:
 * <ol>
 * <li>A Java Object instance per entity
 * <li>A reference to the EntityManager where the Entity lives (if you only have
 * a single EntityManager in your app, then this is just wasting memory)
 * <li>A (possibly null) internal "name" that is easier for humans to read than
 * the UUID when debugging
 * </ol>
 * 
 * <h2>Usage suggestions</h2> To avoid performance degradation, it's expected
 * that you'll only use MetaEntity objects sparingly, and temporarily.
 * <p>
 * 
 * When you have a system that isn't performance limited, you might write it to
 * use MetaEntity objects, to increase readability.
 * <p>
 * 
 * If you have performance problems, you can re-write critical sections directly
 * using Entity's, removing the overhead of this class.
 * <p>
 * 
 * Steppable and Stoppable interfaces are implemented so that the entity can be
 * integrated into MASON's schedule and update itself by calling
 * {@link EntityManager#updateEntity(MetaEntity)}.
 * 
 * @author adam
 * @author cmeyer
 */
public class Entity implements Steppable, Stoppable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_INTERNAL_NAME = "unnamed entity";

    /**
     * Initialized to null, signifying
     * "invalid; NOT registered in any EntityManager yet"
     */
    protected UUID entity = null;

    protected final EntityManager parentEntityManager;

    private Stoppable stoppable;

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
    public Entity(EntityManager manager, String internalName,
	    Collection<Component> components) {
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
     */
    protected Entity(EntityManager manager, UUID entity) {
	this(manager, entity, Collections.<Component> emptyList());
    }

    protected Entity(EntityManager manager, UUID entity,
	    Collection<Component> components) {
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
    public Collection<Component> get(
	    Collection<Class<? extends Component>> types) {
	List<Component> components = new ArrayList<>(types.size());
	for (Class<? extends Component> type : types) {
	    components.add(get(type));
	}
	return components;
    }

    /**
     * Returns a valid component (not null).
     * 
     * @see #get(Class)
     * @param type
     * @return component of class {@code type}
     * @throw {@link IllegalArgumentException} if entity does not have a
     *        component of given {@code type}
     */
    public <T extends Component> T getValid(Class<T> type) {
	T component = parentEntityManager.getComponent(entity, type);
	if (component == null) {
	    throw new IllegalArgumentException(entity + " does not have "
		    + type.getClass().getSimpleName());
	} else {
	    return component;
	}
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
     * the get
     * 
     * @return
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

    public void setStoppable(Stoppable stoppable) {
	this.stoppable = stoppable;
    }

    /**
     * Creates a new Entity that is exactly the same in terms of Components and
     * Component-values, differing only in that it has a unique Entity-id (and
     * that all its data is private, non-shared, of course!)
     * 
     * @return the new Entity
     * 
     *         /* public Entity duplicate() { return source.duplicate( this ); }
     */

    @Override
    public String toString() {
	StringBuffer sb = new StringBuffer();
	for (Component c : parentEntityManager.getAllComponentsOnEntity(entity)) {
	    if (sb.length() > 0)
		sb.append(", ");
	    sb.append(c.toString());
	}
	return "Entity[" + entity + ":" + parentEntityManager.nameFor(entity)
		+ "](" + sb.toString() + ")";
    }

    @Override
    public final void step(SimState state) {
	parentEntityManager.updateEntity(this);
    }

    /** Stops stoppable and kills the entity. */
    @Override
    public void stop() {
	stoppable.stop();
	parentEntityManager.removeEntity(entity);
    }
}
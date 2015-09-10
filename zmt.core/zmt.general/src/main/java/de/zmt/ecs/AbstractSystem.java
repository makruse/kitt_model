package de.zmt.ecs;

import java.util.Collection;

/**
 * Abstract implementation of a system only updating when required components
 * are present.
 * 
 * @author cmeyer
 * 
 */
public abstract class AbstractSystem implements EntitySystem {
    private EntityManager manager;

    protected EntityManager getManager() {
	return manager;
    }

    @Override
    public void onAdd(EntityManager manager) {
	this.manager = manager;
    }

    @Override
    public void onRemove(EntityManager manager) {
    }

    @Override
    public final void update(Entity entity) {
	/*
	 * FIXME null pointer if systems run in parallel
	 * 
	 * If systemA kills entity, components are removed needed by systemB
	 * that runs in parallel.
	 */
	if (entity.has(getRequiredComponentTypes())) {
	    systemUpdate(entity);
	}
    }

    /**
     * Called only if {@code entity} has the required components for this
     * system.
     * 
     * @see #getRequiredComponentTypes()
     * @param entity
     */
    protected abstract void systemUpdate(Entity entity);

    /**
     * @see #systemUpdate(Entity)
     * @return required components for this system
     */
    protected abstract Collection<Class<? extends Component>> getRequiredComponentTypes();

}

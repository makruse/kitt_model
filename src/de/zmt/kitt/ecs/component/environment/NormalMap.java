package de.zmt.kitt.ecs.component.environment;

import sim.field.grid.ObjectGrid2D;
import ecs.Component;

public class NormalMap implements Component {
    private static final long serialVersionUID = 1L;

    /** Stores normal vectors for habitat boundaries */
    private final ObjectGrid2D normalField;

    public NormalMap(ObjectGrid2D normalGrid) {
	this.normalField = normalGrid;
    }

    /**
     * Field object getter for portrayal in GUI.
     * 
     * @return normal grid
     */
    public ObjectGrid2D getField() {
	return normalField;
    }
}

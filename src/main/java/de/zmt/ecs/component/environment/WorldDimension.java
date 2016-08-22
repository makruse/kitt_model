package de.zmt.ecs.component.environment;

import de.zmt.ecs.Component;

/**
 * Component storing the dimension of available world space for the agents to
 * move in.
 * 
 * @author mey
 *
 */
public class WorldDimension implements Component {
    private static final long serialVersionUID = 1L;

    private final double width;
    private final double height;
    
    public WorldDimension(double width, double height) {
        super();
        this.width = width;
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}

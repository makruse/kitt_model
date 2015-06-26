package de.zmt.kitt.ecs.component.environment;

import sim.util.Double2D;

public interface WorldToMapConverter {
    /**
     * Convert from world to map coordinates (pixel).
     * 
     * @param worldCoordinates
     * @return map coordinates
     */
    Double2D worldToMap(Double2D worldCoordinates);
}
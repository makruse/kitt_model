package de.zmt.ecs.component.environment;

import sim.util.Double2D;

public interface WorldToMapConverter {
    /**
     * Convert from world (continuous) to map coordinates (discrete).
     * 
     * @param worldCoordinates
     * @return map coordinates
     */
    Double2D worldToMap(Double2D worldCoordinates);
}
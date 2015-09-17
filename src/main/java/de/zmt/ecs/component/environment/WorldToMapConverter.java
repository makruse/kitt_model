package de.zmt.ecs.component.environment;

import sim.util.*;

public interface WorldToMapConverter {
    /**
     * Convert from world (continuous) to map coordinates (discrete).
     * 
     * @param worldCoordinates
     * @return map coordinates
     */
    Int2D worldToMap(Double2D worldCoordinates);
}
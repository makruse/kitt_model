package de.zmt.ecs.component.environment;

import sim.util.Double2D;
import sim.util.Int2D;

public interface MapToWorldConverter {

    /**
     * Convert from map (discrete) to world coordinates (continuous).
     * 
     * @param mapCoordinates
     * @return world coordinates
     */
    Double2D mapToWorld(Int2D mapCoordinates);

}
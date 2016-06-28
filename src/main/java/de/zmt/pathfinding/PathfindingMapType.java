package de.zmt.pathfinding;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Types of pathfinding maps used in kitt.
 * 
 * @author mey
 *
 */
@XStreamAlias("PathfindingMapType")
public enum PathfindingMapType {
    FOOD, RISK, BOUNDARY, TO_FORAGE, TO_REST;

    private static final double DEFAULT_WEIGHT_RISK = 2;

    /**
     * Returns the default weight for this type.
     * 
     * @return the default weight for this type
     */
    public double getDefaultWeight() {
        switch (this) {
        case RISK:
            return DEFAULT_WEIGHT_RISK;
        default:
            return 1;
        }
    }

    /**
     * Returns the potential map name for this type.
     * 
     * @return the potential map name for this type
     */
    public String getPotentialMapName() {
        return toString() + " Potential Map";
    }

    /**
     * Returns the flow map name for this type.
     * 
     * @return the flow map name for this type
     */
    public String getFlowMapName() {
        return toString() + " Flow Map";
    }

    @Override
    public String toString() {
        switch (this) {
        case FOOD:
        case RISK:
        case BOUNDARY:
            String allCaps = super.toString();
            return allCaps.substring(0, 1) + allCaps.substring(1).toLowerCase();
        case TO_FORAGE:
            return "Migration to Forage";
        case TO_REST:
            return "Migration to Rest";
        default:
            return super.toString();
        }
    }
}

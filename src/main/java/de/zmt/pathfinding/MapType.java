package de.zmt.pathfinding;

/**
 * Types of pathfinding maps used in kitt.
 * 
 * @author mey
 *
 */
public enum MapType {
    FOOD, RISK, BOUNDARY, TO_FORAGE, TO_REST;

    /** @return the potential map name for this type */
    public String getPotentialMapName() {
	return toString() + " Potential Map";
    }

    /** @return the flow map name for this type */
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

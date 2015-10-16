package de.zmt.pathfinding;

import sim.util.Double2D;

class ConstantFlowMap extends SimplePathfindingMap implements FlowMap {
    private final Double2D value;

    public ConstantFlowMap(int width, int height, Double2D value) {
	super(width, height);
	this.value = value;
    }

    @Override
    public Double2D obtainDirection(int x, int y) {
	return value;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + "[value=" + value + "]";
    }
}

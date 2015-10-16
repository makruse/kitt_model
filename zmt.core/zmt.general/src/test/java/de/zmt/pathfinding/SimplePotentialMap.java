package de.zmt.pathfinding;

import sim.field.grid.DoubleGrid2D;

class SimplePotentialMap implements PotentialMap {
    private final DoubleGrid2D grid;

    public SimplePotentialMap(double[][] values) {
        grid = new DoubleGrid2D(values);
    }

    @Override
    public int getWidth() {
        return grid.getWidth();
    }

    @Override
    public int getHeight() {
        return grid.getHeight();
    }

    @Override
    public double obtainPotential(int x, int y) {
        return grid.get(x, y);
    }

}
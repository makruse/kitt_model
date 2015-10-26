package de.zmt.pathfinding;

class ConstantPathfindingMap implements PathfindingMap {
    private final int width;
    private final int height;

    public ConstantPathfindingMap(int width, int height) {
	super();
	this.width = width;
	this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

}
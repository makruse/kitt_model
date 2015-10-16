package de.zmt.pathfinding;

class SimplePathfindingMap implements PathfindingMap {
    private final int width;
    private final int height;

    public SimplePathfindingMap(int width, int height) {
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
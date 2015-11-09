package de.zmt.pathfinding;

import java.util.*;

abstract class TestDynamicPathfindingMap<T> extends BasicMapChangeNotifier implements PathfindingMap {
    private static final long serialVersionUID = 1L;

    private final int width;
    private final int height;

    protected final Queue<T> mapIterations;

    @SafeVarargs
    public TestDynamicPathfindingMap(int width, int height, T... iterations) {
	mapIterations = new ArrayDeque<>(Arrays.asList(iterations));
	this.width = width;
	this.height = height;
    }

    /** Switch to next iteration and notify listeners. */
    public void nextIteration() {
	mapIterations.remove();
	for (int x = 0; x < width; x++) {
	    for (int y = 0; y < height; y++) {
		notifyListeners(x, y);
	    }
	}
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

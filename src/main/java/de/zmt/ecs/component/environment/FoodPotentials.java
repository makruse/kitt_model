package de.zmt.ecs.component.environment;

import sim.field.grid.DoubleGrid2D;

class FoodPotentials {
    private static final Kernel BLUR_MATRIX = new Kernel(new double[] { 1, 1, 1 });
    private static final int EDGE_SIZE = BLUR_MATRIX.center;

    final DoubleGrid2D potentialsField;

    public FoodPotentials(DoubleGrid2D foodField) {
	super();
	this.potentialsField = filterBoxBlurFull(foodField);
    }
    
    public double getPotential(int x, int y) {
	return potentialsField.get(x, y);
    }
    
    void computePotential(int x, int y, DoubleGrid2D foodField) {
	potentialsField.set(x, y, filterBoxBlur(x, y, foodField));
    }

    private static DoubleGrid2D filterBoxBlurFull(DoubleGrid2D foodField) {
	int width = foodField.getWidth();
	int height = foodField.getHeight();

	DoubleGrid2D blurredField = new DoubleGrid2D(width, height);
	for (int x = EDGE_SIZE; x < width - EDGE_SIZE; x++) {
	    for (int y = EDGE_SIZE; y < height - EDGE_SIZE; y++) {
		blurredField.set(x, y, filterBoxBlur(x, y, foodField));
	    }
	}

	return blurredField;
    }

    private static double filterBoxBlur(int x, int y, DoubleGrid2D foodField) {
	// TODO make blur for this pixel
	return foodField.get(x, y);
    }

    private static class Kernel {
	private final double[] data;
	private final int center;

	public Kernel(double[] data) {
	    super();
	    this.data = data;
	    center = (data.length - 1) / 2;
	}
    }
}

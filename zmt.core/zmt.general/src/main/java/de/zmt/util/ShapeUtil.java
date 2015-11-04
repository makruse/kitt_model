package de.zmt.util;

import java.awt.geom.Rectangle2D;

import sim.portrayal.DrawInfo2D;

public class ShapeUtil {
    /**
     * Scales a rectangle. Used for drawing operations with {@link DrawInfo2D}.
     * 
     * @param rectangle
     * @param scaleValue
     * @return rectangle scaled by scaleValue
     */
    public static Rectangle2D.Double scaleRectangle(Rectangle2D.Double rectangle, double scaleValue) {
	double w = rectangle.width * scaleValue;
	double h = rectangle.height * scaleValue;
	double x = rectangle.x - w / 2;
	double y = rectangle.y - h / 2;

	return new Rectangle2D.Double(x, y, w, h);
    }
}

package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.awt.geom.*;

import sim.portrayal.*;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Double2D;

public class DirectionPortrayal extends SimplePortrayal2D {
    private static final long serialVersionUID = 1L;
    private static final Paint DIRECTION_PAINT = Color.RED;
    private static final double MINIMUM_DRAW_SIZE = 8;
    private static final double LINE_SCALE = 1 / 2d;
    private static final double DOT_SCALE = 1 / 8d;

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	double w = info.draw.width;
	double h = info.draw.height;

	// check if zoom level is appropriate
	if (w < MINIMUM_DRAW_SIZE || h < MINIMUM_DRAW_SIZE) {
	    return;
	}

	Double2D normal = (Double2D) object;
	double x1 = info.draw.x;
	double y1 = info.draw.y;
	double x2 = x1 + normal.x * w * LINE_SCALE;
	double y2 = y1 + normal.y * h * LINE_SCALE;

	// draw each normal as a line with a dot for direction
	Line2D line = new Line2D.Double(x1, y1, x2, y2);
	Ellipse2D dot = new Ellipse2D.Double();
	dot.setFrameFromCenter(x2, y2, x2 + w * DOT_SCALE, y2 + h * DOT_SCALE);
	graphics.setPaint(DIRECTION_PAINT);
	graphics.draw(line);
	graphics.fill(dot);
    }

    /**
     * Check if grid position for that normal was hit.
     * 
     * @see RectanglePortrayal2D#hitObject(Object, DrawInfo2D)
     */
    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
	final double width = range.draw.width;
	final double height = range.draw.height;
	return (range.clip.intersects(range.draw.x - width / 2, range.draw.y
		- height / 2, width, height));
    }
}

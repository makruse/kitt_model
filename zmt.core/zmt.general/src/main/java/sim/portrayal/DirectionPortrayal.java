package sim.portrayal;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import sim.field.grid.ObjectGrid2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Double2D;

/**
 * Draws directions using lines with dots at their ends. Directions are
 * represented by {@link Double2D} objects within an {@link ObjectGrid2D}.
 * <p>
 * If the scale is too small to draw all directions, every nth direction is
 * skipped, while n decreases when scale increases.
 * 
 * @author mey
 *
 */
public class DirectionPortrayal extends SimplePortrayal2D {
    private static final long serialVersionUID = 1L;

    private static final Paint DEFAULT_DIRECTION_PAINT = Color.RED;
    /**
     * If draw scale is below minimum, fewer directions are drawn to comply with
     * the lack of space.
     */
    public static final double MINIMUM_FULL_DRAW_SCALE = 8;
    /** Scale of a line related to the size of a square. */
    private static final double LINE_SCALE = 1 / 2d;
    /** Scale of a dot related to the size of a square. */
    private static final double DOT_SCALE = 1 / 6d;

    private final Paint directionPaint;

    /** Constructs a new {@link DirectionPortrayal} with default settings. */
    public DirectionPortrayal() {
	this(DEFAULT_DIRECTION_PAINT);
    }

    /**
     * Constructs a new {@link DirectionPortrayal} with the given paint for the
     * directions.
     * 
     * @param directionPaint
     */
    public DirectionPortrayal(Paint directionPaint) {
	super();
	this.directionPaint = directionPaint;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	double x = info.draw.x;
	double y = info.draw.y;
	double w = info.draw.width;
	double h = info.draw.height;

	if (isSkipped(x, w) || isSkipped(y, h)) {
	    return;
	}

	Double2D direction = (Double2D) object;
	graphics.setPaint(directionPaint);
	drawDirection(graphics, x, y, w, h, direction);
    }

    /**
     * Checks if a direction should be skipped. The more draw scale is below
     * {@value #MINIMUM_FULL_DRAW_SCALE}, the more directions are skipped.
     * 
     * @param position
     *            the position of the direction to be drawn
     * @param scale
     *            the draw scale
     * @return <code>true</code> if drawing of this direction is skipped
     */
    private static boolean isSkipped(double position, double scale) {
	if (scale < MINIMUM_FULL_DRAW_SCALE) {
	    double gap = MINIMUM_FULL_DRAW_SCALE / scale;

	    // positions are centered, need to subtract 0.5
	    if ((position / scale - 0.5) % gap != 0) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Draws direction at given coordinates. Directions are represented as a dot
     * in the center with a line pointing into the direction.
     * 
     * @param graphics
     * @param x
     * @param y
     * @param w
     * @param h
     * @param direction
     */
    private static void drawDirection(Graphics2D graphics, double x, double y, double w, double h, Double2D direction) {
	// do not let the directions be painted too small
	double drawWidth = Math.max(MINIMUM_FULL_DRAW_SCALE, w);
	double drawHeight = Math.max(MINIMUM_FULL_DRAW_SCALE, h);
	double x2 = x + direction.x * drawWidth * LINE_SCALE;
	double y2 = y + direction.y * drawHeight * LINE_SCALE;

	// draw each direction as a line with a dot for direction
	Line2D line = new Line2D.Double(x, y, x2, y2);
	graphics.draw(line);

	Ellipse2D dot = new Ellipse2D.Double();
	dot.setFrameFromCenter(x, y, x + drawWidth * DOT_SCALE, y + drawHeight * DOT_SCALE);
	graphics.fill(dot);
    }

    /**
     * Just check the whole square for hit detection.
     * 
     * @see RectanglePortrayal2D#hitObject(Object, DrawInfo2D)
     */
    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
	final double width = range.draw.width;
	final double height = range.draw.height;
	return (range.clip.intersects(range.draw.x - width / 2, range.draw.y - height / 2, width, height));
    }
}

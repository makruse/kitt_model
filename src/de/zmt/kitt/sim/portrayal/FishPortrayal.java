package de.zmt.kitt.sim.portrayal;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.portrayal.MemoryPortrayal.MemoryPortrayable;
import de.zmt.sim.portrayal.portrayable.*;
import ec.util.MersenneTwisterFast;

/**
 * Portrays fish as a filled oval. When selected, foraging and resting
 * attraction centers are drawn, as well as the fish's position history.
 * 
 * @author cmeyer
 * 
 */
public class FishPortrayal extends CircledPortrayal2D {
    private static final long serialVersionUID = 1L;

    /** Minimum value in random color generation of a component. */
    private static final int FISH_COLOR_MINIMUM = 64;
    /** Range in random color generation of a component. */
    private static final int FISH_COLOR_RANGE = 128;

    private static final Color CIRCLE_COLOR = Color.BLACK;
    private static final Color FISH_COLOR_TRAIL = Color.GRAY;
    private static final double FISH_SCALE = 6;

    private static final double ATTR_RECT_SIZE = 40;
    private static final double ATTR_RECT_ARC_SIZE = 9;

    private final MemoryPortrayal memoryPortrayal;
    private final OvalPortrayal2D oval = new OvalPortrayal2D(FISH_SCALE);
    private final Map<Integer, Color> drawColors = new HashMap<Integer, Color>();

    public FishPortrayal(MemoryPortrayal memoryPortrayal) {
	super(null);
	super.child = oval;
	scale = FISH_SCALE;
	paint = CIRCLE_COLOR;
	this.memoryPortrayal = memoryPortrayal;
    }

    @Override
    public final void draw(Object object, final Graphics2D graphics,
	    final DrawInfo2D info) {
	FishPortrayable portrayable = (FishPortrayable) ((ProvidesPortrayable<?>) object)
		.providePortrayable();

	// get color from map
	int speciesHash = portrayable.getSpeciesHash();
	Color drawColor = drawColors.get(speciesHash);
	// otherwise create a random one and store it in the map
	if (drawColor == null) {
	    MersenneTwisterFast guirandom = info.gui.guirandom;
	    int r = randomColorComponent(guirandom);
	    int g = randomColorComponent(guirandom);
	    int b = randomColorComponent(guirandom);
	    drawColor = new Color(r, g, b);
	    drawColors.put(speciesHash, drawColor);
	}

	// if selected, draw in brighter color
	if (info.selected) {
	    oval.paint = drawColor.brighter();
	    graphics.setPaint(paint);

	    drawAttractionRect(graphics, info,
		    portrayable.getAttrCenterForaging(), "foraging");
	    drawAttractionRect(graphics, info,
		    portrayable.getAttrCenterResting(), "resting");
	    drawPositionHistory(graphics, info, portrayable.getPosHistory());
	} else {
	    oval.paint = drawColor;
	}

	super.draw(object, graphics, info);
    }

    private int randomColorComponent(MersenneTwisterFast guirandom) {
	return FISH_COLOR_MINIMUM + guirandom.nextInt(FISH_COLOR_RANGE);
    }

    /**
     * Draws rounded rectangle for an attraction center.
     * 
     * @param graphics
     * @param attractionCenter
     * @param description
     */
    private void drawAttractionRect(final Graphics2D graphics, DrawInfo2D info,
	    Double2D attractionCenter, String description) {
	// fish did not set given attraction center, draw nothing here
	if (attractionCenter == null) {
	    return;
	}

	double scaleX = info.draw.width;
	double scaleY = info.draw.height;

	double x = (attractionCenter.x - ATTR_RECT_SIZE / 2) * scaleX;
	double y = (attractionCenter.y - ATTR_RECT_SIZE / 2) * scaleY;
	double width = ATTR_RECT_SIZE * scaleX;
	double height = ATTR_RECT_SIZE * scaleX;
	double arcWidth = ATTR_RECT_ARC_SIZE * scaleX;
	double arcHeight = ATTR_RECT_ARC_SIZE * scaleY;
	if (info.precise) {
	    RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width,
		    height, arcWidth, arcHeight);
	    graphics.draw(rect);
	} else {
	    graphics.drawRoundRect((int) x, (int) y, (int) width, (int) height,
		    (int) arcWidth, (int) arcHeight);
	}
	graphics.drawString(description, (int) x, (int) y);
    }

    /**
     * Draw lines connecting previous locations visited.
     * 
     * @param graphics
     * @param posHistory
     */
    private void drawPositionHistory(final Graphics2D graphics,
	    DrawInfo2D info, Collection<Double2D> posHistory) {
	double scaleX = info.draw.width;
	double scaleY = info.draw.height;

	Double2D previous = null;
	graphics.setColor(FISH_COLOR_TRAIL);

	for (Double2D current : posHistory) {
	    if (previous != null) {

		graphics.drawLine((int) (previous.x * scaleX),
			(int) (previous.y * scaleY),
			(int) (current.x * scaleX), (int) (current.y * scaleY));
	    }
	    previous = current;
	}
    }

    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
	FishPortrayable portrayable = (FishPortrayable) ((ProvidesPortrayable<?>) wrapper
		.getObject()).providePortrayable();

	if (selected) {
	    memoryPortrayal.setPortrayable(portrayable
		    .provideMemoryPortrayable());
	} else {
	    memoryPortrayal.setPortrayable(null);
	}
	return super.setSelected(wrapper, selected);
    }

    public static interface FishPortrayable extends Portrayable {
	int getSpeciesHash();

	Double2D getAttrCenterForaging();

	Double2D getAttrCenterResting();

	Collection<Double2D> getPosHistory();

	MemoryPortrayable provideMemoryPortrayable();
    }
}

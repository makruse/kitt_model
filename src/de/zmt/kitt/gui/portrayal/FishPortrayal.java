package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

import sim.portrayal.*;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;
import sim.util.gui.*;
import de.zmt.kitt.gui.Gui;
import de.zmt.kitt.sim.engine.agent.Fish;

/**
 * Portrays fish as a filled oval. When selected, foraging and resting
 * attraction centers are drawn, as well as the fish's position history.
 * 
 * @author cmeyer
 * 
 */
public class FishPortrayal extends OvalPortrayal2D {
    private static final long serialVersionUID = 1L;

    private static final Color FISH_COLOR_UNSELECTED = Color.GRAY;
    private static final Color FISH_COLOR_SELECTED = new Color(250, 80, 80);
    private static final double FISH_SCALE = 6;

    private static final double ATTR_RECT_SIZE = 40;
    private static final double ATTR_RECT_ARC_SIZE = 9;

    private static ColorMap POS_HISTORY_COLOR_MAP = new SimpleColorMap(1,
	    Fish.POS_HISTORY_MAX_SIZE - 1, FISH_COLOR_UNSELECTED,
	    FISH_COLOR_SELECTED);

    private final Gui gui;

    public FishPortrayal(Gui gui) {
	super(FISH_SCALE);
	this.gui = gui;
    }

    @Override
    public final void draw(Object object, final Graphics2D graphics,
	    final DrawInfo2D info) {
	Fish fish = (Fish) object;

	if (info.selected) {
	    this.paint = FISH_COLOR_SELECTED;
	    graphics.setPaint(paint);

	    drawAttractionRect(graphics, info, fish.getAttrCenterForaging(),
		    "foraging");
	    drawAttractionRect(graphics, info, fish.getAttrCenterResting(),
		    "resting");
	    drawPositionHistory(graphics, info, fish.getPosHistory());
	} else {
	    // just draw the oval with the unselected color
	    this.paint = FISH_COLOR_UNSELECTED;
	}

	super.draw(object, graphics, info);
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

	int index = 0;
	Double2D previous = null;

	for (Double2D current : posHistory) {
	    index++;
	    if (previous != null) {
		graphics.setColor(POS_HISTORY_COLOR_MAP.getColor(index));

		graphics.drawLine((int) (previous.x * scaleX),
			(int) (previous.y * scaleY),
			(int) (current.x * scaleX), (int) (current.y * scaleY));
	    }
	    previous = current;
	}
    }

    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
	if (selected) {
	    gui.setSelectedFish((Fish) wrapper.getObject());
	} else {
	    gui.setSelectedFish(null);
	}
	return super.setSelected(wrapper, selected);
    }

}

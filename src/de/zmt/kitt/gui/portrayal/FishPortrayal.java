package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;
import sim.util.gui.*;
import de.zmt.kitt.gui.Gui;
import de.zmt.kitt.sim.engine.agent.Fish;

// TODO rectangles do not scale yet
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

    public FishPortrayal() {
	super(FISH_SCALE);
    }

    @Override
    public final void draw(Object object, final Graphics2D graphics,
	    final DrawInfo2D info) {
	Fish fish = (Fish) object;

	if (info.selected) {
	    // report GUI about selection
	    ((Gui) info.gui).setSelectedFish(fish);

	    this.paint = FISH_COLOR_SELECTED;
	    graphics.setPaint(paint);

	    drawAttractionRect(graphics, fish.getAttrCenterForaging(),
		    "foraging", info.precise);
	    drawAttractionRect(graphics, fish.getAttrCenterResting(),
		    "resting", info.precise);
	    drawPositionHistory(graphics, fish.getPosHistory());
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
    private void drawAttractionRect(final Graphics2D graphics,
	    Double2D attractionCenter, String description, boolean precise) {
	if (precise) {
	    double x = attractionCenter.x - ATTR_RECT_SIZE / 2;
	    double y = attractionCenter.y - ATTR_RECT_SIZE / 2;
	    double size = ATTR_RECT_SIZE;
	    double arcSize = ATTR_RECT_ARC_SIZE;

	    RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, size,
		    size, arcSize, arcSize);

	    graphics.draw(rect);
	} else {
	    int x = (int) (attractionCenter.x - ATTR_RECT_SIZE / 2);
	    int y = (int) (attractionCenter.y - ATTR_RECT_SIZE / 2);
	    int size = (int) ATTR_RECT_SIZE;
	    int arcSize = (int) ATTR_RECT_ARC_SIZE;

	    graphics.drawRoundRect(x, y, size, size, arcSize, arcSize);
	}
	graphics.drawString(description, (int) attractionCenter.x,
		(int) attractionCenter.y);
    }

    /**
     * Draw lines connecting previous locations visited.
     * 
     * @param graphics
     * @param posHistory
     */
    private void drawPositionHistory(final Graphics2D graphics,
	    Collection<Double2D> posHistory) {
	int index = 0;
	Double2D previous = null;

	for (Double2D current : posHistory) {
	    index++;
	    if (previous != null) {
		graphics.setColor(POS_HISTORY_COLOR_MAP.getColor(index));

		graphics.drawLine((int) (previous.x), (int) (previous.y),
			(int) (current.x), (int) (current.y));
	    }
	    previous = current;
	}
    }

}

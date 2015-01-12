package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;
import de.zmt.kitt.gui.Gui;
import de.zmt.kitt.sim.engine.agent.Fish;

// TODO lots of unexplained constants
public class FishPortrayal extends OvalPortrayal2D {
    private static final long serialVersionUID = 1L;

    private final double myScalingX = 1.0;
    private final double myScalingY = 1.0;

    @Override
    public final void draw(Object object, final Graphics2D g,
	    final DrawInfo2D info) {
	Fish fish = (Fish) object;

	if (info.selected) {
	    ((Gui) info.gui).setSelectedFish(fish);

	    this.paint = new Color(250, 80, 80);
	    g.setPaint(new Color(250, 80, 80));

	    Double2D centerOfAttrForaging = fish.getCenterOfAttrForaging();
	    Double2D centerOfAttrResting = fish.getCenterOfAttrResting();
	    g.drawRoundRect((int) centerOfAttrForaging.x - 20,
		    (int) centerOfAttrForaging.y - 20, 40, 40, 9, 9);
	    g.drawString("feeding", (int) centerOfAttrForaging.x,
		    (int) centerOfAttrForaging.y);
	    g.drawRoundRect((int) centerOfAttrResting.x - 20,
		    (int) centerOfAttrResting.y - 20, 40, 40, 9, 9);
	    g.drawString("sleeping", (int) centerOfAttrResting.x,
		    (int) centerOfAttrResting.y);

	    int cnt = 0;
	    Double2D previous = null;
	    for (Double2D vector : fish.getPosHistory()) {
		if (previous != null) {
		    // TODO simplecolormap
		    g.setColor(new Color(250 - (cnt * 17), 90 - (cnt * 6),
			    90 - (cnt * 6)));

		    g.drawLine((int) (previous.x / myScalingX),
			    (int) (previous.y / myScalingY),
			    (int) (vector.x / myScalingX),
			    (int) (vector.y / myScalingY));
		}
		previous = vector;
		cnt++;
	    }
	} else {
	    this.paint = new Color(140, 140, 140);
	}

	info.draw.height = 6;
	info.draw.width = 9;

	super.draw(object, g, info);
    }
}

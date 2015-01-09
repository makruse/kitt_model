package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;
import de.zmt.kitt.sim.engine.agent.Fish;

public class FishPortrayal extends OvalPortrayal2D {
    private static final long serialVersionUID = 1L;

    private final double myScalingX = 1.0;
    private final double myScalingY = 1.0;

    @Override
    public final void draw(Object object, final Graphics2D g,
	    final DrawInfo2D info) {
	Fish fish = (Fish) object;
	// if( fish.speciesDefinition.speciesId==0){
	// g.setPaint( new Color( 90,180,90)); //(int) (3.0 * fish.giveAge()
	// )));
	// }
	// else if( fish.speciesDefinition.speciesId==1){
	// g.setPaint( new Color( 220, 110, 0 ));
	// }

	// int ofsX=(int) info.draw.x;
	// int ofsY=(int) info.draw.y;

	if (info.selected) {
	    this.paint = new Color(250, 80, 80);
	    g.setPaint(new Color(250, 80, 80));
	    g.drawRoundRect((int) fish.centerOfAttrForaging.x - 20,
		    (int) fish.centerOfAttrForaging.y - 20, 40, 40, 9, 9);
	    g.drawString("feeding", (int) fish.centerOfAttrForaging.x,
		    (int) fish.centerOfAttrForaging.y);
	    g.drawRoundRect((int) fish.centerOfAttrResting.x - 20,
		    (int) fish.centerOfAttrResting.y - 20, 40, 40, 9, 9);
	    g.drawString("sleeping", (int) fish.centerOfAttrResting.x,
		    (int) fish.centerOfAttrResting.y);

	    int cnt = 0;
	    Double2D previous = null;
	    for (Double2D vector : fish.history) {
		if (previous != null) {
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
	info.draw.width = 9; // fish.giveSize()*0.5;

	super.draw(object, g, info);
    }
}

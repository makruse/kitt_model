package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.text.DecimalFormat;

import sim.portrayal.*;
import de.zmt.kitt.sim.Sim;

/**
 * Draws elapsed simulation time at bottom of display.
 * 
 * @author oth
 * 
 */
public class TimeView extends FieldPortrayal2D {

    private final Font font = new Font("SansSerif", Font.PLAIN, 16);

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	Sim sim = (Sim) info.gui.state;

	int minsAll = (int) sim.schedule.getSteps()
		* sim.getParams().environmentDefinition
			.getMinutesPerStep();
	int mins = 0, hours = 0, days = 0, months = 0;

	if (minsAll < 60) {
	    mins = minsAll;
	} else if (minsAll < 60 * 24) {
	    hours = minsAll / 60;
	    mins = minsAll - hours * 60;
	} else if (minsAll < 60 * 24 * 30) {
	    days = minsAll / (60 * 24);
	    hours = (minsAll - days * 60 * 24) / 60;
	    mins = minsAll - days * 60 * 24 - hours * 60;
	} else {
	    months = minsAll / (60 * 24 * 30);
	    days = minsAll / (60 * 24);
	    hours = (minsAll - days * 60 * 24) / 60;
	    mins = minsAll - days * 60 * 24 - hours * 60;
	}

	graphics.setColor(Color.white);
	graphics.setFont(font);

	DecimalFormat df = new DecimalFormat();
	df.setMinimumIntegerDigits(2);

	String txt = "";
	if (months > 0)
	    txt = Integer.toString(months) + " m " + days + " days "
		    + df.format(hours) + ":" + df.format(mins) + " ";
	else if (days > 0)
	    txt = Integer.toString(days) + " days " + df.format(hours) + ":"
		    + df.format(mins) + " ";
	else
	    txt = df.format(hours) + ":" + df.format(mins) + " "; // hh:mm

	// TODO lots of weird constants. adjust to stay at lower border when
	// scaled
	final int px = (int) info.draw.width / 2 - 105 + (int) info.draw.x;
	final int py = (int) info.draw.height + (int) info.draw.y;

	graphics.setColor(Color.black);
	graphics.fillRoundRect(px, py - 30, 210, 28, 2, 2);
	graphics.setColor(Color.yellow);
	graphics.drawRoundRect(px, py - 30, 210, 28, 2, 2);
	graphics.drawString(txt, px + 25, py - 8);
    }
}

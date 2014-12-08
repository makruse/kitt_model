package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.text.DecimalFormat;

import sim.portrayal.*;
import de.zmt.kitt.sim.Sim;

public class TimeView extends FieldPortrayal2D {

    double fScale = 1.0;
    Sim sim;
    Font font;

    public TimeView(Sim sim) {

	font = new Font("SansSerif", Font.PLAIN, (int) (16.0 * fScale));
	this.sim = sim;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	int minsAll = ((int) sim.schedule.getSteps() + 1)
		* sim.getParams().environmentDefinition.timeResolutionMinutes;
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

	final int px = (int) info.draw.width / 2 - (int) (105 * fScale)
		+ (int) info.draw.x;
	final int py = (int) info.draw.height + (int) info.draw.y;

	graphics.setColor(Color.black);
	graphics.fillRoundRect(px, py - 30, 210, 28, 2, 2);
	graphics.setColor(Color.yellow);
	graphics.drawRoundRect(px, py - 30, 210, 28, 2, 2);
	graphics.drawString(txt, (int) (px + 25 * fScale),
		(int) (py - 8 * fScale));

	// graphics.drawString( txt, (int)(px+32*fScale), (int)(py-3*fScale));
	/*
	 * if( (hours*60 + mins) > (sim.params.tageszeiten.get(0)*60) &&
	 * (hours*60 + mins) <= (sim.params.tageszeiten.get(1)*60)){
	 * graphics.setColor(Color.yellow); graphics.fillRoundRect( px+240,
	 * (int)(py-15*fScale), (int)(125*fScale), (int)(15*fScale)
	 * ,(int)(4*fScale),(int)(4*fScale)); graphics.setColor(Color.black);
	 * graphics.drawString( "Morgendämmerung", (int)(px+243*fScale),
	 * (int)(py-3*fScale)); } else{ graphics.setColor(Color.gray);
	 * graphics.fillRoundRect( px+240, (int)(py-15*fScale),
	 * (int)(125*fScale), (int)(15*fScale)
	 * ,(int)(4*fScale),(int)(4*fScale)); graphics.setColor(Color.black);
	 * graphics.drawString( "Abenddämmerung", (int)(px+243*fScale),
	 * (int)(py-3*fScale)); }
	 */
    }
}

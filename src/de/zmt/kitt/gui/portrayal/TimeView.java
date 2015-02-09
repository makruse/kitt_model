package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.text.NumberFormat;
import java.util.Locale;

import org.joda.time.Period;

import sim.portrayal.*;
import de.zmt.kitt.sim.KittSim;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.params.def.EnvironmentDefinition;
import de.zmt.kitt.util.TimeUtil;
import de.zmt.kitt.util.gui.DrawUtil;

/**
 * Draws elapsed simulation time at bottom of display.
 * 
 * @author oth
 * 
 */
public class TimeView extends FieldPortrayal2D {
    private static final double RECT_WIDTH = 210;
    private static final double RECT_WIDTH_HALF = RECT_WIDTH / 2;
    private static final double RECT_HEIGHT = 30;
    private static final double RECT_ARC_SIZE = 5;
    private static final double SPACE_BOTTOM = 1;
    private static final Color BACKGROUND_COLOR = Color.BLACK;
    private static final Color FOREGROUND_COLOR = Color.YELLOW;
    private final Font font = new Font("SansSerif", Font.PLAIN, 16);

    private static final NumberFormat TWO_MINIMUM_DIGITS = NumberFormat
	    .getInstance(Locale.US);
    static {
	TWO_MINIMUM_DIGITS.setMinimumIntegerDigits(2);
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	Environment environment = ((KittSim) (info.gui.state)).getEnvironment();

	Period simulatedPeriod = new Period(EnvironmentDefinition.START_INSTANT,
		environment.getTimeInstant());

	graphics.setColor(Color.white);
	graphics.setFont(font);

	RoundRectangle2D bgRect = new RoundRectangle2D.Double(info.clip.x
		+ info.clip.width / 2 - RECT_WIDTH_HALF, info.clip.y
		+ info.clip.height - RECT_HEIGHT - SPACE_BOTTOM, RECT_WIDTH,
		RECT_HEIGHT, RECT_ARC_SIZE, RECT_ARC_SIZE);

	graphics.setColor(BACKGROUND_COLOR);
	graphics.fill(bgRect);
	graphics.setColor(FOREGROUND_COLOR);
	graphics.draw(bgRect);
	DrawUtil.drawCenteredString(TimeUtil.FORMATTER.print(simulatedPeriod),
		(int) bgRect.getX(), (int) bgRect.getY(),
		(int) bgRect.getWidth(), (int) bgRect.getHeight(), graphics);
    }
}

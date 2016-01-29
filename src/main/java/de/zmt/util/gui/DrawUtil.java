package de.zmt.util.gui;

import java.awt.FontMetrics;
import java.awt.Graphics;

public final class DrawUtil {
    private DrawUtil() {

    }

    /**
     * Draws a string centered within given rectangle starting at x, y and
     * spanning over width / height.
     * 
     * @param string
     * @param x
     * @param y
     * @param width
     * @param height
     * @param graphics
     * 
     * @see "http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Centertext.htm"
     */
    public static void drawCenteredString(String string, int x, int y, int width, int height, Graphics graphics) {
	FontMetrics metrics = graphics.getFontMetrics();
	int stringX = x + (width - metrics.stringWidth(string)) / 2;
	int stringY = y + metrics.getAscent() + (height - (metrics.getAscent() + metrics.getDescent())) / 2;
	graphics.drawString(string, stringX, stringY);
    }

    public static void drawCenteredString(String string, double x, double y, double width, double height,
	    Graphics graphics) {
	FontMetrics metrics = graphics.getFontMetrics();
	double stringX = x + (width - metrics.stringWidth(string)) / 2;
	double stringY = y + metrics.getAscent() + (height - (metrics.getAscent() + metrics.getDescent())) / 2;
	graphics.drawString(string, (int) stringX, (int) stringY);
    }

}

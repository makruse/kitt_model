package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.*;
import de.zmt.kitt.gui.Gui;
import de.zmt.kitt.sim.engine.agent.*;

/**
 * Portrays memory for the currently selected fish.
 * 
 * @author cmeyer
 * 
 */
public class MemoryCellsPortrayal extends FieldPortrayal2D {
    private static final Color COLOR_MEM_CELL = Color.WHITE;

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	Fish fish = ((Gui) info.gui).getSelectedFish();

	if (fish == null) {
	    return;
	}

	graphics.setColor(COLOR_MEM_CELL);

	Memory fishMemory = fish.getMemory();
	double scaleX = (info.draw.width * Memory.MEM_CELL_SIZE_INVERSE)
		/ fishMemory.getPreciseWidth();
	int scaledCellSizeX = (int) scaleX * Memory.MEM_CELL_SIZE;
	double scaleY = (info.draw.height * Memory.MEM_CELL_SIZE_INVERSE)
		/ fishMemory.getPreciseHeight();
	int scaledCellSizeY = (int) scaleY * Memory.MEM_CELL_SIZE;

	for (int y = 0; y < fishMemory.getHeight(); y++) {
	    for (int x = 0; x < fishMemory.getWidth(); x++) {
		int drawX = x * scaledCellSizeX;
		int drawY = y * scaledCellSizeY;

		// draw line to right and bottom with one cell length
		graphics.drawLine(drawX, drawY, drawX + scaledCellSizeX, drawY);
		graphics.drawLine(drawX, drawY, drawX, drawY + scaledCellSizeY);

		// draw memory values centered within rectangle
		drawCenteredString(String.valueOf(fishMemory.get(x, y)), drawX,
			drawY, scaledCellSizeX, scaledCellSizeY, graphics);
	    }

	}
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
     * @see http://www.java2s.com/Tutorial/Java/0261__2D-Graphics/Centertext.htm
     */
    private void drawCenteredString(String string, int x, int y, int width,
	    int height, Graphics graphics) {
	FontMetrics metrics = graphics.getFontMetrics();
	int stringX = x + (width - metrics.stringWidth(string)) / 2;
	int stringY = y + metrics.getAscent()
		+ (height - (metrics.getAscent() + metrics.getDescent())) / 2;
	graphics.drawString(string, stringX, stringY);
    }
}
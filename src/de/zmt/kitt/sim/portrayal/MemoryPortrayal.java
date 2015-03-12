package de.zmt.kitt.sim.portrayal;

import java.awt.*;

import sim.portrayal.*;
import de.zmt.kitt.sim.engine.agent.fish.Memory;
import de.zmt.kitt.util.gui.DrawUtil;

/**
 * Portrays memory for the currently selected fish.
 * 
 * @author cmeyer
 * 
 */
// TODO draw only cells within clip
public class MemoryPortrayal extends FieldPortrayal2D {
    private static final Color COLOR_MEM_CELL = Color.WHITE;

    private Memory memory;

    public void setMemory(Memory memory) {
	this.memory = memory;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	// do not draw anything if there is no memory available
	if (memory == null) {
	    return;
	}

	graphics.setColor(COLOR_MEM_CELL);
	double scaleX = (info.draw.width * Memory.MEM_CELL_SIZE_INVERSE)
		/ memory.getPreciseWidth();
	int scaledCellSizeX = (int) scaleX * Memory.MEM_CELL_SIZE;
	double scaleY = (info.draw.height * Memory.MEM_CELL_SIZE_INVERSE)
		/ memory.getPreciseHeight();
	int scaledCellSizeY = (int) scaleY * Memory.MEM_CELL_SIZE;

	for (int y = 0; y < memory.getHeight(); y++) {
	    for (int x = 0; x < memory.getWidth(); x++) {
		int drawX = x * scaledCellSizeX;
		int drawY = y * scaledCellSizeY;

		// draw line to right and bottom with one cell length
		graphics.drawLine(drawX, drawY, drawX + scaledCellSizeX, drawY);
		graphics.drawLine(drawX, drawY, drawX, drawY + scaledCellSizeY);

		// draw memory values centered within rectangle
		DrawUtil.drawCenteredString(String.valueOf(memory.get(x, y)),
			drawX, drawY, scaledCellSizeX, scaledCellSizeY,
			graphics);
	    }

	}
    }
}
package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.*;
import de.zmt.kitt.gui.Gui;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Fish;

public class MemoryCellsPortrayal extends FieldPortrayal2D {
    private static final Color COLOR_MEM_CELL = Color.WHITE;

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	Fish fish = ((Gui) info.gui).getSelectedFish();

	if (fish == null) {
	    return;
	}

	int drawX = (int) info.draw.x;
	int drawY = (int) info.draw.y;
	int width = (int) info.draw.width;
	int height = (int) info.draw.height;

	graphics.setColor(COLOR_MEM_CELL);

	// TODO are mem cells always quadratic?
	int numCells = Environment.MEM_CELLS_X;

	double dsize = height / numCells;

	for (int cell = 0; cell < numCells; cell++) {
	    int c = (int) (dsize * (cell + 1));
	    graphics.drawLine(drawX + c, 0, drawX + c, height);
	    graphics.drawLine(drawX, drawY + c, drawX + width, drawY + c);
	}

	for (int cellX = 0; cellX < numCells; cellX++) {
	    int cx = (int) ((dsize * (cellX + 1)) - dsize / 2.0);
	    for (int cellY = 0; cellY < numCells; cellY++) {

		Integer num = fish.getMemoryFor(cellX, cellY);
		if (num > 0) {
		    int cy = (int) ((dsize * (cellY + 1)) - dsize / 2.0);

		    graphics.drawString(String.valueOf(num), drawX + cx - 5,
			    drawY + cy + 5);
		}
	    }
	}
    }
}
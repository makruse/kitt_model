package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.*;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Fish;

/**
 * A JPanel that draws the distribution of the Coral species for each zone in
 * the field on the screen.
 * */

public class MemoryCellsPortrayal extends FieldPortrayal2D // FieldPortrayal2D
{
    private static final Color COLOR_MEM_CELL = Color.WHITE;

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	int drawX = (int) info.draw.x;
	int drawY = (int) info.draw.y;
	int width = (int) info.draw.width;
	int height = (int) info.draw.height;

	Fish fish = ((Sim) info.gui.state).getFishInFocus();

	if (fish != null) {

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

		    Integer num = fish.memField.get(cellX, cellY);
		    if (num > 0) {
			int cy = (int) ((dsize * (cellY + 1)) - dsize / 2.0);

			graphics.drawString(String.valueOf(num), drawX + cx - 5,
				drawY + cy + 5);
		    }
		}
	    }
	}
    }
}
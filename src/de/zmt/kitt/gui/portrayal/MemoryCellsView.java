package de.zmt.kitt.gui.portrayal;

import java.awt.*;

import sim.portrayal.*;
import de.zmt.kitt.sim.Sim;
import de.zmt.kitt.sim.engine.Environment;
import de.zmt.kitt.sim.engine.agent.Fish;
import de.zmt.kitt.sim.params.ModelParams;

/**
 * A JPanel that draws the distribution of the Coral species for each zone in
 * the field on the screen.
 * */

public class MemoryCellsView extends FieldPortrayal2D // FieldPortrayal2D
{
    private final Sim sim;
    private final ModelParams params;
    private final double diplayWidth;
    private final double displayHeight;

    // static BasicStroke dashed;

    public MemoryCellsView(Sim sim, int width, int height) {
	this.sim = sim;
	this.params = sim.getParams();
	diplayWidth = width - 32;
	displayHeight = height - 32;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	double fWidth = params.environmentDefinition.fieldWidth;
	double fHeight = params.environmentDefinition.fieldHeight;

	double scale = fHeight / displayHeight;

	int ofsX = (int) info.draw.x;
	int ofsY = (int) info.draw.y;

	Fish fish = sim.getFishInFocus();

	if (fish != null) {
	    Color clr = Color.WHITE; // Color.getColor(def.color);

	    graphics.setColor(clr);

	    // TODO are mem cells always quadratic?
	    int numCells = Environment.MEM_CELLS_X;

	    double dsize = fHeight / numCells / scale;

	    for (int cell = 0; cell < numCells; cell++) {
		int c = (int) (dsize * (cell + 1));
		graphics.setColor(clr);
		graphics.drawLine(ofsX + c, 0, ofsX + c, (int) fHeight);
		graphics.setColor(clr);
		graphics.drawLine(ofsX, ofsY + c, ofsX + (int) fWidth, ofsY + c);
	    }

	    for (int cellX = 0; cellX < numCells; cellX++) {
		int cx = (int) ((dsize * (cellX + 1)) - dsize / 2.0);
		for (int cellY = 0; cellY < numCells; cellY++) {

		    Integer num = fish.memField.get(cellX, cellY);
		    if (num > 0) {
			int cy = (int) ((dsize * (cellY + 1)) - dsize / 2.0);

			graphics.setColor(clr);
			graphics.drawString(String.valueOf(num), ofsX + cx - 5,
				ofsY + cy + 5);
		    }
		}
	    }
	}
    }
}
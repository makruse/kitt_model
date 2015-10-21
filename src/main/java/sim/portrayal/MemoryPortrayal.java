package sim.portrayal;

import java.awt.*;

import sim.portrayal.portrayable.Portrayable;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.util.gui.DrawUtil;

/**
 * Portrays memory for the currently selected fish.
 * 
 * @author cmeyer
 * 
 */
// TODO draw only cells within clip
public class MemoryPortrayal extends FieldPortrayal2D {
    private static final Color COLOR_MEM_CELL = Color.WHITE;

    private MemoryPortrayable portrayable;

    public void setPortrayable(MemoryPortrayable portrayable) {
	this.portrayable = portrayable;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
	// do not draw anything if there is no memory available
	if (portrayable == null) {
	    return;
	}

	graphics.setColor(COLOR_MEM_CELL);
	double scaleX = (info.draw.width * Memorizing.MEM_CELL_SIZE_INVERSE)
		/ portrayable.getPreciseWidth();
	int scaledCellSizeX = (int) scaleX * Memorizing.MEM_CELL_SIZE;
	double scaleY = (info.draw.height * Memorizing.MEM_CELL_SIZE_INVERSE)
		/ portrayable.getPreciseHeight();
	int scaledCellSizeY = (int) scaleY * Memorizing.MEM_CELL_SIZE;

	for (int y = 0; y < portrayable.getHeight(); y++) {
	    for (int x = 0; x < portrayable.getWidth(); x++) {
		int drawX = x * scaledCellSizeX;
		int drawY = y * scaledCellSizeY;

		// draw line to right and bottom with one cell length
		graphics.drawLine(drawX, drawY, drawX + scaledCellSizeX, drawY);
		graphics.drawLine(drawX, drawY, drawX, drawY + scaledCellSizeY);

		// draw memory values centered within rectangle
		DrawUtil.drawCenteredString(
			String.valueOf(portrayable.get(x, y)),
			drawX, drawY, scaledCellSizeX, scaledCellSizeY,
			graphics);
	    }

	}
    }

    public static interface MemoryPortrayable extends Portrayable {
	int get(int memX, int memY);

	double getPreciseWidth();

	double getPreciseHeight();

	int getWidth();

	int getHeight();
    }
}
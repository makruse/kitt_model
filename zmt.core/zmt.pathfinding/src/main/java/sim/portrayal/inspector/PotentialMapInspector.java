package sim.portrayal.inspector;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import de.zmt.pathfinding.PotentialMap;
import sim.display.GUIState;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.ColorMapFactory;

/**
 * {@link Inspector} to display a {@link PotentialMap} within the MASON GUI.
 * Attractive potential values are displayed in blue getting brighter towards
 * neutral ones while repulsive values are displayed in red.
 * 
 * @author mey
 *
 */
public class PotentialMapInspector extends Inspector {
    private static final long serialVersionUID = 1L;

    private final PotentialMap map;
    private final FastValueGridPortrayal2D portrayal;
    private final GUIState guiState;

    /**
     * Instantiates a {@link PotentialMapInspector} displaying the given map.
     * 
     * @param state
     * @param map
     */
    public PotentialMapInspector(GUIState state, PotentialMap map) {
        this.map = map;
        portrayal = new FastValueGridPortrayal2D(true);
        portrayal.setField(map.providePortrayable().getField());
	portrayal.setMap(ColorMapFactory.createForPotentials());
        this.guiState = state;
    }

    /**
     * Overrides default {@link ColorMap} with a custom one.
     * 
     * @see ColorMapFactory
     * @param map
     */
    public void setColorMap(ColorMap map) {
	portrayal.setMap(map);
    }

    @Override
    public void updateInspector() {
        portrayal.setDirtyField(true);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(map.getWidth(), map.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Rectangle2D.Double draw = new Rectangle2D.Double(0, 0, map.getWidth(), map.getHeight());
        // draw everything, draw and clip is the same
        portrayal.draw(portrayal.getField(), (Graphics2D) g, new DrawInfo2D(guiState, portrayal, draw, draw));
    }
}
package sim.portrayal.inspector;

import de.zmt.pathfinding.PotentialMap;
import sim.display.GUIState;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Inspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.ValueGridPortrayal2D;
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
public class PotentialMapInspector extends AbstractPathfindingMapInspector<PotentialMap> {
    private static final long serialVersionUID = 1L;

    private final ValueGridPortrayal2D portrayal = new FastValueGridPortrayal2D(true);

    /**
     * Instantiates a {@link PotentialMapInspector} displaying the given map.
     * 
     * @param state
     * @param map
     */
    public PotentialMapInspector(GUIState state, PotentialMap map) {
	super(state, map);
	portrayal.setField(map.providePortrayable().getField());
	portrayal.setMap(ColorMapFactory.createForPotentials());
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
    protected FieldPortrayal2D getPortrayal() {
	return portrayal;
    }
}
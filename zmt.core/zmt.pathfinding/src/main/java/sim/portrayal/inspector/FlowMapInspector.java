package sim.portrayal.inspector;

import de.zmt.pathfinding.FlowMap;
import sim.display.GUIState;
import sim.portrayal.DirectionPortrayal;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Inspector;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.util.Double2D;

/**
 * {@link Inspector} to display a {@link FlowMap} within the MASON GUI. Each
 * direction is portrayed by a line with a dot at its end using
 * {@link DirectionPortrayal}.
 * 
 * @author mey
 *
 */
public class FlowMapInspector extends AbstractPathfindingMapInspector<FlowMap> {
    private static final long serialVersionUID = 1L;

    private final ObjectGridPortrayal2D portrayal = new ObjectGridPortrayal2D();

    /**
     * Instantiates a {@link FlowMapInspector} displaying the given map.
     * 
     * @param state
     * @param map
     */
    public FlowMapInspector(GUIState state, FlowMap map) {
	super(state, map);
	portrayal.setField(map.providePortrayable().getField());
	portrayal.setPortrayalForClass(Double2D.class, new DirectionPortrayal());
    }

    /** Appends notification text if view is compressed due to lack of space. */
    @Override
    protected String generateScaleText(double scaleValue) {
	String scaleText = super.generateScaleText(scaleValue);
	if (scaleValue < DirectionPortrayal.MINIMUM_FULL_DRAW_SCALE) {
	    return scaleText + " (compressed)";
	}
	return scaleText;
    }

    @Override
    protected FieldPortrayal2D getPortrayal() {
	return portrayal;
    }
}

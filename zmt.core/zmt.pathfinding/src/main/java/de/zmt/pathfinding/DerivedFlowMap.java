package de.zmt.pathfinding;

import static de.zmt.util.DirectionUtil.NEUTRAL;

import sim.display.GUIState;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.CombinedInspector;
import sim.portrayal.inspector.FlowMapInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

/**
 * This class provides a skeletal implementation for a flow map that is derived
 * from other underlying pathfinding maps. Changes in an underlying map are
 * propagated automatically if it implements the {@link MapChangeNotifier}
 * interface.
 * <p>
 * Each map is associated with a name and can be accessed via
 * {@link #getUnderlyingMap(String)}. If no name is specified the map's string
 * representation will be used along with its hash code.
 * <p>
 * Implementing classes need to specify abstract
 * {@link #computeDirection(int, int)} which is called when an update is needed.
 * Otherwise cached directions are fetched from a grid. In here the weight
 * factor associated with every underlying map can be used to define its
 * influence on the final result.
 * 
 * @author mey
 *
 * @param <T>
 *            the type of underlying pathfinding maps
 */
abstract class DerivedFlowMap<T extends PathfindingMap> extends AbstractDerivedMap<T>
	implements GridBackedFlowMap, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    /** Grid containing a flow direction for every location. */
    private final ObjectGrid2D flowMapGrid;

    /**
     * Constructs a new {@code DerivedFlowMap} with given dimensions.
     * 
     * @param width
     *            width of map
     * @param height
     *            height of map
     */
    public DerivedFlowMap(int width, int height) {
	super(width, height);
	// no underlying maps yet, initialize all locations to neutral direction
	flowMapGrid = new ObjectGrid2D(width, height, NEUTRAL);
    }

    /**
     * Constructs a new {@link DerivedFlowMap} with initial content from given
     * object.
     * 
     * @param content
     *            the initial content
     */
    public DerivedFlowMap(Changes<T> content) {
	this(content.getWidth(), content.getHeight());
	applyChanges(content);
    }

    /**
     * Called only when the location needs to be updated after locations have
     * been marked dirty. Otherwise direction vectors are fetched from a cache.
     * Implementing classes must specify the result.
     * 
     * @param x
     *            the x-coordinate of location
     * @param y
     *            the y-coordinate of location
     * @return result of direction at given location
     */
    protected abstract Double2D computeDirection(int x, int y);

    /**
     * Gets the grid containing the cached directions as {@link Double2D}
     * objects.
     */
    @Override
    public final ObjectGrid2D getMapGrid() {
	return flowMapGrid;
    }

    @Override
    protected void update(int x, int y) {
	getMapGrid().set(x, y, computeDirection(x, y));
    }

    /**
     * Obtains flow direction for given location after updating updating if
     * needed.
     */
    @Override
    public final Double2D obtainDirection(int x, int y) {
	updateIfDirty(x, y);
	return (Double2D) getMapGrid().get(x, y);
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	FlowMapInspector flowMapInspector = new FlowMapInspector(state, this);
	CombinedInspector combinedInspector = new CombinedInspector(flowMapInspector,
		Inspector.getInspector(getUnderlyingMaps(), state, name));
	combinedInspector.setTitle(toString());
	return combinedInspector;
    }

    /**
     * Returns the field portrayable.<br>
     * <b>NOTE:</b> This displays the field as is, including not-updated dirty
     * locations. To ensure the correct state is drawn, call
     * {@link #updateIfDirtyAll()} before.
     */
    @Override
    public FieldPortrayable<ObjectGrid2D> providePortrayable() {
	return new FieldPortrayable<ObjectGrid2D>() {

	    @Override
	    public ObjectGrid2D getField() {
		return getMapGrid();
	    }
	};
    }
}
package de.zmt.pathfinding;

import sim.display.GUIState;
import sim.field.grid.DoubleGrid2D;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.PotentialMapInspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * A potential map backed by a {@link DoubleGrid2D}.
 * 
 * @author mey
 *
 */
public class SimplePotentialMap extends SimplePathfindingMap<DoubleGrid2D>
        implements GridBackedPotentialMap, EdgeHandledPotentialMap, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private final EdgeHandler edgeHandler;

    /**
     * Constructs a new {@code SimplePotentialMap} backed by given grid.
     * 
     * @param mapGrid
     *            the grid that backs this map
     */
    public SimplePotentialMap(DoubleGrid2D mapGrid) {
        this(mapGrid, EdgeHandler.getDefault());
    }

    /**
     * Constructs a new {@code SimplePotentialMap} backed by a new grid
     * containing given values. To be used in tests.
     * 
     * @param values
     */
    SimplePotentialMap(double[][] values) {
        this(new DoubleGrid2D(values));
    }

    /**
     * Cosntructs a new {@link SimplePotentialMap} with given
     * {@link EdgeHandler}.
     * 
     * @param mapGrid
     *            the grid that backs the map
     * @param edgeHandler
     *            the edge handler
     */
    public SimplePotentialMap(DoubleGrid2D mapGrid, EdgeHandler edgeHandler) {
        super(mapGrid);
        this.edgeHandler = edgeHandler;
    }

    @Override
    public double obtainPotential(int x, int y) {
        return getMapGrid().get(x, y);
    }

    @Override
    public EdgeHandler getEdgeHandler() {
        return edgeHandler;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        return new PotentialMapInspector(state, this);
    }
}
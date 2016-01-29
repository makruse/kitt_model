package de.zmt.ecs.component.environment;

import java.util.*;

import de.zmt.ecs.Component;
import de.zmt.pathfinding.*;
import sim.field.grid.DoubleGrid2D;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Double2D;

public class SpeciesFlowMap extends FlowFromFlowsMap {
    private static final long serialVersionUID = 1L;

    private static final double WEIGHT_RISK = 2;

    private FlowFromPotentialsMap flowFromPotentialsMap;
    /** {@code PotentialMap} for risk. */
    private PotentialMap riskPotentialMap;
    /** Risk flow calculated only from {@link #riskPotentialMap}. */
    private final FlowFromPotentialsMap riskFlowMap;

    public SpeciesFlowMap(GlobalFlowMap globalFlowMap) {
	super(globalFlowMap);
	flowFromPotentialsMap = new FlowFromPotentialsMap(getWidth(), getHeight());
	riskFlowMap = new FlowFromPotentialsMap(getWidth(), getHeight());
	addMap(flowFromPotentialsMap);
    }

    /**
     * Obtains risk-only flow direction vector for given location.
     * 
     * @see #setRiskPotentialMap(PotentialMap)
     * @param x
     * @param y
     * @return risk-only direction vector at given location
     */
    public Double2D obtainRiskDirection(int x, int y) {
	return riskFlowMap.obtainDirection(x, y);
    }

    /**
     * Sets potential map containing predation risk and add it to the combined
     * map. If already set, the old one will be removed from the combined map as
     * well.
     * 
     * @see #obtainRiskDirection(int, int)
     * @param riskPotentialMap
     */
    public void setRiskPotentialMap(PotentialMap riskPotentialMap) {
	if (this.riskPotentialMap != null) {
	    flowFromPotentialsMap.removeMap(riskPotentialMap);
	    riskFlowMap.removeMap(riskPotentialMap);
	}
	this.riskPotentialMap = riskPotentialMap;
	flowFromPotentialsMap.addMap(riskPotentialMap, WEIGHT_RISK);
	riskFlowMap.addMap(riskPotentialMap);
    }

    /**
     * Provides risk potentials portrayable.
     *
     * @return the field portrayable
     */
    public FieldPortrayable<DoubleGrid2D> provideRiskPotentialsPortrayable() {
	return riskPotentialMap.providePortrayable();
    }

    /**
     * Container {@link Map} for storing a {@link SpeciesFlowMap} for every
     * species.
     * 
     * @author mey
     *
     */
    public static class Container extends HashMap<SpeciesDefinition, SpeciesFlowMap> implements Component {
	private static final long serialVersionUID = 1L;
    }
}

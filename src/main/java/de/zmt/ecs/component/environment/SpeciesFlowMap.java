package de.zmt.ecs.component.environment;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.pathfinding.FlowFromFlowsMap;
import de.zmt.pathfinding.FlowFromPotentialsMap;
import de.zmt.pathfinding.FlowMap;
import de.zmt.pathfinding.PotentialMap;
import sim.field.grid.DoubleGrid2D;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.portrayable.FieldPortrayable;

public class SpeciesFlowMap implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final double WEIGHT_RISK = 2;

    /** Flow for feeding, containing risk and food. */
    private final FlowFromFlowsMap feedingFlowMap;
    /** Potentials for risk. */
    private final PotentialMap riskPotentialMap;
    /** Risk flow calculated only from {@link #riskPotentialMap}. */
    private final FlowFromPotentialsMap riskFlowMap;
    /**
     * Flow for {@link BehaviorMode#MIGRATING}, risk and target habitat,
     * separated by target mode.
     */
    private final Map<BehaviorMode, FlowFromPotentialsMap> migratingFlowMaps = new EnumMap<>(BehaviorMode.class);

    public SpeciesFlowMap(GlobalFlowMap globalFlowMap, PotentialMap riskPotentialMap, PotentialMap toForagePotentialMap,
	    PotentialMap toRestPotentialMap) {
	this.feedingFlowMap = new FlowFromFlowsMap(globalFlowMap);
	this.riskPotentialMap = riskPotentialMap;
	this.riskFlowMap = new FlowFromPotentialsMap(riskPotentialMap);

	feedingFlowMap.addMap(riskFlowMap, WEIGHT_RISK);

	this.migratingFlowMaps.put(BehaviorMode.FORAGING, new FlowFromPotentialsMap(toForagePotentialMap));
	this.migratingFlowMaps.put(BehaviorMode.RESTING, new FlowFromPotentialsMap(toRestPotentialMap));

	// add risk influence into migrating maps
	for (FlowFromPotentialsMap migratingFlowMap : migratingFlowMaps.values()) {
	    migratingFlowMap.addMap(riskPotentialMap, WEIGHT_RISK);
	}
    }

    /** @return the flow map used for feeding (risk + food) */
    public FlowFromFlowsMap getFeedingFlowMap() {
	return feedingFlowMap;
    }

    /** @return the risk-only {@link FlowMap} */
    public FlowFromPotentialsMap getRiskFlowMap() {
	return riskFlowMap;
    }

    /**
     * Returns migrating flow map containing target habitat and risk.
     * 
     * @param nextMode
     *            the {@link BehaviorMode} after migration
     * @return the migrating {@link FlowMap} leading to the habitat of nextMode
     */
    public FlowMap getMigratingFlowMap(BehaviorMode nextMode) {
	return migratingFlowMaps.get(nextMode);
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

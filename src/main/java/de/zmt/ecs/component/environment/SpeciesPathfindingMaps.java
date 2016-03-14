package de.zmt.ecs.component.environment;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.pathfinding.DerivedMap.Changes;
import de.zmt.pathfinding.FlowFromPotentialsMap;
import de.zmt.pathfinding.FlowMap;
import de.zmt.pathfinding.PotentialMap;
import sim.field.grid.DoubleGrid2D;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.util.Proxiable;

public class SpeciesPathfindingMaps implements Serializable, Proxiable {
    private static final long serialVersionUID = 1L;

    private static final double WEIGHT_RISK = 2;

    /** Flow for feeding, containing risk and food. */
    private final FlowFromPotentialsMap feedingFlowMap;
    /** Flow evading high risk areas and boundaries. */
    private final FlowFromPotentialsMap riskFlowMap;
    /**
     * Flow for {@link BehaviorMode#MIGRATING}, risk and target habitat,
     * separated by target mode.
     */
    private final Map<BehaviorMode, FlowFromPotentialsMap> migratingFlowMaps = new EnumMap<>(BehaviorMode.class);
    /** Direct reference to risk potential map to provide portrayal. */
    private final PotentialMap riskPotentialMap;

    public SpeciesPathfindingMaps(GlobalPathfindingMaps globalPathfindingMaps, PotentialMap riskPotentialMap,
	    PotentialMap toForagePotentialMap, PotentialMap toRestPotentialMap) {
	this.riskPotentialMap = riskPotentialMap;

	// create changes objects for risk and boundary maps
	Changes<PotentialMap> riskAndBoundaryChanges = Changes.Factory.addMap(riskPotentialMap, WEIGHT_RISK)
		.addMap(globalPathfindingMaps.getBoundaryPotentialMap());

	riskFlowMap = new FlowFromPotentialsMap(riskAndBoundaryChanges);
	feedingFlowMap = new FlowFromPotentialsMap(
		riskAndBoundaryChanges.addMap(globalPathfindingMaps.getFoodPotentialMap()));
	migratingFlowMaps.put(BehaviorMode.FORAGING,
		new FlowFromPotentialsMap(riskAndBoundaryChanges.addMap(toForagePotentialMap)));
	migratingFlowMaps.put(BehaviorMode.RESTING,
		new FlowFromPotentialsMap(riskAndBoundaryChanges.addMap(toRestPotentialMap)));

	riskFlowMap.setName("Risk Flow Map");
	feedingFlowMap.setName("Feeding Flow Map");
	migratingFlowMaps.get(BehaviorMode.FORAGING).setName("Migrate to Foraging Flow Map");
	migratingFlowMaps.get(BehaviorMode.RESTING).setName("Migrate to Resting Flow Map");
    }

    /** @return the flow map used for feeding (risk + food) */
    public FlowMap getFeedingFlowMap() {
	return feedingFlowMap;
    }

    /** @return the risk-only {@link FlowMap} */
    public FlowMap getRiskFlowMap() {
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

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    /**
     * Container {@link Map} for storing a {@link SpeciesPathfindingMaps} for
     * every species.
     * 
     * @author mey
     *
     */
    public static class Container extends HashMap<SpeciesDefinition, SpeciesPathfindingMaps> implements Component {
	private static final long serialVersionUID = 1L;
    }

    public class MyPropertiesProxy {
	public FlowMap getFeedingFlowMap() {
	    return feedingFlowMap;
	}

	public FlowMap getRiskFlowMap() {
	    return riskFlowMap;
	}

	public FlowMap getMigratingToForagingFlowMap() {
	    return migratingFlowMaps.get(BehaviorMode.FORAGING);
	}

	public FlowMap getMigratingToRestingFlowMap() {
	    return migratingFlowMaps.get(BehaviorMode.RESTING);
	}
    }
}

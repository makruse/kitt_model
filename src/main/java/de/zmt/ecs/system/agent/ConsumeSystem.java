package de.zmt.ecs.system.agent;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;

import de.zmt.ecs.component.agent.*;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.EntitySystem;
import de.zmt.ecs.component.agent.Compartments.TransferDigestedResult;
import de.zmt.ecs.component.agent.LifeCycling.CauseOfDeath;
import de.zmt.ecs.system.agent.move.MoveSystem;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import sim.engine.SimState;

/**
 * Compute consumed energy for this update cycle.
 * <p>
 * <img src="doc-files/gen/ConsumeSystem.svg" alt= "ConsumeSystem Activity
 * Diagram">
 * 
 * @author mey
 * 
 */
/*
@formatter:off
@startuml doc-files/gen/ConsumeSystem.svg

start
:compute consumed energy
(net cost of swimming + RMR);
partition "Transfer Digested Energy" {
    start
    :subtract consumed
    from excess and
    digested energy;
    if (remaining < 0) then
        partition "Subtract Energy" {
            :amount to subtract<
            while (amount not subtracted\n and compartments left)
                :subtract energy from compartment;
                :use next compartment
                in order: shortterm, fat, protein;
            endwhile
        }
    :reject energy lack
    (non-zero if compartments are empty)>
    else
        :add remaining
        to shortterm;
        if (surplus > 0) then
            :divide surplus between
            fat, protein and reproduction
            according to growth fractions;
            :store rejected energy in excess;
        else
        endif
    :reject nothing>
    endif
}
if (energy lack present?) then (yes)
    :agent dies from starvation;
    end
else (no)
    stop
endif

@enduml
@formatter:on
 */
public class ConsumeSystem extends AgentSystem {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(ConsumeSystem.class.getName());

    /**
     * Calculate consumed energy from RMR and cost factor of behavior. Subtract
     * that energy from compartments and kill the agent if they lack available
     * energy.
     */
    @Override
    protected void systemUpdate(Entity entity, SimState state) {
        Metabolizing metabolizing = entity.get(Metabolizing.class);
        LifeCycling lifeCycling = entity.get(LifeCycling.class);
        Compartments compartments = entity.get(Compartments.class);
        Moving moving = entity.get(Moving.class);
        Growing growing = entity.get(Growing.class);
        Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();

        Amount<Energy> costRestingMetabolism = metabolizing.getRestingMetabolicRate().times(deltaTime)
                .to(UnitConstants.CELLULAR_ENERGY);
        Amount<Energy> netCostSwimming = FormulaUtil.netCostOfSwimming(moving.getSpeed()).times(deltaTime)
                .to(UnitConstants.CELLULAR_ENERGY);
        Amount<Energy> totalEnergyCost = costRestingMetabolism.plus(netCostSwimming);

        metabolizing.setConsumedEnergy(totalEnergyCost);
        // subtract needed energy from compartments
        TransferDigestedResult transferDigestedResult = compartments.transferDigestedEnergyToCompartments(lifeCycling.isAdultFemale(),
                totalEnergyCost, entity, state);
        metabolizing.setNetEnergyIngested(transferDigestedResult.getNet());

        // also called when growing, but new values needed for killing the fish
        compartments.computeBiomassAndEnergy(growing);

        // if the needed energy is not available the fish starves to death
            //killAgent only if actual Biomass < 0.6*expectedBiomass
            if(growing.getBiomass().compareTo(growing.getExpectedBiomass().times(0.6d)) == -1)
            killAgent(entity, CauseOfDeath.STARVATION);
    }

    @Override
    protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
        return Arrays.asList(Metabolizing.class, Compartments.class, DynamicScheduling.class);
    }

    @Override
    public Collection<Class<? extends EntitySystem>> getDependencies() {
        return Arrays.asList(
                // for the current behavior mode
                BehaviorSystem.class,
                // for movement speed and updating delta time
                MoveSystem.class,
                // for the current age when transferring digested
                AgeSystem.class);
    }
}

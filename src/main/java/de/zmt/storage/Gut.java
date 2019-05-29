package de.zmt.storage;

import static javax.measure.unit.SI.*;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Aging;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.util.Bag;

/**
 * A limited {@link StoragePipeline} used to model a gut. Digesta are created
 * when food is added. They can be drained after a certain amount of time and
 * consumed or stored in other compartments.
 * 
 * @author mey
 *
 */
public class Gut extends AbstractLimitedStoragePipeline<Energy> implements Compartment {
    private static final long serialVersionUID = 1L;

    private static final int UPPER_LIMIT_GUT_MG_DW_FOOD_PER_G_WW_BIOMASS = 17;
    private static final Amount<Dimensionless> UPPER_LIMIT_FOOD_PER_BIOMASS = Amount.valueOf(
            UPPER_LIMIT_GUT_MG_DW_FOOD_PER_G_WW_BIOMASS,
            MILLI(GRAM).divide(UnitConstants.BIOMASS).asType(Dimensionless.class));

    private final SpeciesDefinition definition;
    private final Growing growing;
    private final Aging aging;
    private final Bag queue = new Bag(); //bag is faster than arrayList or similar
    private Amount<Energy> sum = Amount.valueOf(0,UnitConstants.CELLULAR_ENERGY);

    public Gut(final SpeciesDefinition definition, final Growing growing, Aging aging) {
        super(new SumStorage(UnitConstants.CELLULAR_ENERGY, growing, definition));
        this.growing = growing;
        this.definition = definition;
        this.aging = aging;
    }

    @Override
    public Amount<Mass> toMass() {
        return getType().toMass(getAmount());
    }

    @Override
    protected AbstractLimitedStoragePipeline.DelayedStorage<Energy> createDelayedStorage(Amount<Energy> storedAmount) {
        return new Digesta(storedAmount);
    }

    @Override
    public Type getType() {
        return Type.GUT;
    }

    public Amount<Energy> getAndRemoveProcessed(){
        Amount<Energy> total = Amount.valueOf(0, UnitConstants.CELLULAR_ENERGY);
        boolean objectsLeftToProcess = true;
        while (objectsLeftToProcess){
            if(queue.isEmpty())
                break;

            Digesta d = (Digesta)queue.get(0);

            if(d.getDelay(TimeUnit.SECONDS) <= 0) {
                total = total.plus(d.getAmount());
                if(sum.isLessThan(d.getAmount()))
                    sum = Amount.valueOf(0, UnitConstants.CELLULAR_ENERGY);
                else
                    sum = sum.minus(d.getAmount());

                queue.removeNondestructively(0); //a little bit slower but keeps ordering
            }else{
                //elements are sorted by time, first element not read therefore indicates that none remaining is ready
                objectsLeftToProcess = false;
            }
        }

        if(queue.isEmpty())
            sum = Amount.valueOf(0, UnitConstants.CELLULAR_ENERGY);

        return total.times(definition.getAssimilationEfficiency());
    }

    @Override
    public Amount<Energy> getAmount(){
        return sum;
    }

    public int getGutSize(){ return queue.size(); }

    @Override
    public ChangeResult<Energy> add(Amount<Energy> amount) {
         Amount<Energy> stored = Amount.valueOf(0,UnitConstants.CELLULAR_ENERGY);
        Amount<Energy> rejected = Amount.valueOf(0,UnitConstants.CELLULAR_ENERGY);
        Amount<Energy> freeSpace = getUpperLimit().minus(sum);
        if(freeSpace.getEstimatedValue() <= 0){
            rejected = amount;
        }else if(amount.isGreaterThan(freeSpace)){
            stored = stored.plus(freeSpace);
            rejected = rejected.plus(amount.minus(freeSpace));
        }else{
            stored = stored.plus(amount);
        }

        if(stored.isGreaterThan(Amount.valueOf(0,UnitConstants.CELLULAR_ENERGY))) {
            sum = sum.plus(stored);
            queue.add(new Digesta(stored));
        }

        return new ChangeResult<>(stored,rejected);
    }

    @Override
    public boolean atUpperLimit(){
        int result = sum.compareTo(getUpperLimit());
        return result >= 0;
    }

    public Amount<Energy> getUpperLimit() {
        return UPPER_LIMIT_FOOD_PER_BIOMASS.times(definition.getEnergyContentFood()).times(growing.getBiomass())
                .to(UnitConstants.CELLULAR_ENERGY);
    }

    /**
     * Food undergoing digestion.
     * 
     * @author mey
     * 
     */
    private class Digesta extends AbstractLimitedStoragePipeline.DelayedStorage<Energy> {
        private static final long serialVersionUID = 1L;

        /** Age of fish when digestion of this digesta is finished. */
        protected final Amount<Duration> digestionFinishedAge;

        /**
         * Create new digesta with given amount of energy.
         * 
         * @param energy
         *            in kJ
         */
        public Digesta(Amount<Energy> energy) {
            super(energy);
            this.digestionFinishedAge = aging.getAge().plus(definition.getGutTransitDuration());
        }

        @Override
        public long getDelay(TimeUnit unit) {
            Amount<Duration> delay = digestionFinishedAge.minus(aging.getAge());
            return AmountUtil.toTimeUnit(delay, unit);
        }

        @Override
        public int compareTo(Delayed o) {
            // shortcut for better performance
            if (o instanceof Gut.Digesta) {
                return digestionFinishedAge.compareTo(((Gut.Digesta) o).digestionFinishedAge);
            }
            return super.compareTo(o);
        }

    }

    /**
     * Stores sum of all {@link Digesta}s currently in gut and specifies limits.
     * 
     * @author mey
     *
     */
    private static class SumStorage extends ConfigurableStorage<Energy> {
        private static final long serialVersionUID = 1L;

        private static final int UPPER_LIMIT_GUT_MG_DW_FOOD_PER_G_WW_BIOMASS = 17;
        /**
         * Amount of food per biomass for deriving upper limit.
         * 
         * @see #getUpperLimit()
         */
        private static final Amount<Dimensionless> UPPER_LIMIT_FOOD_PER_BIOMASS = Amount.valueOf(
                UPPER_LIMIT_GUT_MG_DW_FOOD_PER_G_WW_BIOMASS,
                MILLI(GRAM).divide(UnitConstants.BIOMASS).asType(Dimensionless.class));

        private final Growing growing;
        private final SpeciesDefinition definition;

        private SumStorage(Unit<Energy> unit, Growing growing, SpeciesDefinition definition) {
            super(unit);
            this.growing = growing;
            this.definition = definition;
        }

        /**
         * Upper limit depending on biomass:
         * 
         * <pre>
         * upper_limit_kJ = {@value #UPPER_LIMIT_GUT_MG_DW_FOOD_PER_G_WW_BIOMASS} [mg/g, food dry weight per biomass]
         * 	&sdot; {@code energyContentFood} [kJ/g] &sdot; biomass [g]
         * </pre>
         */
        @Override
        protected Amount<Energy> getUpperLimit() {
            return UPPER_LIMIT_FOOD_PER_BIOMASS.times(definition.getEnergyContentFood()).times(growing.getBiomass())
                    .to(UnitConstants.CELLULAR_ENERGY);
        }

        @Override
        protected double getFactorOut() {
            // energy is lost while digesting
            return definition.getGutFactorOut();
        }
    }
}
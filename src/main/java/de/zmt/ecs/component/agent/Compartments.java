package de.zmt.ecs.component.agent;

import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;

import de.zmt.ecs.Entity;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.storage.Compartment;
import de.zmt.storage.ExcessStorage;
import de.zmt.storage.FatStorage;
import de.zmt.storage.Gut;
import de.zmt.storage.LimitedStorage;
import de.zmt.storage.MutableStorage;
import de.zmt.storage.ProteinStorage;
import de.zmt.storage.ReproductionStorage;
import de.zmt.storage.ShorttermStorage;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * Compound energy {@link MutableStorage} consisting of all simulated body
 * compartments that play a part in metabolism. Responsible for storing and
 * releasing energy.
 * <p>
 * Incoming raw energy from food is digested within the gut over a time span and
 * decreases in amount. After digestion, the energy is stored in a short-term
 * buffer where it is used directly for consumption or stored in other
 * compartments, which are either fat, protein or reproduction (ovaries).
 * <p>
 * Any energy left will be stored in excess, which makes the fish stop being
 * hungry if filled more than the desired amount. This ensures other
 * compartments being full before the fish stops feeding.
 *
 * @author mey
 */
public class Compartments implements LimitedStorage<Energy>, Proxiable, Component {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(Compartments.class.getName());

    /**
     * Compartments to consume from in depletion order.
     */
    private static final Compartment.Type[] CONSUMABLE_COMPARTMENTS = {Compartment.Type.SHORTTERM,
            Compartment.Type.FAT, Compartment.Type.PROTEIN};
    /**
     * Compartments included in biomass.
     */
    private static final Compartment.Type[] BIOMASS_COMPARTMENTS = {Compartment.Type.SHORTTERM, Compartment.Type.FAT,
            Compartment.Type.PROTEIN, Compartment.Type.EXCESS};

    /**
     * Gut storage. Processes food to energy. Modeled as portions of amounts of
     * energy that can be consumed after some time has passed.
     */
    private final Gut gut;
    /**
     * Short-term storage (kJ)
     */
    private final ShorttermStorage shortterm;
    /**
     * Fat storage (kJ)
     */
    private final FatStorage fat;
    /**
     * Protein storage (kJ). Represents vital body tissue like muscle, organs
     * and skin.
     */
    private final ProteinStorage protein;
    /**
     * Reproduction storage (kJ). Represents stored reproductive energy like
     * ovaries.
     */
    private final ReproductionStorage reproduction;
    private int reproductionsSinceLastUpdate = 0;
    /**
     * Excess storage (kJ). Stores energy if other compartments are full.
     */
    private final ExcessStorage excess;

    private boolean isMissingBiomass = false;

    /**
     * Creates a new compartment storage with given compartments.
     *
     * @param gut
     * @param shortterm
     * @param fat
     * @param protein
     * @param reproduction
     * @param excess
     * @see de.zmt.storage
     */
    public Compartments(Gut gut, ShorttermStorage shortterm, FatStorage fat, ProteinStorage protein,
                        ReproductionStorage reproduction, ExcessStorage excess) {
        this.gut = gut;
        this.shortterm = shortterm;
        this.fat = fat;
        this.protein = protein;
        this.reproduction = reproduction;
        this.excess = excess;
    }

    /**
     * Get digested energy from gut and transfer it to other compartments,
     * divided by given growth fractions. The consumed energy is subtracted
     * first from the digested, then from the other compartments.
     *
     * @param adultFemale
     * adult female fish transfer energy to reproduction storage
     * @param totalEnergyCost
     * the energy that was consumed
     * @return a change result which contains the energy that could not be
     * provided
     */
    private Amount<Mass> lastBiomass;

    public TransferDigestedResult transferDigestedEnergyToCompartments(boolean adultFemale,
                                                                       Amount<Energy> totalEnergyCost, Entity entity) {
        //gut.drainExpired() returns assimilated part of ingested energy portion available after digestion period from gut
        Amount<Energy> netEnergyIngested = gut.drainExpired();
        Amount<Energy> netEnergyGain = netEnergyIngested.minus(totalEnergyCost);
        Amount<Energy> availableEnergyExcess = excess.clear();
        Growing growing = entity.get(Growing.class);

        //if (netEnergyGain.getEstimatedValue() > 0)
         //   System.out.println("Grows: " + lastBiomass.isLessThan(growing.getBiomass()));

        //lastBiomass = growing.getBiomass();

        // System.out.print("BEFORE--Biomass: " + growing.getBiomass() + " Expected: " + growing.getExpectedBiomass()
        //         + " NetEnergyGain: " + netEnergyGain + " Excess: " + availableEnergyExcess
        //        + "\n");

        //in case of energy loss: re-metabolize energy from compartments in following order:
        // excess (only RMR costs) -> shortterm -> fat -> protein
        if (netEnergyGain.getEstimatedValue() < 0) {
            //negative netEnergyGain = energyDeficit
            Amount<Energy> energyDeficit = netEnergyGain;

            if (availableEnergyExcess.getEstimatedValue() > 0) {
                Metabolizing metabolizing = entity.get(Metabolizing.class);
                Amount<Duration> deltaTime = entity.get(DynamicScheduling.class).getDeltaTime();
                Amount<Energy> costRestingMetabolism = metabolizing.getRestingMetabolicRate().times(deltaTime)
                        .to(UnitConstants.CELLULAR_ENERGY);

                if (availableEnergyExcess.isGreaterThan(costRestingMetabolism)) {
                    energyDeficit = energyDeficit.plus(costRestingMetabolism);
                    availableEnergyExcess = availableEnergyExcess.minus(costRestingMetabolism);
                } else {
                    energyDeficit = energyDeficit.plus(availableEnergyExcess);
                    availableEnergyExcess = availableEnergyExcess.minus(availableEnergyExcess);
                }
            }
            excess.add(availableEnergyExcess);

            Amount<Energy> consumedFromCompartments = AmountUtil.zero(netEnergyGain);
            for (int i = 0; energyDeficit.getEstimatedValue() < 0 && i < CONSUMABLE_COMPARTMENTS.length; i++) {
                // take the next compartment until nothing gets rejected (= energyDeficit could be satisfied from compartments)
                // if last compartment still rejects, the fish will die of starvation if biomass < 60% of expectedBiomass
                ChangeResult<Energy> result = getStorage(CONSUMABLE_COMPARTMENTS[i]).add(energyDeficit);
                // rejected because it surpasses lower limit-> rejected to provide requested energy
                energyDeficit = result.getRejected();
                consumedFromCompartments = consumedFromCompartments.plus(result.getStored());
            }
            return new TransferDigestedResult(new ChangeResult<Energy>(consumedFromCompartments, energyDeficit),
                    netEnergyIngested);
        } else {
            //fish only grows if biomass does not exceed 120% of expectedBiomass
            if (growing.isLower120ExpectedBiomass()) {
                // add netEnergyGain to shortterm:
                ChangeResult<Energy> shorttermResult = shortterm.add(netEnergyGain);
                Amount<Energy> stored = shorttermResult.getStored();
                //if shortterm is full, add additional energy to compartments with certain loss
                //order: fat -> protein/repo -> excess
                Amount<Energy> surplus = shorttermResult.getRejected();
                if (surplus.getEstimatedValue() > 0) {
                    Amount<Energy> surplusFat = surplus.times(Compartment.Type.FAT.getGrowthFraction(adultFemale));
                    ChangeResult<Energy> fatResult = fat.add(surplusFat);
                    surplus.minus(surplusFat.minus(fatResult.getRejected()));

                    Amount<Energy> surplusProtein = surplus.times(Compartment.Type.PROTEIN.getGrowthFraction(adultFemale));
                    ChangeResult<Energy> proteinResult = protein.add(surplusProtein);
                    surplus.minus(surplusProtein.minus(proteinResult.getRejected()));

                    Amount<Energy> surplusRepro = surplus.times(Compartment.Type.REPRODUCTION.getGrowthFraction(adultFemale));
                    ChangeResult<Energy> reproductionResult = reproduction.add(surplusRepro);
                    surplus.minus(surplusRepro.minus(reproductionResult.getRejected()));

                    // energy surplus that cannot be stored in compartments -> stored in excess
                    ChangeResult<Energy> excessResult = excess.add(surplus);

                    // sum stored energy
                    stored = stored.plus(fatResult.getStored()).plus(proteinResult.getStored())
                            .plus(reproductionResult.getStored().plus(excessResult.getStored()));
                }
                return new TransferDigestedResult(stored, AmountUtil.zero(totalEnergyCost), netEnergyIngested);
            }
            return new TransferDigestedResult(AmountUtil.zero(totalEnergyCost), AmountUtil.zero(totalEnergyCost), netEnergyIngested);
        }
    }

    /**
     * @return sum of mass amounts from all compartments excluding gut and
     * reproduction.
     */
    private Amount<Mass> computeBiomass() {
        Amount<Mass> biomass = AmountUtil.zero(UnitConstants.BIOMASS);
        for (Compartment.Type type : BIOMASS_COMPARTMENTS) {
            biomass = biomass.plus(getStorage(type).toMass());
        }
        return biomass;
    }

    /**
     * computes the energy of all biomass compartments
     */
    private Amount<Energy> computeEnergy() {
        Amount<Energy> result = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);
        for (Compartment.Type type : BIOMASS_COMPARTMENTS) {
            result = result.plus(getStorageAmount(type));
        }
        return result;
    }

    public void computeBiomassAndEnergy(Growing growing) {
        growing.setEnergy(computeEnergy());
        growing.setBiomass(computeBiomass());
    }

    /**
     * @param type
     * @return Energy stored in given {@link de.zmt.storage.Compartment.Type}
     */
    public Amount<Energy> getStorageAmount(Compartment.Type type) {
        return getStorage(type).getAmount();
    }

    /**
     * Triggers reproduction if possible. If the reproduction storage contains
     * enough energy it is cleared, i.e. the ovaries are released.
     *
     * @return the amount cleared from the reproduction storage or
     * <code>null</code> if reproduction was not possible
     */
    public Amount<Energy> tryReproduction() {
        reproduction.refreshUpperLimit();
        if (reproduction.atUpperLimit()) {
            reproduction.refreshLowerLimit();
            reproductionsSinceLastUpdate++;
            return reproduction.clear();
        }
        return null;
    }

    public int getReproductionsSinceLastUpdate() {
        return reproductionsSinceLastUpdate;
    }

    public void clearReproductionSinceLastUpdate() {
        reproductionsSinceLastUpdate = 0;
    }

    /**
     * @param type
     * @return object of compartment with given type
     */
    private Compartment getStorage(Compartment.Type type) {
        switch (type) {
            case GUT:
                return gut;
            case SHORTTERM:
                return shortterm;
            case FAT:
                return fat;
            case PROTEIN:
                return protein;
            case REPRODUCTION:
                return reproduction;
            case EXCESS:
                return excess;
            default:
                throw new IllegalArgumentException("Invalid compartment type.");
        }
    }

    /**
     * The agent stops being hungry if the gut is at its maximum capacity or the
     * excess storage contains the desired amount of energy.
     *
     * @return <code>true</code> until gut is at maximum capacity or desired
     * excess amount is achieved
     */
    public boolean isHungry() {
        return !(atUpperLimit() || excess.atDesired()) && isMissingBiomass;
    }

    public void setIsMissingBiomass(boolean missing){
        isMissingBiomass = missing;
    }

    public boolean isMissingBiomass(){
        return isMissingBiomass;
    }

    /**
     * Sum of energy stored in all compartments
     */
    @Override
    public Amount<Energy> getAmount() {
        Amount<Energy> sum = AmountUtil.zero(UnitConstants.CELLULAR_ENERGY);

        for (Compartment.Type type : Compartment.Type.values()) {
            sum = sum.plus(getStorageAmount(type));
        }

        return sum;
    }

    /**
     * Changed it so that amount will now be added to gut regardless
     * of it's value. If the fish needs to consume it's compartments,
     * because of missing energy, that now happens in transferDigestedEnergyToCompartments
     * (code unchanged except for names).
     * <p>
     * This function is actually only used when feeding, didn't change
     * the name since it uses a base method which might be relevant,
     * somewhere in the future or in the background, but should currently
     * not matter.
     * <p>
     * And technically the fish is now able to vomit
     */
    public ChangeResult<Energy> addToGut(Amount<Energy> amount) {
        return gut.add(amount);
    }

    /**
     * currently unused
     *
     * @param amount
     * @return
     */
    @Override
    public ChangeResult<Energy> add(Amount<Energy> amount) {
        return null;
    }

    @Override
    public Amount<Energy> store(Amount<Energy> amount) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * Clears all compartments and returns the sum.
     */
    @Override
    public Amount<Energy> clear() {
        return gut.clear().plus(shortterm.clear()).plus(fat.clear()).plus(protein.clear()).plus(reproduction.clear());
    }

    /**
     * Returns <code>true</code> if no energy can be retrieved, i.e. if the
     * protein storage is at its minimum capacity.
     */
    @Override
    public boolean atLowerLimit() {
        return protein.atLowerLimit();
    }

    /**
     * Returns <code>true</code> if no incoming energy can be stored, i.e. if
     * the gut is at its maximum capacity.
     */
    @Override
    public boolean atUpperLimit() {
        return gut.atUpperLimit();
    }

    @Override
    public double doubleValue() {
        return getAmount().getEstimatedValue();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [total=" + getAmount() + "]";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
        public Valuable getTotal() {
            return AmountValuable.wrap(getAmount());
        }

        public Gut getGut() {
            return gut;
        }

        public ShorttermStorage getShortterm() {
            return shortterm;
        }

        public FatStorage getFat() {
            return fat;
        }

        public ProteinStorage getProtein() {
            return protein;
        }

        public ReproductionStorage getReproduction() {
            return reproduction;
        }

        public ExcessStorage getExcess() {
            return excess;
        }

        public boolean isHungry() {
            return Compartments.this.isHungry();
        }

        @Override
        public String toString() {
            return Compartments.this.getClass().getSimpleName();
        }
    }

    public static class TransferDigestedResult extends ChangeResult<Energy> {
        private final Amount<Energy> net;

        private TransferDigestedResult(Amount<Energy> stored, Amount<Energy> rejected, Amount<Energy> net) {
            super(stored, rejected);
            this.net = net;
        }

        private TransferDigestedResult(ChangeResult<Energy> result, Amount<Energy> net) {
            this(result.getStored(), result.getRejected(), net);
        }

        /**
         * @return net energy that was passed from {@link Gut} to other
         * compartments
         */
        public Amount<Energy> getNet() {
            return net;
        }
    }
}
package de.zmt.storage;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.SpecificEnergy;
import sim.util.AmountValuable;
import sim.util.Valuable;

/**
 * Body compartment identified by its {@link Type}.
 * 
 * @author mey
 * 
 */
public interface Compartment extends LimitedStorage<Energy> {
    /**
     * @return {@link Type} of compartment.
     */
    Type getType();

    /**
     * @return energy stored in compartment converted to mass
     */
    Amount<Mass> toMass();

    /**
     * Body compartment types storing energy including conversion methods from
     * mass to energy and energy to mass.
     * 
     * @author mey
     * 
     */
    public enum Type {
        GUT, SHORTTERM, FAT, PROTEIN, REPRODUCTION, EXCESS;

        private static final String ILLEGAL_ARGUMENT_MSG = "No valid value for ";

        // ENERGY-BIOMASS CONVERSIONS
        public static final double KJ_PER_GRAM_FAT_VALUE = 39.5;
        public static final double KJ_PER_GRAM_PROTEIN_VALUE = 4.1;
        //Wootton: 23.5 kJ/g DW fish -> conversion to wet weight:
        //moisture content of parrot fish (Karakoltidis et al. 1995) = ca. 78%
        //equation(from researchgate):Conc in wet weight = ((100 - % of water)/100)* Conc in dry weight
        //(100-78)/100*23.5 = 5.17, but for simplicity I use same as protein!
        public static final double KJ_PER_GRAM_REPRO_VALUE = 4.1;
        /**
         * 1 g fat = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable energy
         * 
         * @see #GRAM_PER_KJ_FAT
         */
        private static final Amount<SpecificEnergy> KJ_PER_GRAM_FAT = Amount.valueOf(KJ_PER_GRAM_FAT_VALUE,
                UnitConstants.ENERGY_CONTENT_TISSUE);
        /**
         * 1 g protein = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable energy
         * 
         * @see #GRAM_PER_KJ_PROTEIN
         */
        private static final Amount<SpecificEnergy> KJ_PER_GRAM_PROTEIN = Amount.valueOf(KJ_PER_GRAM_PROTEIN_VALUE,
                UnitConstants.ENERGY_CONTENT_TISSUE);
        /**
         * 1 g reproduction = {@value #KJ_PER_GRAM_FAT_VALUE} kJ metabolizable
         * energy
         * 
         * @see #GRAM_PER_KJ_REPRO
         */
        private static final Amount<SpecificEnergy> KJ_PER_GRAM_REPRO = Amount.valueOf(KJ_PER_GRAM_REPRO_VALUE,
                UnitConstants.ENERGY_CONTENT_TISSUE);
        /**
         * 1 kJ fat = (1/{@value #KJ_PER_GRAM_FAT_VALUE}) g fat in biomass
         * 
         * @see #KJ_PER_GRAM_FAT
         */
        private static final Amount<?> GRAM_PER_KJ_FAT = KJ_PER_GRAM_FAT.inverse();
        /**
         * 1 kJ gut / short-term / protein = (1/
         * {@value #KJ_PER_GRAM_PROTEIN_VALUE}) g protein in biomass
         * 
         * @see #KJ_PER_GRAM_PROTEIN
         */
        private static final Amount<?> GRAM_PER_KJ_PROTEIN = KJ_PER_GRAM_PROTEIN.inverse();
        /**
         * 1 kJ reproduction = (1/{@value #KJ_PER_GRAM_REPRO_VALUE}) g
         * reproduction in biomass
         * 
         * @see #KJ_PER_GRAM_REPRO
         */
        private static final Amount<?> GRAM_PER_KJ_REPRO = KJ_PER_GRAM_REPRO.inverse();

        // GROWTH FRACTIONS
        /** Fraction of protein biomass growth from total. */
        private static final double GROWTH_FRACTION_PROTEIN_NONREPRODUCTIVE = 0.99;
        /**
         * Fraction of fat biomass growth from total for non-reproductive fish.
         */

        private static final double GROWTH_FRACTION_FAT_NONREPRODUCTIVE = 1 - GROWTH_FRACTION_PROTEIN_NONREPRODUCTIVE;
        /**
         * Fraction of reproduction biomass growth from total for reproductive
         * fish.
         */
        private static final double GROWTH_FRACTION_REPRO_REPRODUCTIVE = 0.1;
        /** Fraction of protein biomass growth from total for reproductive fish. */
        private static final double GROWTH_FRACTION_PROTEIN_REPRODUCTIVE = 0.899;
        /** Fraction of fat biomass growth from total for reproductive fish. */
        private static final double GROWTH_FRACTION_FAT_REPRODUCTIVE = 1 - (GROWTH_FRACTION_PROTEIN_REPRODUCTIVE + GROWTH_FRACTION_REPRO_REPRODUCTIVE);
        /**
         * 
         * @param mass
         * @return energy contained in given {@code mass} of this type
         */
        public Amount<Energy> toEnergy(Amount<Mass> mass) {
            return mass.times(getEnergyPerMass()).to(UnitConstants.CELLULAR_ENERGY);
        }

        private Amount<SpecificEnergy> getEnergyPerMass() {
            switch (this) {
            case FAT:
                return KJ_PER_GRAM_FAT;
            case SHORTTERM:
            case PROTEIN:
            case EXCESS:
                return KJ_PER_GRAM_PROTEIN;
            case REPRODUCTION:
                return KJ_PER_GRAM_REPRO;
            default:
                throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
            }
        }

        /**
         * 
         * @param energy
         * @return mass needed to store given {@code energy} of this type
         */
        public Amount<Mass> toMass(Amount<Energy> energy) {
            return energy.times(getMassPerEnergy()).to(UnitConstants.BIOMASS);
        }

        private Amount<?> getMassPerEnergy() {
            switch (this) {
            case FAT:
                return GRAM_PER_KJ_FAT;
            case SHORTTERM:
            case PROTEIN:
            case EXCESS:
                return GRAM_PER_KJ_PROTEIN;
            case REPRODUCTION:
                return GRAM_PER_KJ_REPRO;
            default:
                throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
            }
        }

        /**
         * @param reproductive
         *            reproductive fish store energy for reproduction
         * @return Fraction of growth for this compartment type.
         */
        public double getGrowthFraction(boolean reproductive) {
            switch (this) {
            case FAT:
                return reproductive ? GROWTH_FRACTION_FAT_REPRODUCTIVE : GROWTH_FRACTION_FAT_NONREPRODUCTIVE;
                case PROTEIN:
                return reproductive ? GROWTH_FRACTION_PROTEIN_REPRODUCTIVE : GROWTH_FRACTION_PROTEIN_NONREPRODUCTIVE;
            case REPRODUCTION:
                return reproductive ? GROWTH_FRACTION_REPRO_REPRODUCTIVE : 0;
            default:
                throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MSG + this);
            }
        }
    }

    /**
     * Abstract implementation for a {@link Compartment} using {@link Unit}
     * defined in {@link UnitConstants#CELLULAR_ENERGY}.
     * 
     * @author mey
     * 
     */
    public abstract static class AbstractCompartmentStorage extends ConfigurableStorage<Energy> implements Compartment {
        private static final long serialVersionUID = 1L;
        private static final Unit<Energy> UNIT = UnitConstants.CELLULAR_ENERGY;

        public AbstractCompartmentStorage(Amount<Energy> amount) {
            this();
            this.setAmount(amount);
        }

        /**
         * Create a new empty energy storage.
         */
        public AbstractCompartmentStorage() {
            super(UNIT);
        }

        /**
         * @param fillLevel
         *            value between 0-1 defining the initial fill level between
         *            lower and upper limit
         */
        protected void fill(double fillLevel) {
            if (fillLevel < 0 || fillLevel > 1) {
                throw new IllegalArgumentException(fillLevel + " is not a valid value between 0 and 1.");
            }

            double lowerLimitValue = getLowerLimit().doubleValue(UNIT);
            double upperLimitValue = getUpperLimit().doubleValue(UNIT);
            double fillValue = (fillLevel * (upperLimitValue - lowerLimitValue)) + lowerLimitValue;
            setAmount(Amount.valueOf(fillValue, UNIT));
        }

        @Override
        public Amount<Mass> toMass() {
            return getType().toMass(getAmount());
        }

        @Override
        public Object propertiesProxy() {
            return new MyPropertiesProxy();
        }

        public class MyPropertiesProxy extends ConfigurableStorage<Energy>.MyPropertiesProxy {
            public Valuable getMassAmount() {
                return AmountValuable.wrap(toMass());
            }
        }
    }
}
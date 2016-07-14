package de.zmt.params;

import static de.zmt.util.Habitat.*;

import java.util.Map;

import javax.measure.quantity.Frequency;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.accessor.DefinitionAccessor;
import de.zmt.params.accessor.MapAccessor;
import de.zmt.params.def.EnumToAmountMap;
import de.zmt.util.AmountUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.CombinedInspector;

/**
 * Class associating each habitat with a predation risk. Estimated predation
 * risk are summarizing factors of habitat complexity, available refuge and
 * predator abundances.
 * <p>
 * <b>NOTE:</b> Habitat predation risks will be converted from per day to per
 * step. This will lead to a different number of deaths per day, because dead
 * fish are subtracted from total number immediately and the check is much more
 * often.
 * 
 * @author mey
 *
 */
class PredationRisks extends MapParamDefinition<Habitat, Amount<Frequency>> {
    private static final long serialVersionUID = 1L;

    private static final double CORALREEF_DEFAULT_FACTOR = 0;
    private static final double SEAGRASS_DEFAULT_FACTOR = 0;
    private static final double MANGROVE_DEFAULT_FACTOR = 0;
    private static final double ROCK_DEFAULT_FACTOR = 0.25;
    private static final double SANDYBOTTOM_DEFAULT_FACTOR = 0.5;
    /** Constant value for inaccessible (not editable). Always highest. */
    private static final Amount<Frequency> INACCESSIBLE_PER_DAY_VALUE = Amount.valueOf(1, UnitConstants.PER_STEP);

    @XStreamImplicit
    private final MyMap map = new MyMap();

    private transient Amount<Frequency> minRisk = null;
    private transient Amount<Frequency> maxRisk = null;

    /**
     * Default constructor. Used internally for XML unmarshalling.
     */
    @SuppressWarnings("unused")
    private PredationRisks() {
        this(AmountUtil.zero(UnitConstants.PER_STEP));
    }

    /**
     * Constructs a new {@link PredationRisks} instance. Each habitat is
     * initialized with its default predation risk.
     * 
     * @param naturalMortalityRisk
     *            the natural mortality risk used as base for the default
     *            predation risks
     */
    public PredationRisks(Amount<Frequency> naturalMortalityRisk) {
        super();

        // associate each habitat with its default predation risk
        putDefaultRisk(CORALREEF, naturalMortalityRisk, CORALREEF_DEFAULT_FACTOR);
        putDefaultRisk(SEAGRASS, naturalMortalityRisk, SEAGRASS_DEFAULT_FACTOR);
        putDefaultRisk(MANGROVE, naturalMortalityRisk, MANGROVE_DEFAULT_FACTOR);
        putDefaultRisk(ROCK, naturalMortalityRisk, ROCK_DEFAULT_FACTOR);
        putDefaultRisk(SANDYBOTTOM, naturalMortalityRisk, SANDYBOTTOM_DEFAULT_FACTOR);
    }

    /**
     * Calculates default risk by {@code base * factor} and associates it.
     * 
     * @param habitat
     * @param base
     *            the base for the default predation risks
     * @param factor
     */
    private void putDefaultRisk(Habitat habitat, Amount<Frequency>base, double factor) {
        put(habitat, base.times(factor));
    }

    /** @return minimum predation risk for accessible habitats */
    public Amount<Frequency> getMinPredationRisk() {
        return minRisk;
    }

    /** @return maximum predation risk for accessible habitats */
    public Amount<Frequency> getMaxPredationRisk() {
        return maxRisk;
    }

    public Amount<Frequency> get(Habitat key) {
        if (!key.isAccessible()) {
            return INACCESSIBLE_PER_DAY_VALUE;
        }
        return getMap().get(key);
    }

    /**
     * Associates a habitat with a predation risk. May also update habitat with
     * maximum risk.
     * 
     * @param habitat
     * @param predationRisk
     * @return previously associated predation risk
     */
    Amount<Frequency> put(Habitat habitat, Amount<Frequency> predationRisk) {
        Amount<Frequency> previousRisk = getMap().put(habitat, predationRisk);
        updateBounds();
        return previousRisk;
    }

    /**
     * Updates {@link #maxRisk} and {@link #minRisk} by looking through all
     * habitats for the highest risk.
     */
    private void updateBounds() {
        for (Habitat habitat : getMap().keySet()) {
            Amount<Frequency> risk = get(habitat);
            if (maxRisk == null || risk.isGreaterThan(getMaxPredationRisk())) {
                maxRisk = risk;
            }
            if (minRisk == null || risk.isLessThan(getMinPredationRisk())) {
                minRisk = risk;
            }
        }
    }

    private Object readResolve() {
        updateBounds();
        return this;
    }

    @Override
    public Map<Habitat, Amount<Frequency>> getMap() {
        return map;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
        Inspector inspector = Inspector.getInspector(getMap(), state, name);
        Inspector wrappingInspector = new CombinedInspector(inspector) {
            private static final long serialVersionUID = 1L;

            @Override
            public void updateInspector() {
                // bounds update needed when values are changed via GUI
                updateBounds();
                super.updateInspector();
            }

        };
        wrappingInspector.setTitle(getClass().getSimpleName());
        return wrappingInspector;
    }

    @Override
    public DefinitionAccessor<?> accessor() {
        // bounds update when value is set
        return new MapAccessor<Habitat, Amount<Frequency>>(getMap()) {

            @Override
            public Amount<Frequency> set(Identifier<?> identifier, Object value) {
                Amount<Frequency> oldValue = super.set(identifier, value);
                updateBounds();
                return oldValue;
            }

        };
    }

    /**
     * {@link EnumToAmountMap} that checks added risks to be in a valid range.
     * 
     * @author mey
     *
     */
    private static class MyMap extends EnumToAmountMap<Habitat, Frequency> {
        private static final long serialVersionUID = 1L;

        public MyMap() {
            super(Habitat.class, UnitConstants.PER_STEP, UnitConstants.PER_YEAR);
        }

        @Override
        public Amount<Frequency> put(Habitat habitat, Amount<Frequency> predationRisk) {
            Unit<Frequency> storeUnit = getStoreUnit();
            // null when deserializing
            if (storeUnit != null) {
                double predationRiskStore = predationRisk.doubleValue(storeUnit);
                if (predationRiskStore < 0 || predationRiskStore > 1) {
                    throw new IllegalArgumentException(
                            "Invalid value: " + predationRisk.to(storeUnit) + " (Risks must be probabilities [0-1])");
                }
            }
            return super.put(habitat, predationRisk);
        }

    }
}

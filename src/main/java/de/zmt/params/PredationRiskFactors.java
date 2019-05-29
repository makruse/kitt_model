package de.zmt.params;

import static de.zmt.util.Habitat.CORALREEF;
import static de.zmt.util.Habitat.MANGROVE;
import static de.zmt.util.Habitat.ROCK;
import static de.zmt.util.Habitat.SANDYBOTTOM;
import static de.zmt.util.Habitat.SEAGRASS;

import java.util.EnumMap;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.accessor.DefinitionAccessor;
import de.zmt.params.accessor.MapAccessor;
import de.zmt.util.Habitat;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.CombinedInspector;

/**
 * Class associating each habitat with a predation risk factor. Estimated
 * predation risks are summarizing factors of habitat complexity, available
 * refuge and predator abundances.
 * 
 * @author mey
 *
 */
class PredationRiskFactors extends MapParamDefinition<Habitat, Double> {
    private static final long serialVersionUID = 1L;

    private static final double CORALREEF_DEFAULT_FACTOR = 1.1; //0
    private static final double SEAGRASS_DEFAULT_FACTOR = 1.1;
    private static final double MANGROVE_DEFAULT_FACTOR = 1.1;
    private static final double ROCK_DEFAULT_FACTOR = 1.3;
    private static final double SANDYBOTTOM_DEFAULT_FACTOR = 1.5; //0.5
    /** Constant factor for inaccessible (not editable). Always highest. */
    private static final double INACCESSIBLE_RISK_FACTOR = 1;

    @XStreamImplicit
    private final Map<Habitat, Double> map = new EnumMap<>(Habitat.class);

    private transient Double minRiskFactor;
    private transient Double maxRiskFactor;

    /**
     * Constructs a new {@link PredationRiskFactors} object with default values.
     */
    public PredationRiskFactors() {
        // associate each habitat with its default factor
        put(CORALREEF, CORALREEF_DEFAULT_FACTOR);
        put(SEAGRASS, SEAGRASS_DEFAULT_FACTOR);
        put(MANGROVE, MANGROVE_DEFAULT_FACTOR);
        put(ROCK, ROCK_DEFAULT_FACTOR);
        put(SANDYBOTTOM, SANDYBOTTOM_DEFAULT_FACTOR);
    }

    /**
     * Returns the minimum predation risk factor for accessible habitats.
     * 
     * @return the minimum predation risk factor for accessible habitats
     */
    public double getMinRiskFactor() {
        assert minRiskFactor != null;
        return minRiskFactor;
    }

    /**
     * Returns the maximum predation risk factor for accessible habitats.
     * 
     * @return the maximum predation risk factor for accessible habitats
     */
    public double getMaxRiskFactor() {
        assert maxRiskFactor != null;
        return maxRiskFactor;
    }

    /**
     * Gets the risk factor for the given habitat.
     * 
     * @param key
     *            the habitat key
     * @return the risk for given habitat
     */
    public double get(Habitat key) {
        if (!key.isAccessible()) {
            return INACCESSIBLE_RISK_FACTOR;
        }
        return getMap().get(key);
    }

    /**
     * Associates an accessible habitat with a predation risk factor. Will also
     * update minimum and maximum risk factors.
     * 
     * @param habitat
     *            the habitat, must be accessible
     * @param predationRiskFactor
     *            the factor to associate
     * @return previously associated predation risk or <code>null</code>
     */
    Double put(Habitat habitat, double predationRiskFactor) {
        if (!habitat.isAccessible()) {
            throw new IllegalArgumentException(
                    habitat + " is not accessible. Risk factors can only be set for accessible habitats.");
        }
        /*if (predationRiskFactor < 0 || predationRiskFactor > 1) {
            throw new IllegalArgumentException(
                    "Invalid value: " + predationRiskFactor + " (Risks must be probabilities [0-1])");
        }*/ //values are given as FACTORS -> they should increase the value therefore over 1

        Double previousRiskFactor = getMap().put(habitat, predationRiskFactor);
        updateBounds();
        return previousRiskFactor;
    }

    /**
     * Updates {@link #maxRiskFactor} and {@link #minRiskFactor} by looking
     * through all habitats for the highest and lowest risk factors.
     */
    private void updateBounds() {
        maxRiskFactor = null;
        minRiskFactor = null;

        for (Habitat habitat : getMap().keySet()) {
            double riskFactor = get(habitat);
            if (maxRiskFactor == null || riskFactor > maxRiskFactor) {
                maxRiskFactor = riskFactor;
            }
            if (minRiskFactor == null || riskFactor < minRiskFactor) {
                minRiskFactor = riskFactor;
            }
        }
    }

    private Object readResolve() {
        updateBounds();
        return this;
    }

    @Override
    public Map<Habitat, Double> getMap() {
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
        return new MapAccessor<Habitat, Double>(getMap()) {

            @Override
            public Double set(Identifier<?> identifier, Object value) {
                Double oldValue = super.set(identifier, value);
                updateBounds();
                return oldValue;
            }

        };
    }
}

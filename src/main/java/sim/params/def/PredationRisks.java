package sim.params.def;

import static de.zmt.util.Habitat.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.measure.quantity.Frequency;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;

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
class PredationRisks extends EnumToAmountMap<Habitat, Frequency> {
    private static final long serialVersionUID = 1L;

    private static final double CORALREEF_DEFAULT_FACTOR = 0;
    private static final double SEAGRASS_DEFAULT_FACTOR = 0;
    private static final double MANGROVE_DEFAULT_FACTOR = 0;
    private static final double ROCK_DEFAULT_FACTOR = 0.25;
    private static final double SANDYBOTTOM_DEFAULT_FACTOR = 0.5;
    /** Constant value for inaccessible (not editable). Always highest. */
    private static final Amount<Frequency> INACCESSIBLE_PER_DAY_VALUE = Amount.valueOf(1, UnitConstants.PER_STEP);

    @XmlTransient
    private Amount<Frequency> minRisk = null;
    @XmlTransient
    private Amount<Frequency> maxRisk = null;

    /**
     * Default constructor. Used internally for XML unmarshalling.
     */
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
	super(Habitat.class, UnitConstants.PER_STEP, UnitConstants.PER_YEAR);

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
	put(habitat, base.times(factor).to(getStoreUnit()));
    }

    /** @return minimum predation risk for accessible habitats */
    public Amount<Frequency> getMinPredationRisk() {
	return minRisk;
    }

    /** @return maximum predation risk for accessible habitats */
    public Amount<Frequency> getMaxPredationRisk() {
	return maxRisk;
    }

    @Override
    public Amount<Frequency> get(Object key) {
	if (!Habitat.class.cast(key).isAccessible()) {
	    return INACCESSIBLE_PER_DAY_VALUE;
	}
	return super.get(key);
    }

    /**
     * Associates a habitat with a predation risk. May also update habitat with
     * maximum risk.
     * 
     * @param habitat
     * @param predationRisk
     * @return previously associated predation risk
     */
    @Override
    public Amount<Frequency> put(Habitat habitat, Amount<Frequency> predationRisk) {
	double predationRiskStore = predationRisk.doubleValue(getStoreUnit());
	if (predationRiskStore < 0 || predationRiskStore > 1) {
	    throw new IllegalArgumentException(
		    "Invalid value: " + predationRisk.to(getStoreUnit()) + " (Risks must be probabilities [0-1])");
	}

	Amount<Frequency> previousRisk = super.put(habitat, predationRisk);
	updateBounds();
	return previousRisk;
    }

    /**
     * Updates {@link #maxRisk} and {@link #minRisk} by looking through all
     * habitats for the highest risk.
     */
    private void updateBounds() {
	for (Habitat habitat : keySet()) {
	    Amount<Frequency> risk = get(habitat);
	    if (maxRisk == null || risk.isGreaterThan(getMaxPredationRisk())) {
		maxRisk = risk;
	    }
	    if (minRisk == null || risk.isLessThan(getMinPredationRisk())) {
		minRisk = risk;
	    }
	}
    }

    /**
     * Calls {@link #updateBounds()} after unmarshalling from XML.
     * 
     * @param unmarshaller
     * @param parent
     */
    @SuppressWarnings("unused") // used by jaxb
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	updateBounds();
    }

    static class MyXmlAdapter extends XmlAdapter<MyXmlEntry[], PredationRisks> {

	@Override
	public PredationRisks unmarshal(MyXmlEntry[] v) throws Exception {
	    PredationRisks map = new PredationRisks();

	    for (MyXmlEntry entry : v) {
		map.put(entry.key, entry.value);
	    }
	    return map;
	}

	@Override
	public MyXmlEntry[] marshal(PredationRisks map) throws Exception {
	    Collection<MyXmlEntry> entries = new ArrayList<>(map.size());
	    for (Map.Entry<Habitat, Amount<Frequency>> e : map.entrySet()) {
		entries.add(new MyXmlEntry(e));
	    }

	    return entries.toArray(new MyXmlEntry[map.size()]);
	}
    }

    @XmlType(namespace = "predationRisks")
    private static class MyXmlEntry {
	@XmlAttribute
	public final Habitat key;

	@XmlValue
	public final Amount<Frequency> value;

	@SuppressWarnings("unused") // needed by JAXB
	public MyXmlEntry() {
	    key = null;
	    value = null;
	}

	public MyXmlEntry(Map.Entry<Habitat, Amount<Frequency>> e) {
	    key = e.getKey();
	    value = e.getValue();
	}
    }
}

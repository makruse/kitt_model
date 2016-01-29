package sim.params.def;

import static de.zmt.util.Habitat.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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
 * 
 * @author mey
 *
 */
class PredationRisks extends EnumToAmountMap<Habitat, Frequency> {
    private static final long serialVersionUID = 1L;

    /*
     * NOTE: Default habitat predation risks will be converted from per day to
     * per step. This will lead to a slightly different number of deaths per
     * day, because dead fish are subtracted from total number immediately.
     */
    private static final double DEFAULT_CORALREEF_PER_DAY_VALUE = 0.002;
    private static final double DEFAULT_SEAGRASS_PER_DAY_VALUE = 0.001;
    private static final double DEFAULT_MANGROVE_PER_DAY_VALUE = 0.002;
    private static final double DEFAULT_ROCK_PER_DAY_VALUE = 0.004;
    private static final double DEFAULT_SANDYBOTTOM_PER_DAY_VALUE = 0.008;
    private static final double DEFAULT_MAINLAND_PER_DAY_VALUE = 1d;

    /** Habitats excluded from maximum. */
    private static final Set<Habitat> EXCLUDED_FROM_MAXIMUM = EnumSet.of(MAINLAND);

    @XmlTransient
    private Habitat maxRiskHabitat = Habitat.DEFAULT;

    /**
     * Constructs a new {@link PredationRisks} instance. Each habitat is
     * initialized with its default predation risk.
     */
    public PredationRisks() {
	super(Habitat.class, UnitConstants.PER_STEP, UnitConstants.PER_DAY);

	// associate each habitat with its default predation risk
	put(CORALREEF, DEFAULT_CORALREEF_PER_DAY_VALUE);
	put(SEAGRASS, DEFAULT_SEAGRASS_PER_DAY_VALUE);
	put(MANGROVE, DEFAULT_MANGROVE_PER_DAY_VALUE);
	put(ROCK, DEFAULT_ROCK_PER_DAY_VALUE);
	put(SANDYBOTTOM, DEFAULT_SANDYBOTTOM_PER_DAY_VALUE);
	put(MAINLAND, DEFAULT_MAINLAND_PER_DAY_VALUE);
    }

    /** @return maximum predation risk ({@link Habitat#MAINLAND} excluded) */
    public Amount<Frequency> getMaxPredationRisk() {
	Amount<Frequency> maxPredationRisk = get(maxRiskHabitat);
	if (maxPredationRisk == null) {
	    return AmountUtil.zero(Frequency.UNIT);
	}
	return maxPredationRisk;
    }

    /**
     * @see #getMaxPredationRisk()
     * @return habitat with maximum predation risk
     */
    public Habitat getMaxPredationRiskHabitat() {
	return maxRiskHabitat;
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
	Amount<Frequency> previousRisk = super.put(habitat, predationRisk);
	updateMaxRiskHabitat();
	return previousRisk;
    }

    /**
     * Updates {@link #maxRiskHabitat} by looking through all habitats for the
     * highest risk.
     */
    private void updateMaxRiskHabitat() {
	for (Habitat habitat : keySet()) {
	    if (!EXCLUDED_FROM_MAXIMUM.contains(habitat) && get(habitat).isGreaterThan(getMaxPredationRisk())) {
		maxRiskHabitat = habitat;
	    }
	}
    }

    /**
     * Finds {@link #maxRiskHabitat} after unmarshalling from XML.
     * 
     * @param unmarshaller
     * @param parent
     */
    @SuppressWarnings("unused") // used by jaxb
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	updateMaxRiskHabitat();
    }

    static class MyXmlAdapter extends XmlAdapter<MyXmlEntry[], Map<Habitat, Amount<Frequency>>> {

	@Override
	public Map<Habitat, Amount<Frequency>> unmarshal(MyXmlEntry[] v) throws Exception {
	    Map<Habitat, Amount<Frequency>> map = new EnumMap<>(Habitat.class);

	    for (MyXmlEntry entry : v) {
		map.put(entry.key, entry.value);
	    }
	    return map;
	}

	@Override
	public MyXmlEntry[] marshal(Map<Habitat, Amount<Frequency>> map) throws Exception {
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

package de.zmt.params.def;

import static de.zmt.ecs.component.agent.Metabolizing.BehaviorMode.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.measure.quantity.Frequency;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.util.UnitConstants;

/**
 * Class associating each behavior mode with a speed factor.
 * 
 * @author mey
 *
 */
class SpeedFactors extends EnumToAmountMap<BehaviorMode, Frequency> {
    private static final long serialVersionUID = 1L;

    public SpeedFactors() {
	super(BehaviorMode.class, UnitConstants.BODY_LENGTH_VELOCITY);

	put(FORAGING, 2.1);
	put(MIGRATING, 2.7);
	put(RESTING, 0);
    }

    static class MyXmlAdapter extends XmlAdapter<MyXmlEntry[], SpeedFactors> {

	@Override
	public SpeedFactors unmarshal(MyXmlEntry[] v) throws Exception {
	    SpeedFactors map = new SpeedFactors();

	    for (MyXmlEntry entry : v) {
		map.put(entry.key, entry.value);
	    }
	    return map;
	}

	@Override
	public MyXmlEntry[] marshal(SpeedFactors map) throws Exception {
	    Collection<MyXmlEntry> entries = new ArrayList<>(map.size());
	    for (Map.Entry<BehaviorMode, Amount<Frequency>> e : map.entrySet()) {
		entries.add(new MyXmlEntry(e));
	    }

	    return entries.toArray(new MyXmlEntry[map.size()]);
	}

    }

    @XmlType(namespace = "speedFactors")
    private static class MyXmlEntry {
	@XmlAttribute
	public final BehaviorMode key;

	@XmlValue
	public final Amount<Frequency> value;

	@SuppressWarnings("unused") // needed by JAXB
	public MyXmlEntry() {
	    key = null;
	    value = null;
	}

	public MyXmlEntry(Map.Entry<BehaviorMode, Amount<Frequency>> e) {
	    key = e.getKey();
	    value = e.getValue();
	}
    }

}
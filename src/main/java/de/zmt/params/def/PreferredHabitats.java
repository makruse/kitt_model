package de.zmt.params.def;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.util.Habitat;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.CheckBoxInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.CollectionProperties;

/**
 * Preferred habitats per {@link BehaviorMode}.
 * 
 * @author mey
 *
 */
class PreferredHabitats extends EnumMap<BehaviorMode, PreferredHabitats.HabitatSet> implements ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private static final EnumSet<Habitat> DEFAULT_RESTING_HABITATS = EnumSet.of(Habitat.CORALREEF);
    private static final EnumSet<Habitat> DEFAULT_FORAGING_HABITATS = EnumSet.of(Habitat.CORALREEF, Habitat.SEAGRASS);

    public PreferredHabitats() {
	super(BehaviorMode.class);
	put(BehaviorMode.FORAGING, new HabitatSet(DEFAULT_FORAGING_HABITATS));
	put(BehaviorMode.RESTING, new HabitatSet(DEFAULT_RESTING_HABITATS));
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new SimpleInspector(new MyProperties(), state, name);
    }

    @Override
    public String toString() {
	return getClass().getSimpleName();
    }

    /**
     * Habitat set wrapper for JAXB to specify type. Cannot extend
     * {@link EnumSet}, so we need to wrap it.
     * 
     * @author mey
     *
     */
    public static class HabitatSet implements Set<Habitat>, Serializable {
	private static final long serialVersionUID = 1L;

	private final Set<Habitat> set;

	@SuppressWarnings("unused") // needed by JAXB
	private HabitatSet() {
	    set = EnumSet.noneOf(Habitat.class);
	}

	public HabitatSet(Set<Habitat> set) {
	    this.set = set;
	}

	@Override
	public int size() {
	    return set.size();
	}

	@Override
	public boolean isEmpty() {
	    return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
	    return set.contains(o);
	}

	@Override
	public Iterator<Habitat> iterator() {
	    return set.iterator();
	}

	@Override
	public Object[] toArray() {
	    return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
	    return set.toArray(a);
	}

	@Override
	public boolean add(Habitat e) {
	    return set.add(e);
	}

	@Override
	public boolean remove(Object o) {
	    return set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
	    return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends Habitat> c) {
	    return set.addAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
	    return set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
	    return set.removeAll(c);
	}

	@Override
	public void clear() {
	    set.clear();
	}

	@Override
	public String toString() {
	    return set.toString();
	}
    }

    /**
     * Wraps habitat sets into
     * 
     * @author mey
     *
     */
    private class MyProperties extends CollectionProperties {
	private static final long serialVersionUID = 1L;

	public MyProperties() {
	    super(PreferredHabitats.this);
	}

	@Override
	public Class<?> getType(int index) {
	    return Set.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getValue(int index) {
	    return new ProvidesHabitatSetInspector((Set<Habitat>) super.getValue(index), getName(index));
	}

    }

    /**
     * Class providing an {@link Inspector} that displays habitats with check
     * boxes to choose from.
     * 
     * @author mey
     *
     */
    private static class ProvidesHabitatSetInspector extends CheckBoxInspector.ProvidesCheckBoxInspector<Habitat> {
	public ProvidesHabitatSetInspector(Set<Habitat> habitatSet, String name) {
	    super(habitatSet, Arrays.asList(Habitat.values()), name);
	}
    }

    static class MyXmlAdapter extends XmlAdapter<MyXmlEntry[], PreferredHabitats> {

	@Override
	public PreferredHabitats unmarshal(MyXmlEntry[] v) throws Exception {
	    PreferredHabitats map = new PreferredHabitats();

	    for (MyXmlEntry entry : v) {
		map.put(entry.key, entry.value);
	    }
	    return map;
	}

	@Override
	public MyXmlEntry[] marshal(PreferredHabitats map) throws Exception {
	    Collection<MyXmlEntry> entries = new ArrayList<>(map.size());
	    for (Map.Entry<BehaviorMode, HabitatSet> e : map.entrySet()) {
		entries.add(new MyXmlEntry(e));
	    }

	    return entries.toArray(new MyXmlEntry[map.size()]);
	}

    }

    @XmlType(namespace = "preferredHabitats")
    private static class MyXmlEntry {
	@XmlAttribute
	public final BehaviorMode key;

	@XmlValue
	public final HabitatSet value;

	@SuppressWarnings("unused") // needed by JAXB
	public MyXmlEntry() {
	    key = null;
	    value = null;
	}

	public MyXmlEntry(Map.Entry<BehaviorMode, HabitatSet> e) {
	    key = e.getKey();
	    value = e.getValue();
	}
    }

}

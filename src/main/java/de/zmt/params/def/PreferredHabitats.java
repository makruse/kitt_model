package de.zmt.params.def;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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
class PreferredHabitats implements ProvidesInspector, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Set<Habitat> DEFAULT_RESTING_HABITATS = EnumSet.of(Habitat.CORALREEF);
    private static final Set<Habitat> DEFAULT_FORAGING_HABITATS = EnumSet.of(Habitat.CORALREEF, Habitat.SEAGRASS);

    private final Map<BehaviorMode, Set<Habitat>> map = new EnumMap<>(BehaviorMode.class);

    public PreferredHabitats() {
	map.put(BehaviorMode.FORAGING, EnumSet.copyOf(DEFAULT_FORAGING_HABITATS));
	map.put(BehaviorMode.RESTING, EnumSet.copyOf(DEFAULT_RESTING_HABITATS));
    }

    public Set<Habitat> get(BehaviorMode key) {
	return map.get(key);
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
     * Wraps habitat sets into
     * 
     * @author mey
     *
     */
    private class MyProperties extends CollectionProperties {
	private static final long serialVersionUID = 1L;

	public MyProperties() {
	    super(map);
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

}

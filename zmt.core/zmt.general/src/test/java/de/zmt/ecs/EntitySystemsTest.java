package de.zmt.ecs;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.*;

import org.junit.*;
import org.junit.rules.ExpectedException;

public class EntitySystemsTest {
    private static final EntitySystem INDEPENDENT = new IndependentSystem();
    private static final EntitySystem DEPENDENT = new DependentSystem();

    private EntitySystems systems;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
	systems = new EntitySystems();
    }

    @Test
    public void addAndRemove() {
	assertThat(systems.getOrder(), is(empty()));
	systems.add(INDEPENDENT);
	assertThat(systems.getOrder(), contains(INDEPENDENT));
	systems.add(DEPENDENT);
	assertThat(systems.getOrder(), contains(INDEPENDENT, DEPENDENT));

    }

    @Test
    public void remove() {
	systems.add(INDEPENDENT);
	systems.add(DEPENDENT);

	systems.remove(DEPENDENT);
	assertThat(systems.getOrder(), contains(INDEPENDENT));
    }

    @Test
    public void getOrderOnMissingDependency() {
	systems.add(DEPENDENT);
	thrown.expect(IllegalStateException.class);
	systems.getOrder();
    }

    @Test
    public void clear() {
	systems.add(DEPENDENT);
	systems.clear();
	assertThat(systems.getOrder(), is(empty()));

	systems.add(INDEPENDENT);
	assertThat(systems.getOrder(), contains(INDEPENDENT));
    }

    private static class IndependentSystem extends AbstractSystem {

	@Override
	public Collection<Class<? extends EntitySystem>> getDependencies() {
	    return Collections.emptySet();
	}

	@Override
	protected void systemUpdate(Entity entity) {
	}

	@Override
	protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	    return Collections.emptySet();
	}

    }

    private static class DependentSystem extends AbstractSystem {

	@Override
	public Collection<Class<? extends EntitySystem>> getDependencies() {
	    return Collections.<Class<? extends EntitySystem>> singleton(IndependentSystem.class);
	}

	@Override
	protected void systemUpdate(Entity entity) {
	}

	@Override
	protected Collection<Class<? extends Component>> getRequiredComponentTypes() {
	    return Collections.emptySet();
	}

    }
}

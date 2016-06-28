package de.zmt.ecs;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class EntityManagerTest {
    private static final String ENTITY_NAME = "name";

    private EntityManager manager;
    private UUID entity;

    @Before
    public void setUp() throws Exception {
        manager = new EntityManager();
        entity = manager.createEntity();
    }

    @Test
    public void entities() {
        UUID createdEntity = manager.createEntity(ENTITY_NAME);
        assertThat(manager.nameFor(createdEntity), is(ENTITY_NAME));
        manager.removeEntity(createdEntity);
        assertThat(manager.nameFor(createdEntity), nullValue());
    }

    @Test
    public void components() {
        assertThat(manager.hasComponent(entity, TestComponent.class), is(false));
        TestComponent component = new TestComponent();

        manager.addComponent(entity, component);
        assertThat(manager.hasComponent(entity, TestComponent.class), is(true));
        assertThat(manager.getAllComponentsOnEntity(entity), hasItem((Component) component));
        assertThat(manager.getAllEntitiesPossessingComponent(TestComponent.class), hasItem(entity));
        assertThat(manager.getAllComponentsOfType(TestComponent.class), hasItem(component));

        manager.removeComponent(entity, component);
        assertThat(manager.hasComponent(entity, TestComponent.class), is(false));
    }

    @Test
    public void updateEntity() {
        EntitySystem systemA = mock(TestSystemA.class);
        EntitySystem systemB = mock(TestSystemB.class);
        // make system B depend on A
        when(systemA.getDependencies())
                .thenReturn(Collections.<Class<? extends EntitySystem>> singleton(systemB.getClass()));

        manager.addSystem(systemA);
        manager.addSystem(systemB);
        Entity wrappedEntity = new Entity(manager, entity);
        manager.updateEntity(wrappedEntity);

        InOrder inOrder = inOrder(systemA, systemB);
        inOrder.verify(systemB).update(wrappedEntity);
        inOrder.verify(systemA).update(wrappedEntity);
    }

    private static class TestComponent implements Component {
        private static final long serialVersionUID = 1L;
    }

    private static interface TestSystemA extends EntitySystem {
    }

    private static interface TestSystemB extends EntitySystem {
    }
}

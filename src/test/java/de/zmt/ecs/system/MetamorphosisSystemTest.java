package de.zmt.ecs.system;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.component.Metamorphic;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.params.SpeciesDefinition;
import ec.util.MersenneTwisterFast;
import sim.engine.Schedule;

public class MetamorphosisSystemTest {
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();
    private static final SpeciesDefinition SPECIES_DEFINITION = new SpeciesDefinition();

    private EntityManager manager;
    private Entity larva;
    private Entity environment;

    @Before
    public void setUp() throws Exception {
        manager = new EntityManager();
        larva = new Entity(manager,
                Arrays.asList(new Metamorphic(Schedule.BEFORE_SIMULATION), SPECIES_DEFINITION));
        environment = new Entity(manager);
    }

    @Test
    public void metamorphose() {
        KittEntityCreationHandler mockCreationHandler = mock(KittEntityCreationHandler.class);
        MetamorphosisSystem.metamorphose(larva, mockCreationHandler, environment, RANDOM);

        assertThat(larva.isAlive(), is(false));
        verify(mockCreationHandler).createFish(SPECIES_DEFINITION, environment,
                SPECIES_DEFINITION.getPostSettlementAge(), RANDOM);
    }
}

package de.zmt.ecs.system.agent;

import static javax.measure.unit.SI.SECOND;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import de.zmt.ecs.Entity;
import de.zmt.ecs.EntityManager;
import de.zmt.ecs.factory.KittEntityCreationHandler;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import ec.util.MersenneTwisterFast;

public class ReproductionSystemTest {
    private static final int MAX_AGENT_COUNT = 3;
    private static final int NUM_OFFSPRING = 5;
    private static final Amount<Duration> STEP_DURATION = AmountUtil.one(SECOND);
    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    private SpeciesDefinition speciesDefinition;
    private EntityManager manager;

    @Before
    public void setUp() throws Exception {
        speciesDefinition = new SpeciesDefinition();
        manager = new EntityManager();
    }

    @Test
    public void reproduceOnMaxCount() {
        KittEntityCreationHandler mockCreationHandler = mock(KittEntityCreationHandler.class);
        
        when(mockCreationHandler.getManager()).thenReturn(manager);
        // add entity to manager when larva creation is called
        doAnswer(invocation -> new Entity(manager, Collections.singleton(speciesDefinition))).when(mockCreationHandler)
                .createLarva(speciesDefinition, STEP_DURATION, RANDOM);
        // mock that returns ints up to NUM_OFFSPRING
        ((SpeciesDefinition.MyPropertiesProxy) speciesDefinition.propertiesProxy()).setNumOffspring(NUM_OFFSPRING);

        ReproductionSystem.reproduce(speciesDefinition, MAX_AGENT_COUNT, STEP_DURATION, mockCreationHandler,
                RANDOM);
        // verify creation happened without exceeding maximum
        verify(mockCreationHandler, times(MAX_AGENT_COUNT)).createLarva(speciesDefinition, STEP_DURATION, RANDOM);
    }

}

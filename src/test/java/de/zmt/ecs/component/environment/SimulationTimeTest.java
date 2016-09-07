package de.zmt.ecs.component.environment;

import static javax.measure.unit.SI.SECOND;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

public class SimulationTimeTest {
    private static final LocalDateTime MIDNIGHT = LocalDateTime.of(2000, 1, 1, 0, 0);
    
    private SimulationTime simulationTime;

    @Before
    public void setUp() throws Exception {
        simulationTime = new SimulationTime(MIDNIGHT, Duration.ofSeconds(1));
    }

    @Test
    public void isFirstStepInDay() {
        simulationTime.addStep();
        assertThat(simulationTime.isFirstStepInDay(Amount.valueOf(2, SECOND)), is(true));
        simulationTime.addStep();
        assertThat(simulationTime.isFirstStepInDay(Amount.valueOf(1, SECOND)), is(false));
    }

}

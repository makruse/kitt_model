package de.zmt.ecs.system.agent.move;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static sim.util.DirectionConstants.EAST;
import static sim.util.DirectionConstants.NEUTRAL;
import static sim.util.DirectionConstants.SOUTH;
import static sim.util.DirectionConstants.SOUTHWEST;

import org.junit.Before;
import org.junit.Test;

import ec.util.MersenneTwisterFast;
import sim.util.Rotation2D;

public class DesiredDirectionMovementTest {
    /** Mock RNG that will always return zero. */
    private static final MersenneTwisterFast MOCK_RANDOM = mock(MersenneTwisterFast.class);
    private static final Rotation2D MAX_ROTATION = Rotation2D.QUARTER;

    @Before
    public void setUp() throws Exception {
    }

    /** From neutral to neutral: expect random direction. */
    @Test
    public void computeDirectionFromNeutralToNeutral() {
        assertThat(DesiredDirectionMovement.computeDirection(NEUTRAL, NEUTRAL, MAX_ROTATION, MOCK_RANDOM), is(EAST));
    }

    /** From neutral to other: expect other direction. */
    @Test
    public void computeDirectionFromNeutralToOther() {
        assertThat(DesiredDirectionMovement.computeDirection(NEUTRAL, SOUTH, MAX_ROTATION, MOCK_RANDOM), is(SOUTH));
    }

    /** From and to are equal: expect the same direction. */
    @Test
    public void computeDirectionSame() {
        assertThat(DesiredDirectionMovement.computeDirection(SOUTH, SOUTH, MAX_ROTATION, MOCK_RANDOM), is(SOUTH));
    }

    /** To is within max turn range: expect to. */
    @Test
    public void computeDirectionWithinRange() {
        assertThat(DesiredDirectionMovement.computeDirection(EAST, SOUTH, MAX_ROTATION, MOCK_RANDOM), is(SOUTH));
    }

    /**
     * To exceeds max turn range: expect max turn range added to from towards
     * to.
     */
    @Test
    public void computeDirectionExceedsRange() {
        assertThat(DesiredDirectionMovement.computeDirection(EAST, SOUTHWEST, MAX_ROTATION, MOCK_RANDOM), is(SOUTH));
    }

}

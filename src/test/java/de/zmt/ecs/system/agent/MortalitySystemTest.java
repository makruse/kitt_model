package de.zmt.ecs.system.agent;

import static javax.measure.unit.NonSI.DAY;
import static javax.measure.unit.NonSI.HOUR;
import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.junit.Assert.assertThat;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;

public class MortalitySystemTest {
    private static final Amount<Frequency> MORTALITY_RISK = Amount.valueOf(0.3, UnitConstants.PER_DAY);
    private static final long HOURS_PER_DAY = AmountUtil.one(DAY).longValue(HOUR);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void convertMortalityRisk() {
        double riskPerHour = MortalitySystem.convertMortalityRisk(MORTALITY_RISK, AmountUtil.one(HOUR));
        double chancePerHour = 1 - riskPerHour;
        // chance of survival per day when tested every hour
        double chancePerDay = Math.pow(chancePerHour, HOURS_PER_DAY);
        double riskPerDay = 1 - chancePerDay;
        assertThat(Amount.valueOf(riskPerDay, UnitConstants.PER_DAY), amountCloseTo(MORTALITY_RISK));
    }

}

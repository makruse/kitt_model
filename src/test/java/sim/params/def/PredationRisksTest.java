package sim.params.def;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;
import org.junit.*;

import de.zmt.util.*;

public class PredationRisksTest {
    private PredationRisks predationRisks;

    @Before
    public void setUp() throws Exception {
	predationRisks = new PredationRisks();
    }

    @Test
    public void getMaxPredationRisk() {
	// set highest to zero
	Habitat maxPredationRiskHabitat = predationRisks.getMaxPredationRiskHabitat();
	predationRisks.put(maxPredationRiskHabitat, AmountUtil.zero(Frequency.UNIT));
	assertThat(maxPredationRiskHabitat, not(predationRisks.getMaxPredationRiskHabitat()));

	// set zero higher than highest
	predationRisks.put(maxPredationRiskHabitat,
		predationRisks.getMaxPredationRisk().plus(Amount.valueOf(1, UnitConstants.PER_YEAR)));
	assertThat(maxPredationRiskHabitat, is(predationRisks.getMaxPredationRiskHabitat()));
    }

}

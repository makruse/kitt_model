package sim.params.def;

import static org.hamcrest.AmountCloseTo.amountCloseTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;
import org.junit.Before;
import org.junit.Test;

import de.zmt.util.AmountUtil;
import de.zmt.util.Habitat;
import de.zmt.util.UnitConstants;

public class PredationRisksTest {
    private static final Amount<Frequency> INCREMENT = Amount.valueOf(1, UnitConstants.PER_YEAR);

    private PredationRisks predationRisks;

    @Before
    public void setUp() throws Exception {
	predationRisks = new PredationRisks(Amount.valueOf(1, UnitConstants.PER_DAY));
    }

    @Test
    public void updateBounds() {
	// set higher than max
	Amount<Frequency> incrementedMax = predationRisks.getMaxPredationRisk().plus(INCREMENT);
	predationRisks.put(Habitat.DEFAULT, incrementedMax);
	assertThat(predationRisks.getMaxPredationRisk(), is(incrementedMax));
	
	// set lower than min
	Amount<Frequency> zeroRisk = AmountUtil.zero(predationRisks.getMinPredationRisk());
	predationRisks.put(Habitat.DEFAULT, zeroRisk);
	assertThat(predationRisks.getMinPredationRisk(), is(amountCloseTo(zeroRisk)));
    }

}

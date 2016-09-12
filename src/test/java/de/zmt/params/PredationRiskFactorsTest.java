package de.zmt.params;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import de.zmt.util.Habitat;

public class PredationRiskFactorsTest {
    private static final double DEFAULT_RISK_FACTOR = 0.5;
    private static final double INCREMENT = 0.1;

    private PredationRiskFactors predationRiskFactors;

    @Before
    public void setUp() throws Exception {
        predationRiskFactors = new PredationRiskFactors();
        Arrays.stream(Habitat.values()).filter(habitat -> habitat.isAccessible())
                .forEach(habitat -> predationRiskFactors.put(habitat, DEFAULT_RISK_FACTOR));
    }

    @Test
    public void updateBounds() {
        double incrementedMax = predationRiskFactors.getMaxRiskFactor() + INCREMENT;
        // set higher than max
        predationRiskFactors.put(Habitat.DEFAULT, incrementedMax);
        assertThat(predationRiskFactors.getMaxRiskFactor(), is(incrementedMax));

        double decrementedMin = predationRiskFactors.getMinRiskFactor() - INCREMENT;
        // set lower than min
        predationRiskFactors.put(Habitat.DEFAULT, decrementedMin);
        assertThat(predationRiskFactors.getMinRiskFactor(), is(decrementedMin));
    }

}

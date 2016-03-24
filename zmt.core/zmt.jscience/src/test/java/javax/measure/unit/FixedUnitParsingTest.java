package javax.measure.unit;

import static javax.measure.unit.SI.HERTZ;
import static org.junit.Assert.fail;

import java.util.logging.Logger;

import org.jscience.physics.amount.Amount;
import org.jscience.physics.amount.AmountFormat;
import org.junit.BeforeClass;
import org.junit.Test;

import de.zmt.util.AmountUtil;

public class FixedUnitParsingTest {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(FixedUnitParsingTest.class.getName());

    @BeforeClass
    public static void setUpBeforeClass() {
	AmountFormat.setInstance(AmountUtil.FORMAT);
    }

    @Test
    public void test() {
	Amount<?> amount = Amount.valueOf(50.3, HERTZ);
	logger.info("Unit without divider working in unfixed jscience: " + amount);
	try {
	    amount = Amount.valueOf("30.5 1/month");
	} catch (IllegalArgumentException e) {
	    fail("Parsing failed: " + e.getMessage());
	}
	logger.info("Unit with divider only working in fixed jscience: " + amount);
    }

}

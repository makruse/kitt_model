package test;

import static javax.measure.unit.SI.HERTZ;
import static org.junit.Assert.fail;

import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;
import org.junit.Test;

public class TestFixedUnitParsing {
    @Test
    public void test() {
	Amount<?> amount = Amount.valueOf(50.3, HERTZ);
	System.out.println(amount);
	try {
	    amount = Amount.valueOf(30.5, Unit.valueOf("1/month"));
	} catch (IllegalArgumentException e) {
	    fail("Parsing failed: " + e.getMessage());
	}
	System.out.println(amount);
    }

}

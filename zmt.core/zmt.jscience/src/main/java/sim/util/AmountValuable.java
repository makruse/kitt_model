package sim.util;

import org.jscience.physics.amount.Amount;

/**
 * Simple adapter class wrapping around an {@link Amount} to be used as a
 * {@link Valuable}.
 * 
 * @author mey
 */
public class AmountValuable implements Valuable {
    private final Amount<?> amount;

    private AmountValuable(Amount<?> amount) {
	super();
	this.amount = amount;
    }

    @Override
    public double doubleValue() {
	return amount.getEstimatedValue();
    }
    
    @Override
    public String toString() {
	return amount.toString();
    }

    /**
     * Returns {@code amount} wrapped into a {@link Valuable} or
     * <code>null</code> if {@code amount} is null.
     * 
     * @param amount
     * @return wrapped {@code amount}
     */
    public static Valuable wrap(Amount<?> amount) {
	if (amount != null) {
	    return new AmountValuable(amount);
	}
	return null;
    }
}

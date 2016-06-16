package de.zmt.ecs.component.agent;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
import sim.util.Proxiable;
import sim.util.Valuable;

/**
 * Grants a simulation object the ability to age.
 * 
 * @author mey
 *
 */
public class Aging implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Age {@link Duration} of the fish. */
    private Amount<Duration> age;
    /** Maximum lifetime of the agent. */
    private final Amount<Duration> maxAge;

    public Aging(Amount<Duration> initialAge, Amount<Duration> maxAge) {
	this.age = initialAge;
	this.maxAge = maxAge;
    }

    /**
     * Become older by given amount
     * 
     * @param delta
     *            amount to age
     * @return new age with given amount added
     */
    public Amount<Duration> addAge(Amount<Duration> delta) {
	age = age.plus(delta);
	return age;
    }

    public Amount<Duration> getAge() {
	return age;
    }

    public Amount<Duration> getMaxAge() {
	return maxAge;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [age=" + age + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Valuable getAge() {
	    return AmountValuable.wrap(age.to(UnitConstants.AGE_GUI));
	}

	public Valuable getMaxAge() {
	    return AmountValuable.wrap(maxAge.to(UnitConstants.AGE_GUI));
	}

	@Override
	public String toString() {
	    return Aging.this.getClass().getSimpleName();
	}
    }
}

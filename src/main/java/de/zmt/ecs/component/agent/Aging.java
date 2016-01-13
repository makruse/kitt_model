package de.zmt.ecs.component.agent;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.*;
import sim.util.*;

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

    public Aging(Amount<Duration> initialAge) {
	this.age = initialAge;
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
	    return ValuableAmountAdapter.wrap(age.to(UnitConstants.AGE_GUI));
	}
    }
}

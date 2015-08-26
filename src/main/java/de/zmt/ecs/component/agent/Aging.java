package de.zmt.ecs.component.agent;

import static javax.measure.unit.NonSI.DAY;

import javax.measure.quantity.Duration;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import sim.util.Proxiable;

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
	return "Aging [age=" + age + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public double getAge_day() {
	    return getAge().to(DAY).getEstimatedValue();
	}
    }
}

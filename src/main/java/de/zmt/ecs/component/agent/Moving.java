package de.zmt.ecs.component.agent;

import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import de.zmt.util.ValuableAmountAdapter;
import sim.util.Double2D;
import sim.util.Proxiable;
import sim.util.Valuable;

public class Moving implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    private Double2D position;

    /** velocity vector of agent (m/s) */
    private Double2D velocity = new Double2D();

    /** length of {@link #velocity} */
    private Amount<Velocity> speed = AmountUtil.zero(UnitConstants.VELOCITY);

    public Moving(Double2D value) {
	this.position = value;
    }

    public Double2D getPosition() {
	return position;
    }

    public void setPosition(Double2D position) {
	this.position = position;
    }

    public Double2D getVelocity() {
	return velocity;
    }

    public void setVelocity(Double2D velocity) {
	this.velocity = velocity;
	this.speed = Amount.valueOf(velocity.length(), UnitConstants.VELOCITY);
    }

    public Amount<Velocity> getSpeed() {
	return speed;
    }

    @Override
    public String toString() {
	return getClass().getSimpleName() + " [position=" + getPosition() + ", velocity=" + velocity + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Double2D getPosition() {
	    return position;
	}

	public Double2D getVelocity() {
	    return velocity;
	}

	public String nameVelocity() {
	    return "Velocity_" + UnitConstants.VELOCITY;
	}

	public Valuable getSpeed() {
	    return ValuableAmountAdapter.wrap(speed);
	}

	@Override
	public String toString() {
	    return Moving.this.getClass().getSimpleName();
	}
    }
}

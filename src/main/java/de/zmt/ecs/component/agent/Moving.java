package de.zmt.ecs.component.agent;

import sim.util.*;
import de.zmt.ecs.Component;
import de.zmt.util.UnitConstants;

public class Moving implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    private Double2D position;

    /** velocity vector of agent (m/s) */
    private Double2D velocity = new Double2D();

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
    }

    @Override
    public String toString() {
	return "Moving [position=" + getPosition() + ", velocity=" + velocity
		+ "]";
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

	public double getSpeed() {
	    return getVelocity().length();
	}

	public String nameSpeed() {
	    return "Speed_" + UnitConstants.VELOCITY;
	}
    }
}

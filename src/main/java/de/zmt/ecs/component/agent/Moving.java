package de.zmt.ecs.component.agent;

import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
import sim.util.Double2D;
import sim.util.Proxiable;
import sim.util.Valuable;

public class Moving implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** position of agent (m) */
    private Double2D position;
    /** the direction the agent moves towards (unit vector) */
    private Double2D direction = new Double2D();
    /** speed of the agent (m/s) */
    private Amount<Velocity> speed = AmountUtil.zero(UnitConstants.VELOCITY);

    /**
     * Constructs a new {@link Moving} component with given initial position.
     * 
     * @param position
     *            the initial position
     */
    public Moving(Double2D position) {
        this.position = position;
    }

    /**
     * Constructs a new {@link Moving} component with given initial position and
     * direction.
     * 
     * @param position
     *            the initial position
     * @param direction
     *            the initial direction
     */
    public Moving(Double2D position, Double2D direction) {
        this(position);
        this.direction = direction;
    }

    public Double2D getPosition() {
        return position;
    }

    public void setPosition(Double2D position) {
        this.position = position;
    }

    public Double2D getDirection() {
        return direction;
    }

    public void setVelocity(Double2D direction, double speed) {
        this.direction = direction;
        this.speed = Amount.valueOf(speed, UnitConstants.VELOCITY);
    }

    public Amount<Velocity> getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[position=" + position + ", direction=" + direction + ", speed=" + speed
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

        public Double2D getDirection() {
            return direction;
        }

        public Valuable getSpeed() {
            return AmountValuable.wrap(speed);
        }

        @Override
        public String toString() {
            return Moving.this.getClass().getSimpleName();
        }
    }
}

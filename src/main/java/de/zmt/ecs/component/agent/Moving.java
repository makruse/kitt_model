package de.zmt.ecs.component.agent;

import javax.measure.quantity.Velocity;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.environment.WorldToMapConverter;
import de.zmt.util.AmountUtil;
import de.zmt.util.UnitConstants;
import sim.util.AmountValuable;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Proxiable;
import sim.util.Valuable;

public class Moving implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Continuous world position (m). */
    private Double2D worldPosition = new Double2D();
    /** Discrete position on map grid. */
    private Int2D mapPosition = new Int2D();
    /** the direction the agent moves towards (unit vector) */
    private Double2D direction = new Double2D();
    /** speed of the agent (m/s) */
    private Amount<Velocity> speed = AmountUtil.zero(UnitConstants.VELOCITY);

    /**
     * Constructs a new {@link Moving} component with fields set to neutral
     * values.
     */
    public Moving() {

    }

    /**
     * Returns the continuous world position (m).
     * 
     * @return the continuous world position (m)
     */
    public Double2D getWorldPosition() {
        return worldPosition;
    }

    /**
     * Returns the discrete position on map grid.
     * 
     * @return the discrete position on map grid
     */
    public Int2D getMapPosition() {
        return mapPosition;
    }

    /**
     * Sets the continuous world position (m). The map position is set
     * accordingly using the given converter.
     * 
     * @see #getMapPosition()
     * @param worldPosition
     *            the continuous world position to be set
     * @param converter
     *            the converter to obtain the related map position
     */
    public void setPosition(Double2D worldPosition, WorldToMapConverter converter) {
        setPosition(worldPosition, converter.worldToMap(worldPosition));
    }

    /**
     * Sets position. The caller needs to make sure world and map positions are
     * correctly related.
     * 
     * @see WorldToMapConverter#worldToMap(Double2D)
     * @param worldPosition
     *            the continuous world position to be set
     * @param mapPosition
     *            the discrete map position to be set
     */
    public void setPosition(Double2D worldPosition, Int2D mapPosition) {
        this.worldPosition = worldPosition;
        this.mapPosition = mapPosition;
    }

    /**
     * Returns the direction the agent moves towards (unit vector).
     * 
     * @return the direction the agent moves towards (unit vector)
     */
    public Double2D getDirection() {
        return direction;
    }

    /**
     * Sets velocity from given direction and speed.
     * 
     * @param direction
     *            the direction the agent moves towards (unit vector)
     * @param speed
     *            the speed of the agent (m/s)
     */
    public void setVelocity(Double2D direction, double speed) {
        this.direction = direction;
        this.speed = Amount.valueOf(speed, UnitConstants.VELOCITY);
    }

    /**
     * Returns the speed of the agent (m/s).
     * 
     * @return the speed of the agent (m/s)
     */
    public Amount<Velocity> getSpeed() {
        return speed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[position=" + worldPosition + ", direction=" + direction + ", speed="
                + speed + "]";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
        public Double2D getPosition() {
            return worldPosition;
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

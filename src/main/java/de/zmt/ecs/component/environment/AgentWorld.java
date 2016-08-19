package de.zmt.ecs.component.environment;

import java.io.Serializable;

import de.zmt.ecs.Component;
import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.util.UnitConstants;
import sim.field.continuous.Continuous2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;
import sim.util.Double2D;
import sim.util.Proxiable;

/**
 * Handles agents locations in continuous world space, backed by a
 * {@link Continuous2D} field. Space is continuous and managed in world units.
 * 
 * @see UnitConstants#WORLD_DISTANCE
 * @see UnitConstants#WORLD_AREA
 * @author mey
 *
 */
public class AgentWorld implements Component, Proxiable, ProvidesPortrayable<FieldPortrayable<Continuous2D>> {
    private static final long serialVersionUID = 1L;

    /**
     * Discretization constant for field.
     * 
     * @see Continuous2D
     */
    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of agents */
    private final Continuous2D agentField;
    public AgentWorld(double width, double height) {
        this.agentField = new Continuous2D(FIELD_DISCRETIZATION, width, height);
    }

    /**
     * Schedules agent, sets stoppable, adds to field and increment count.
     * 
     * @param agent
     */
    public void addAgent(Entity agent) {
        Moving moving = agent.get(Moving.class);
        if (moving == null) {
            throw new IllegalArgumentException("Entities added to an AgentWorld must have a "
                    + Moving.class.getSimpleName() + " component to obtain their position.");
        }

        agentField.setObjectLocation(agent, moving.getWorldPosition());
    }

    /**
     * Removes agent from field and decrement its species count.
     * 
     * @param agent
     */
    public void removeAgent(Entity agent) {
        agentField.remove(agent);
    }

    /**
     * Set agent position within the field.
     * 
     * @param agent
     * @param position
     */
    public void setAgentPosition(Entity agent, Double2D position) {
        agentField.setObjectLocation(agent, position);
    }

    /**
     * @return field width in meters
     */
    public double getWidth() {
        return agentField.getWidth();
    }

    /**
     * @return field height in meters
     */
    public double getHeight() {
        return agentField.getHeight();
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    @Override
    public FieldPortrayable<Continuous2D> providePortrayable() {
        return new FieldPortrayable<Continuous2D>() {

            @Override
            public Continuous2D getField() {
                return agentField;
            }
        };
    }

    public class MyPropertiesProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        public double getWidth() {
            return AgentWorld.this.getWidth();
        }

        public double getHeight() {
            return AgentWorld.this.getHeight();
        }

        @Override
        public String toString() {
            return AgentWorld.this.getClass().getSimpleName();
        }
    }
}

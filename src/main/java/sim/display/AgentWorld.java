package sim.display;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Moving;
import de.zmt.util.UnitConstants;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.continuous.Continuous2D;
import sim.portrayal.portrayable.FieldPortrayable;
import sim.portrayal.portrayable.ProvidesPortrayable;
import sim.util.Double2D;

/**
 * Handles agents locations in continuous world space, backed by a
 * {@link Continuous2D} field, to display them in the GUI. Space is continuous
 * and managed in world units.
 * 
 * @see UnitConstants#WORLD_DISTANCE
 * @see UnitConstants#WORLD_AREA
 * @author mey
 *
 */
class AgentWorld implements Steppable, Stoppable, ProvidesPortrayable<FieldPortrayable<Continuous2D>> {
    private static final long serialVersionUID = 1L;

    /**
     * Discretization constant for field.
     * 
     * @see Continuous2D
     */
    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of agents */
    private final Continuous2D agentField;

    private Stoppable stoppable;

    public AgentWorld(double width, double height) {
        this.agentField = new Continuous2D(FIELD_DISCRETIZATION, width, height);
    }

    /**
     * Adds agent to field
     * 
     * @param agent
     *            the agent {@link Entity} to add
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
     * Removes agent from field.
     * 
     * @param agent
     *            the agent {@link Entity} to remove
     */
    public void removeAgent(Entity agent) {
        agentField.remove(agent);
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

    public void setStoppable(Stoppable stoppable) {
        this.stoppable = stoppable;
    }

    /** Updates field location for every added agent. */
    @Override
    public void step(SimState state) {
        for (Object object : agentField.allObjects) {
            Double2D worldPosition = ((Entity) object).get(Moving.class).getWorldPosition();
            agentField.setObjectLocation(object, worldPosition);
        }

    }

    @Override
    public void stop() {
        stoppable.stop();
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + agentField.getAllObjects().size() + "]";
    }
}

package de.zmt.kitt.ecs.component.environment;

import java.io.Serializable;
import java.util.*;

import sim.field.continuous.Continuous2D;
import sim.util.*;
import de.zmt.kitt.ecs.component.agent.Moving;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.sim.engine.params.def.ParamDefinition;
import ecs.*;

public class AgentWorld implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    private static final double FIELD_DISCRETIZATION = 10;

    /** Stores locations of agents */
    private final Continuous2D agentField;
    private final MyPropertiesProxy proxy = new MyPropertiesProxy();

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
	    throw new IllegalArgumentException(
		    "Entities added to an AgentWorld must have a "
			    + Moving.class.getSimpleName()
			    + " component to obtain their position.");
	}

	agentField.setObjectLocation(agent, moving.getPosition());
	proxy.incrementAgentCount(agent.get(SpeciesDefinition.class), 1);
    }

    /**
     * Removes agent from field and decrement its count.
     * 
     * @param agent
     * @param identifier
     *            agents with the same identifier are totaled
     */
    public void removeAgent(Entity agent) {
	agentField.remove(agent);
	proxy.incrementAgentCount(agent.get(SpeciesDefinition.class), -1);
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

    /** @return field width in meters */
    public double getWidth() {
	return agentField.getWidth();
    }

    /** @return field height in meters */
    public double getHeight() {
	return agentField.getHeight();
    }

    public Bag getAgents() {
	return agentField.allObjects;
    }

    /**
     * Field object getter for portrayal in GUI.
     * 
     * @return agent field
     */
    public Continuous2D getFieldObject() {
	return agentField;
    }

    @Override
    public Object propertiesProxy() {
	return proxy;
    }

    public class MyPropertiesProxy implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Map<ParamDefinition, Integer> agentCounts = new HashMap<ParamDefinition, Integer>();

	private Integer incrementAgentCount(ParamDefinition definition,
		int increment) {
	    int count = agentCounts.containsKey(definition) ? agentCounts
		    .get(definition) : 0;
	    int incrementedCount = count + increment;

	    if (incrementedCount > 0) {
		agentCounts.put(definition, incrementedCount);
	    } else {
		// count is zero, remove group from map
		agentCounts.remove(definition);
	    }
	    return count;
	}

	public double getWidth() {
	    return AgentWorld.this.getWidth();
	}

	public double getHeight() {
	    return AgentWorld.this.getHeight();
	}

	public Map<ParamDefinition, Integer> getAgentCounts() {
	    return agentCounts;
	}
    }
}

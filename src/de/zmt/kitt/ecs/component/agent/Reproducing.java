package de.zmt.kitt.ecs.component.agent;

import java.util.*;

import sim.util.Proxiable;
import ec.util.MersenneTwisterFast;
import ecs.Component;

public class Reproducing implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Sex of the fish. Females can reproduce at adult age. */
    private final Sex sex;

    /** Fish life stage indicating its ability to reproduce. */
    private LifeStage lifeStage = LifeStage.JUVENILE;

    private CauseOfDeath causeOfDeath;

    public Reproducing(Sex sex) {
	this.sex = sex;
    }

    /**
     * Female fish are reproductive at adult age.
     * 
     * @return reproductive
     */
    public boolean isReproductive() {
	return lifeStage == LifeStage.MATURE && sex == Sex.FEMALE;
    }

    public LifeStage getLifeStage() {
	return lifeStage;
    }

    /** Make entity mature into an adult. */
    public void mature() {
	this.lifeStage = LifeStage.MATURE;
    }

    public void die(CauseOfDeath causeOfDeath) {
	this.lifeStage = LifeStage.DEAD;
	this.causeOfDeath = causeOfDeath;
    }

    @Override
    public String toString() {
	return "Reproducing [sex=" + sex + ", lifeStage=" + lifeStage + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Sex getSex() {
	    return sex;
	}

	public LifeStage getLifeStage() {
	    return lifeStage;
	}

	public CauseOfDeath getCauseOfDeath() {
	    return causeOfDeath;
	}
    }

    public static enum Sex {
	FEMALE, MALE, UNDEFINED
    }

    public static enum LifeStage {
	JUVENILE, MATURE, DEAD
    }

    public static enum CauseOfDeath {
	RANDOM, HABITAT, STARVATION;

	private static final Map<CauseOfDeath, String[]> DEATH_MESSAGES = new HashMap<>();
	/** Random number generator for death messages, can have its own. */
	private static final MersenneTwisterFast random = new MersenneTwisterFast();

	static {
	    String[] randomDeathMessages = new String[] {
		    " died from disease.",
		    " was ripped to shreds by a screw propeller.",
		    " ended up in a fisher's net." };
	    String[] habitatDeathMessages = new String[] {
		    " was torn apart by a predator.",
		    " found itself within the belly of another fish." };
	    String[] starvationDeathMessages = new String[] {
		    " starved to death.", " was too hungry to go on living." };

	    DEATH_MESSAGES.put(RANDOM, randomDeathMessages);
	    DEATH_MESSAGES.put(HABITAT, habitatDeathMessages);
	    DEATH_MESSAGES.put(STARVATION, starvationDeathMessages);
	}

	/**
	 * @return random death message for this cause of death
	 */
	public String getMessage() {
	    String[] messages = DEATH_MESSAGES.get(this);
	    return messages[random.nextInt(messages.length)] + " ("
		    + this.name() + ")";
	}
    }
}

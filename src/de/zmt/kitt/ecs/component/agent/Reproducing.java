package de.zmt.kitt.ecs.component.agent;

import java.util.*;

import sim.util.Proxiable;
import ec.util.MersenneTwisterFast;
import ecs.Component;

public class Reproducing implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Sex of the fish. Females can reproduce at adult age. */
    private Sex sex;

    /** Fish life stage indicating its ability to reproduce. */
    private Phase phase = Phase.JUVENILE;

    private CauseOfDeath causeOfDeath;

    public Reproducing(Sex sex) {
	this.sex = sex;
    }

    /**
     * Female fish are reproductive after reaching maturity.
     * 
     * @return reproductive
     */
    public boolean isReproductive() {
	return (phase == Phase.INITIAL || phase == Phase.TERMINAL)
		&& sex == Sex.FEMALE;
    }

    /** Enter next phase, change sex when going from initial to terminal. */
    public void enterNextPhase() {
	switch (phase) {
	case JUVENILE:
	    phase = Phase.INITIAL;
	    break;
	case INITIAL:
	    if (sex == Sex.FEMALE) {
		sex = Sex.MALE;
	    } else if (sex == Sex.MALE) {
		sex = Sex.FEMALE;
	    }
	    phase = Phase.TERMINAL;
	    break;
	default:
	    throw new IllegalStateException("Cannot enter next phase when "
		    + phase);
	}
    }

    public void die(CauseOfDeath causeOfDeath) {
	this.phase = Phase.DEAD;
	this.causeOfDeath = causeOfDeath;
    }

    public Phase getPhase() {
	return phase;
    }

    @Override
    public String toString() {
	return "Reproducing [sex=" + sex + ", phase=" + phase + "]";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public class MyPropertiesProxy {
	public Sex getSex() {
	    return sex;
	}

	public Phase getPhase() {
	    return phase;
	}

	public CauseOfDeath getCauseOfDeath() {
	    return causeOfDeath;
	}
    }

    public static enum Sex {
	FEMALE, MALE, HERMAPHRODITE
    }

    public static enum Phase {
	/** Before reaching maturity. */
	JUVENILE,
	/** Entered initial phase, reached maturity. */
	INITIAL,
	/** Entered terminal phase, changed sex. */
	TERMINAL, DEAD
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
		    " ended up within the belly of another fish." };
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

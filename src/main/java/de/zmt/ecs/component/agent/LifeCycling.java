package de.zmt.ecs.component.agent;

import java.util.HashMap;
import java.util.Map;

import de.zmt.ecs.Component;
import ec.util.MersenneTwisterFast;
import sim.params.def.SpeciesDefinition;
import sim.util.Proxiable;

/**
 * Component that models state for agents going through a life cycle, including
 * juvenile and reproductive phases.
 * 
 * @author mey
 *
 */
public class LifeCycling implements Component, Proxiable {
    private static final long serialVersionUID = 1L;

    /** Sex of the fish. Females can reproduce at adult age. */
    private Sex sex;

    /** Fish life stage indicating its ability to reproduce. */
    private Phase phase = Phase.JUVENILE;

    /**
     * Set when fish is no longer alive.
     * 
     * @see #die(CauseOfDeath)
     */
    private CauseOfDeath causeOfDeath = null;

    /**
     * Constructs a {@link LifeCycling} component with given sex and phase set
     * to {@link Phase#JUVENILE}.
     * 
     * @param sex
     */
    public LifeCycling(Sex sex) {
	super();
	this.sex = sex;
    }


    /**
     * Female fish are reproductive after reaching maturity.
     * 
     * @return reproductive
     */
    public boolean isReproductive() {
	return (phase == Phase.INITIAL || phase == Phase.TERMINAL) && sex == Sex.FEMALE;
    }

    /**
     * All agents enter the initial phase after being juvenile, but only those
     * that can change sex are able to enter the terminal phase later.
     * 
     * @see SpeciesDefinition#canChangeSex()
     * @param canChangeSex
     * @return true if phase can be changed
     */
    public boolean canChangePhase(boolean canChangeSex) {
	return phase == Phase.JUVENILE || (canChangeSex && phase == Phase.INITIAL);
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
	    throw new IllegalStateException("Cannot enter next phase when " + phase);
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
	return getClass().getSimpleName() + " [sex=" + sex + ", phase=" + phase + "]";
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

	@Override
	public String toString() {
	    return LifeCycling.this.getClass().getSimpleName();
	}
    }

    public static enum Sex {
	FEMALE, MALE
    }

    /**
     * Phases that species go through during their lifetime.
     * 
     * @author mey
     *
     */
    public static enum Phase {
	/** Before reaching maturity. */
	JUVENILE,
	/** Entered initial phase, reached maturity. */
	INITIAL,
	/**
	 * Entered terminal phase, changed sex. Gonochoristic agents will not
	 * enter this phase.
	 */
	TERMINAL, DEAD
    }

    public static enum CauseOfDeath {
	/** Diseases and all causes not covered by other values. */
	RANDOM,
	/** Predation and other causes related to habitat. */
	HABITAT, STARVATION, OLD_AGE;

	private static final Map<CauseOfDeath, String[]> DEATH_MESSAGES = new HashMap<>();
	/** Random number generator for death messages, can have its own. */
	private static final MersenneTwisterFast RANDOM_GENERATOR = new MersenneTwisterFast();

	static {
	    String[] randomDeathMessages = new String[] { " died from disease.",
		    " was ripped to shreds by a screw propeller.", " ended up in a fisher's net." };
	    String[] habitatDeathMessages = new String[] { " was torn apart by a predator.",
		    " ended up within the belly of another fish." };
	    String[] starvationDeathMessages = new String[] { " starved to death.",
		    " was too hungry to go on living." };
	    String[] oldAgeDeathMessages = new String[] { " is too old to live any longer." };

	    DEATH_MESSAGES.put(RANDOM, randomDeathMessages);
	    DEATH_MESSAGES.put(HABITAT, habitatDeathMessages);
	    DEATH_MESSAGES.put(STARVATION, starvationDeathMessages);
	    DEATH_MESSAGES.put(OLD_AGE, oldAgeDeathMessages);
	}

	/**
	 * @return random death message for this cause of death
	 */
	public String getMessage() {
	    String[] messages = DEATH_MESSAGES.get(this);
	    return messages[RANDOM_GENERATOR.nextInt(messages.length)] + " (" + this.name() + ")";
	}
    }
}

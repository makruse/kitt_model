package de.zmt.kitt.ecs.component.agent;

import sim.util.Proxiable;
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
	return lifeStage == LifeStage.ADULT && sex == Sex.FEMALE;
    }

    public LifeStage getLifeStage() {
	return lifeStage;
    }

    /** Make entity mature into an adult. */
    public void mature() {
	this.lifeStage = LifeStage.ADULT;
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
	FEMALE, MALE
    }

    public static enum LifeStage {
	JUVENILE, ADULT, DEAD
    }

    public static enum CauseOfDeath {
	RANDOM, HABITAT, STARVATION;

	public String getMessage() {
	    switch (this) {
	    case RANDOM:
		return " had bad luck and died from random mortality.";
	    case HABITAT:
		return " was torn apart by a predator and died from habitat mortality.";
	    case STARVATION:
		return " starved to death.";
	    }
	    return " died without specific reason.";
	}
    }
}

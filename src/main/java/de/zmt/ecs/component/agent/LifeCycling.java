package de.zmt.ecs.component.agent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.collect.ImmutableMap;

import de.zmt.ecs.Component;
import de.zmt.params.SpeciesDefinition;
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
    private CauseOfDeath causeOfDeath = CauseOfDeath.NONE;

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
     * Adult female fish need energy to grow ovaries.
     * 
     * @return <code>true</code> if female and in initial / terminal phase
     */
    public boolean isAdultFemale() {
        return phase == Phase.INITIAL && sex == Sex.FEMALE;
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
        this.causeOfDeath = causeOfDeath;
    }

    public Phase getPhase() {
        return phase;
    }

    public CauseOfDeath getCauseOfDeath() {
        return causeOfDeath;
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
        TERMINAL,
        /** Agent is dead. */
        DEAD;

        /**
         * @return the probability of an individual to be within this phase
         */
        public double getProbability() {
            switch (this) {
            case JUVENILE:
                return 0.6;
            case INITIAL:
                return 0.35;
            case TERMINAL:
                return 0.05;
            default:
                return 0;
            }
        }
    }

    public static enum CauseOfDeath {
        /** Diseases and all causes not covered by other values. */
        NATURAL,
        /** Predation and other causes related to habitat. */
        PREDATION, STARVATION, OLD_AGE,
        /**is not dead yet*/
        NONE;

        private static final Map<CauseOfDeath, String[]> DEATH_MESSAGES;

        static {
            Map<CauseOfDeath, String[]> map = new HashMap<>();
            String[] naturalDeathMessages = new String[] { " died from disease.",
                    " was ripped to shreds by a screw propeller.", " ended up in a fisher's net." };
            String[] predationDeathMessage = new String[] { " was torn apart by a predator.",
                    " ended up within the belly of another fish." };
            String[] starvationDeathMessages = new String[] { " starved to death.",
                    " was too hungry to go on living." };
            String[] oldAgeDeathMessages = new String[] { " is too old to live any longer." };
            String[] stillAlive = new String[] { "" };

            map.put(NATURAL, naturalDeathMessages);
            map.put(PREDATION, predationDeathMessage);
            map.put(STARVATION, starvationDeathMessages);
            map.put(OLD_AGE, oldAgeDeathMessages);
            map.put(NONE, stillAlive);

            DEATH_MESSAGES = ImmutableMap.copyOf(map);
        }

        /**
         * @return random death message for this cause of death
         */
        public String getMessage() {
            String[] messages = DEATH_MESSAGES.get(this);
            return messages[ThreadLocalRandom.current().nextInt(messages.length)] + " (" + this.name() + ")";
        }
    }

    public String getSex(){
        if(sex == Sex.FEMALE)
            return "Female";
        else
            return "Male";
    }
}

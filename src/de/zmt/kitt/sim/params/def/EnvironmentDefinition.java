package de.zmt.kitt.sim.params.def;

import static javax.measure.unit.NonSI.MINUTE;

import java.util.logging.Logger;

import javax.measure.quantity.*;
import javax.xml.bind.annotation.*;

import org.joda.time.Instant;
import org.jscience.physics.amount.Amount;

import sim.util.Proxiable;
import de.zmt.kitt.util.*;
import de.zmt.sim.engine.params.def.AbstractParameterDefinition;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnvironmentDefinition extends AbstractParameterDefinition
	implements Proxiable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(EnvironmentDefinition.class.getName());

    /** Time instant the simulation starts (1970-01-01 01:00) */
    public static final Instant START_INSTANT = new Instant(0);
    /** Simulation time passing every step, must be exact. */
    public static final Amount<Duration> STEP_DURATION = Amount.valueOf(10,
	    MINUTE);

    /** Duration of simulation in discrete time steps when running without GUI */
    private int simTime = 1000;
    /** random seed value */
    private long seed = 0;
    /** File name of habitat map image. Loaded from sub-folder resources */
    private String mapImageFilename = "CoralEyeHabitatMapGUI.png";
    /** Map scale: pixel per meter */
    private double mapScale = 1;
    /** Proportional increase of algae per time unit. */
    private Amount<Frequency> algalGrowthRate = Amount.valueOf(0.01,
	    UnitConstants.PER_DAY);
    /** Step interval for writing population data to file */
    private int outputPopulationInterval = 50;
    /** Step interval for writing age data to file */
    private int outputAgeInterval = 50;

    @Override
    public String getTitle() {
	return "Environment";
    }

    @Override
    public Object propertiesProxy() {
	return new MyPropertiesProxy();
    }

    public int getSimTime() {
	return simTime;
    }

    public long getSeed() {
	return seed;
    }

    public String getMapImageFilename() {
	return mapImageFilename;
    }

    public double getMapScale() {
	return mapScale;
    }

    public int getOutputPopulationInterval() {
	return outputPopulationInterval;
    }

    public int getOutputAgeInterval() {
	return outputAgeInterval;
    }

    public Amount<Frequency> getAlgalGrowthRate() {
	return algalGrowthRate;
    }

    public class MyPropertiesProxy {

	public int getSimTime() {
	    return simTime;
	}

	public void setSimTime(int simTime) {
	    EnvironmentDefinition.this.simTime = simTime;
	}

	public long getSeed() {
	    return seed;
	}

	public void setSeed(long seed) {
	    EnvironmentDefinition.this.seed = seed;
	}

	public String getMapImageFilename() {
	    return mapImageFilename;
	}

	public void setMapImageFilename(String mapImageFilename) {
	    EnvironmentDefinition.this.mapImageFilename = mapImageFilename;
	}

	public double getMapScale() {
	    return mapScale;
	}

	public void setMapScale(double mapScale) {
	    if (mapScale != 1) {
		logger.warning("Dynamic map scale not yet implemented.");
	    }
	    EnvironmentDefinition.this.mapScale = 1;
	}

	public int getOutputPopulationInterval() {
	    return outputPopulationInterval;
	}

	public void setOutputPopulationInterval(int outputPopulationInterval) {
	    if (outputPopulationInterval > 0) {
		EnvironmentDefinition.this.outputPopulationInterval = outputPopulationInterval;
	    }
	}

	public int getOutputAgeInterval() {
	    return outputAgeInterval;
	}

	public void setOutputAgeInterval(int outputAgeInterval) {
	    if (outputAgeInterval > 0) {
		EnvironmentDefinition.this.outputAgeInterval = outputAgeInterval;
	    }
	}

	public String getAlgalGrowthRate() {
	    return algalGrowthRate.toString();
	}

	public void setAlgalGrowthRate(String algalGrowthRateString) {
	    EnvironmentDefinition.this.algalGrowthRate = AmountUtil
		    .parseAmount(algalGrowthRateString, UnitConstants.PER_DAY);
	}
    }
}

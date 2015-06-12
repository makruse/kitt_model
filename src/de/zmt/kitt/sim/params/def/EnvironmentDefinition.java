package de.zmt.kitt.sim.params.def;

import static javax.measure.unit.NonSI.MINUTE;

import java.util.logging.Logger;

import javax.measure.quantity.*;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.joda.time.Instant;
import org.jscience.physics.amount.Amount;

import sim.util.*;
import de.zmt.ecs.Component;
import de.zmt.kitt.ecs.component.environment.FoodMap.DensityToMassConverter;
import de.zmt.kitt.ecs.component.environment.HabitatMap.WorldToMapConverter;
import de.zmt.kitt.util.UnitConstants;
import de.zmt.kitt.util.quantity.AreaDensity;
import de.zmt.sim.engine.params.def.AbstractParamDefinition;
import de.zmt.util.AmountUtil;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvironmentDefinition extends AbstractParamDefinition implements
	Proxiable, Component, DensityToMassConverter, WorldToMapConverter {
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
    private double simTime = 1000;
    /** random seed value */
    private long seed = 0;
    /** File name of habitat map image. Loaded from sub-folder resources */
    private String mapImageFilename = "CoralEyeHabitatMapGUI.png";
    /** Map scale: pixel per meter */
    // remove after implementing dynamically
    @XmlTransient
    private double mapScale = 1;
    /** @see #mapScale */
    @XmlTransient
    private double inverseMapScale = computeInverseMapScale();

    /** Area that spans over one pixel in map. */
    @XmlTransient
    private Amount<Area> pixelArea = computePixelArea();

    /** Proportional increase of algae per time unit. */
    // TODO get correct value
    /*
     * regrowth function: 9 mg algal dry weight per m2 and day<br>
     * 
     * @see "Adey & Goertemiller 1987", "Clifton 1995"
     */
    private Amount<Frequency> algalGrowthRate = Amount.valueOf(0.01,
	    UnitConstants.PER_DAY);
    /** Step interval for writing population data to file */
    private int outputPopulationInterval = 50;
    /** Step interval for writing age data to file */
    private int outputAgeInterval = 50;

    private double computeInverseMapScale() {
        return 1 / mapScale;
    }

    private Amount<Area> computePixelArea() {
        return Amount.valueOf(inverseMapScale
            * inverseMapScale, UnitConstants.MAP_AREA);
    }

    /** @see #mapScale */
    @Override
    public Double2D worldToMap(Double2D worldCoordinates) {
	return worldCoordinates.multiply(mapScale);
    }

    /**
     * Convert from map (pixel) to world coordinates.
     * 
     * @see #mapScale
     * @param mapCoordinates
     * @return world coordinates
     */
    public Double2D mapToWorld(Int2D mapCoordinates) {
	return new Double2D(mapCoordinates).multiply(inverseMapScale);
    }

    /** @see #pixelArea */
    @Override
    public Amount<Mass> densityToMass(Amount<AreaDensity> density) {
	return density.times(pixelArea).to(UnitConstants.FOOD);
    }

    public double getSimTime() {
	return simTime;
    }

    public long getSeed() {
	return seed;
    }

    public String getMapImageFilename() {
	return mapImageFilename;
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

    @Override
    public String getTitle() {
        return "Environment";
    }

    @Override
    public Object propertiesProxy() {
        return new MyPropertiesProxy();
    }

    @Override
    protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	super.afterUnmarshal(unmarshaller, parent);
	inverseMapScale = computeInverseMapScale();
	pixelArea = computePixelArea();
	System.out.println(pixelArea);
    }

    public class MyPropertiesProxy {

	public double getSimTime() {
	    return simTime;
	}

	public void setSimTime(double simTime) {
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
		return;
	    }
	    EnvironmentDefinition.this.mapScale = mapScale;
	    inverseMapScale = computeInverseMapScale();
	    pixelArea = computePixelArea();
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

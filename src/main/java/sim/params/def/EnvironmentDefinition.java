package sim.params.def;

import static javax.measure.unit.SI.SECOND;

import java.io.File;
import java.util.logging.Logger;

import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.environment.FoodMap.FindFoodConverter;
import de.zmt.ecs.component.environment.MapToWorldConverter;
import de.zmt.util.AmountUtil;
import de.zmt.util.FormulaUtil;
import de.zmt.util.UnitConstants;
import de.zmt.util.quantity.AreaDensity;
import sim.engine.params.def.AbstractParamDefinition;
import sim.util.Double2D;
import sim.util.Int2D;
import sim.util.Proxiable;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvironmentDefinition extends AbstractParamDefinition
	implements Proxiable, Component, FindFoodConverter, MapToWorldConverter {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(EnvironmentDefinition.class.getName());

    /** Time instant the simulation starts (2000-01-01 08:00) */
    public static final Instant START_INSTANT = new Instant(new DateTime(2000, 1, 1, 8, 0));
    /** Simulation time passing every step, must be exact. */
    public static final Amount<Duration> STEP_DURATION = Amount.valueOf(1, SECOND);
    /** Name of resources directory. Habitat map images are loaded from here. */
    public static final String RESOURCES_DIR = "resources" + File.separator;

    /** Seed value for random number generation. */
    private long seed = 0;
    /** File name of habitat map image. Loaded from {@link #RESOURCES_DIR}. */
    private String mapImageFilename = "CoralEyeHabitatMapGUI_300x450.png";
    /** Map scale: pixel per meter */
    // TODO remove transient annotation after implementing dynamically
    @XmlTransient
    private double mapScale = 1;
    /**
     * @see #mapScale
     */
    @XmlTransient
    private double inverseMapScale = computeInverseMapScale();

    /** World area that spans over one pixel or grid cell in map. */
    @XmlTransient
    private Amount<Area> pixelArea = computePixelArea();

    /**
     * Proportional increase of algae per time unit.
     * 
     * @see FormulaUtil#growAlgae(Amount, Amount, Amount, Amount)
     */
    // TODO get correct value
    private Amount<Frequency> algalGrowthRate = Amount.valueOf(0.01, UnitConstants.PER_DAY);
    /** Step interval for writing population data to file */
    private int outputPopulationInterval = 50;
    /** Step interval for writing age data to file */
    private int outputAgeInterval = 50;

    private double computeInverseMapScale() {
	return 1 / mapScale;
    }

    private Amount<Area> computePixelArea() {
	return Amount.valueOf(inverseMapScale * inverseMapScale, UnitConstants.WORLD_AREA);
    }

    /**
     * @see #mapScale
     */
    @Override
    public Int2D worldToMap(Double2D worldCoordinates) {
	Double2D mapCoordinates = worldCoordinates.multiply(mapScale);
	return new Int2D((int) mapCoordinates.x, (int) mapCoordinates.y);
    }

    /**
     * @see #mapScale
     */
    @Override
    public double worldToMap(Amount<Length> worldDistance) {
	return worldDistance.doubleValue(UnitConstants.WORLD_DISTANCE) * mapScale;
    }

    @Override
    public Double2D mapToWorld(Int2D mapCoordinates) {
	return new Double2D(mapCoordinates).multiply(inverseMapScale);
    }

    /**
     * @see #pixelArea
     */
    @Override
    public Amount<Mass> densityToMass(Amount<AreaDensity> density) {
	return density.times(pixelArea).to(UnitConstants.FOOD);
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
    }

    public class MyPropertiesProxy {
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
	    EnvironmentDefinition.this.algalGrowthRate = AmountUtil.parseAmount(algalGrowthRateString,
		    UnitConstants.PER_DAY);
	}
    }
}

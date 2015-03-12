package de.zmt.kitt.sim.params.def;

import static javax.measure.unit.NonSI.MINUTE;

import java.util.logging.Logger;

import javax.measure.quantity.Duration;
import javax.xml.bind.annotation.*;

import org.joda.time.Instant;
import org.jscience.physics.amount.Amount;

import de.zmt.sim.engine.params.def.AbstractParameterDefinition;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnvironmentDefinition extends AbstractParameterDefinition {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(EnvironmentDefinition.class.getName());

    /** Time instant the simulation starts (1970-01-01 01:00) */
    public static final Instant START_INSTANT = new Instant(0);
    /** Simulation time passing every step */
    public static final Amount<Duration> STEP_DURATION = Amount
	    .valueOf(10, MINUTE);

    /** Duration of simulation in discrete time steps when running without GUI */
    private int simTime = 1000;
    /** random seed value */
    private int seed = 0;
    /** File name of habitat map image. Loaded from sub-folder resources */
    private String mapImageFilename = "CoralEyeHabitatMapGUI.png";
    /** Map scale: pixel per meter */
    private double mapScale = 1;

    public int getSimTime() {
	return simTime;
    }

    public void setSimTime(int simTime) {
	this.simTime = simTime;
    }

    public int getSeed() {
	return seed;
    }

    public void setSeed(int seed) {
	this.seed = seed;
    }

    public String getMapImageFilename() {
	return mapImageFilename;
    }

    public void setMapImageFilename(String mapImageFilename) {
	this.mapImageFilename = mapImageFilename;
    }

    public double getMapScale() {
	return mapScale;
    }

    public void setMapScale(double mapScale) {
	if (mapScale != 1) {
	    logger.warning("Dynamic map scale not yet implemented.");
	}
	this.mapScale = 1;
    }

    @Override
    public String getTitle() {
	return "Environment";
    }
}

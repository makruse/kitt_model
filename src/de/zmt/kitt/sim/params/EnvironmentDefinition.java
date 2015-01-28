package de.zmt.kitt.sim.params;

import javax.xml.bind.annotation.*;

import de.zmt.sim_base.engine.params.ParameterDefinition;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnvironmentDefinition extends ParameterDefinition {
    /** Time scale: minutes per step */
    private int timeScale = 1;
    /** Duration of simulation in discrete timesteps */
    private int simTime = 1000;
    /** random seed value */
    private int seed = 0;
    /** File name of habitat map image. Loaded from sub-folder resources */
    private String mapImageFilename = "CoralEyeHabitatMapGUI.png";
    /** Map scale: pixel per meter */
    private double mapScale = 1;

    public int getTimeScale() {
	return timeScale;
    }

    public void setTimeScale(int minutesPerStep) {
	this.timeScale = minutesPerStep;
    }

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
	this.mapScale = mapScale;
    }

    @Override
    public String getTitle() {
        return "Environment";
    }
}

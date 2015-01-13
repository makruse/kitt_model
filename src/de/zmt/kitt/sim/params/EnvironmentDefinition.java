package de.zmt.kitt.sim.params;

import javax.xml.bind.annotation.*;

import de.zmt.sim_base.engine.params.ParameterDefinition;

/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvironmentDefinition extends ParameterDefinition {

    /** resolution of one step in minutes */
    public int timeResolutionMinutes = 10;
    /** Duration of simulation in discrete timesteps */
    public double simtime = 100;
    /** interval for graphic output */
    public double drawinterval = 1;
    /** random seed value */
    public int seed = 0;
    /** File name of habitat map image. Loaded from subfolder resources */
    public String mapImageFilename = "CoralEyeHabitatMapGUI.png";

    @Override
    public String getTitle() {
	return "Environment";
    }
}

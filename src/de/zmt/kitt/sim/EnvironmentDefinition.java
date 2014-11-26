package de.zmt.kitt.sim;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import flanagan.analysis.Regression;


/**
 * holds the initial common parameters for the environment.<br />
 * it is part of the Config class.<br />
 * By JAXB annotation @XmlAccessorType(XmlAccessType.FIELD) all fields<br />
 * are written to xml file.
 * 
 * @see de.zmt.itn#Config
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvironmentDefinition{
		
	/** resolution of timesteps in minutes */
	public int timeResolutionMinutes=10;
	/** Duration of simulation in discrete timesteps */
	public double simtime=100;
	/** interval for graphic output */
	public double drawinterval=1;
	/** random seed value */
	public int rst;
	/** size of field x */
	public double xMax=471;
	/** size of field x */
	public double yMax=708;
	
	/** cell resolution in cm */
	public double cellResolution=100;
		
	public double memCellsX=10;
	public double memCellsY=10;
	public int foodCellsX=80;
	public int foodCellsY=140;
	
	@XmlTransient
	static public Color bgColor= Color.black;
}

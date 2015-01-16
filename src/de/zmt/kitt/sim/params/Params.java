package de.zmt.kitt.sim.params;

import java.util.*;

import javax.xml.bind.annotation.*;

import de.zmt.sim_base.engine.params.ParamsBase;

/**
 * Config holds all parameters to initialize the model state.<br />
 * The parameters are read from and written to an Xml file <br />
 * with the help of JAXB lib. Due to JAXB annotations it is defined which
 * properties<br />
 * of the Configuration are written. <br />
 * XmlTransient marked properties are not serialized to xml.<br />
 * the names for the xml nodes are taken from the class-property name<br />
 * if not overridden by annotation.<br />
 * in this case the config file looks like this:<br />
 * 
 * <pre>
 * {@code
 * <ModelParams>
 *    <env>
 *        <simtime>2000.0</simtime>
 *        <drawinterval>1.0</drawinterval>
 *        ...
 *    </env>
 *    <prey>
 *        <initialNr>900</initialNr>
 *        <maxNr>1800</maxNr>        
 *        ...
 *    </prey>
 *    <pred>
 *        <initialNr>40</initialNr>
 *        <maxNr>100</maxNr>
 *        ...
 *    </pred>
 *    ..
 * </ModelParams>
 * }
 * </pre>
 */
@XmlRootElement(name = "params")
@XmlAccessorType(XmlAccessType.NONE)
public class Params extends ParamsBase {
    public static final String DEFAULT_FILENAME = "params.xml";

    @XmlElement
    public EnvironmentDefinition environmentDefinition = new EnvironmentDefinition();

    /**
     * list of species definition
     */
    @XmlElementWrapper(name = "speciesList")
    @XmlElement(name = "speciesDefinition")
    private final List<SpeciesDefinition> speciesDefs = new ArrayList<SpeciesDefinition>();

    public List<SpeciesDefinition> getSpeciesDefs() {
	return Collections.unmodifiableList(speciesDefs);
    }
}

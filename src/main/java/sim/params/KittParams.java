package sim.params;

import java.util.*;

import javax.xml.bind.annotation.*;

import sim.engine.params.SimParams;
import sim.engine.params.def.*;
import sim.params.def.*;

/**
 * Config holds all parameters to initialize the model state. The parameters are
 * read from and written to an Xml file with the help of JAXB lib. Due to JAXB
 * annotations it is defined which properties of the Configuration are written.
 * XmlTransient marked properties are not serialized to xml. the names for the
 * xml nodes are taken from the class-property name if not overridden by
 * annotation. in this case the config file looks like this:
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
@XmlRootElement(name = "kittParams", namespace = "http://www.zmt-bremen.de/")
@XmlAccessorType(XmlAccessType.NONE)
public class KittParams implements SimParams {
    private static final long serialVersionUID = 1L;

    @XmlElement
    private final EnvironmentDefinition environmentDefinition = new EnvironmentDefinition();

    /**
     * list of species definition
     */
    @XmlElementWrapper(name = "speciesDefinitions")
    @XmlElement(name = "definition")
    private final List<SpeciesDefinition> speciesDefs = new LinkedList<SpeciesDefinition>();

    public KittParams() {
	// default setup with one species
	speciesDefs.add(new SpeciesDefinition());
    }

    public EnvironmentDefinition getEnvironmentDefinition() {
	return environmentDefinition;
    }

    public Collection<SpeciesDefinition> getSpeciesDefs() {
	return Collections.unmodifiableCollection(speciesDefs);
    }

    /**
     * Add a new {@link SpeciesDefinition}.
     * 
     * @param def
     */
    public void addSpeciesDef(SpeciesDefinition def) {
	speciesDefs.add(def);
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
	List<ParamDefinition> defs = new ArrayList<ParamDefinition>(speciesDefs);
	defs.add(environmentDefinition);
	return Collections.unmodifiableCollection(defs);
    }

    @Override
    public boolean removeOptionalDefinition(OptionalParamDefinition optionalDef) {
	return speciesDefs.remove(optionalDef);
    }

    @Override
    public long getSeed() {
	return environmentDefinition.getSeed();
    }

}

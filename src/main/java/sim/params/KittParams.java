package sim.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import sim.engine.params.BaseParams;
import sim.engine.params.SimParams;
import sim.engine.params.def.OptionalParamDefinition;
import sim.engine.params.def.ParamDefinition;
import sim.params.def.EnvironmentDefinition;
import sim.params.def.SpeciesDefinition;

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
public class KittParams extends BaseParams implements SimParams {
    private static final long serialVersionUID = 1L;

    @XmlElement
    private final EnvironmentDefinition environmentDefinition = new EnvironmentDefinition();

    /**
     * list of species definition
     */
    @XmlElementWrapper(name = "speciesDefinitions")
    @XmlElement(name = "definition")
    private final List<SpeciesDefinition> speciesDefs = new ArrayList<>(1);

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

    @Override
    public boolean addOptionalDefinition(OptionalParamDefinition optionalDef) {
	if (optionalDef instanceof SpeciesDefinition) {
	    return speciesDefs.add((SpeciesDefinition) optionalDef);
	}
	throw new IllegalArgumentException("Cannot add " + optionalDef + ". Only instances of "
		+ SpeciesDefinition.class.getSimpleName() + " allowed.");
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
	List<ParamDefinition> defs = new ArrayList<>(speciesDefs.size() + 1);
	defs.add(environmentDefinition);
	defs.addAll(speciesDefs);
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

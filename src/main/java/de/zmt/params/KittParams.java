package de.zmt.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.def.EnvironmentDefinition;
import de.zmt.params.def.OptionalParamDefinition;
import de.zmt.params.def.ParamDefinition;
import de.zmt.params.def.SpeciesDefinition;
import de.zmt.util.AmountUtil;
import de.zmt.util.ParamsUtil;

/**
 * {@link SimParams} class for {@code kitt} containing the simulation
 * configuration.
 * 
 * @author mey
 *
 */
@XStreamAlias("KittParams")
public class KittParams extends BaseParams implements SimParams {
    private static final long serialVersionUID = 1L;

    static {
	XStream xStream = ParamsUtil.getXStreamInstance();
	xStream.processAnnotations(KittParams.class);
	AmountUtil.registerConverters(xStream);
    }

    private final EnvironmentDefinition environmentDefinition = new EnvironmentDefinition();
    @XStreamImplicit
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

package de.zmt.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.def.EnvironmentDefinition;
import de.zmt.params.def.SpeciesDefinition;

/**
 * {@link SimParams} class for {@code kitt} containing the simulation
 * configuration.
 * 
 * @author mey
 *
 */
@XStreamAlias("KittParams")
public class KittParams extends BaseSimParamsNode {
    private static final long serialVersionUID = 1L;

    private final EnvironmentDefinition environmentDefinition = new EnvironmentDefinition();
    @XStreamImplicit
    private final List<SpeciesDefinition> speciesDefs;

    public KittParams() {
        // default setup with one species
        speciesDefs = new ArrayList<>(Collections.singleton(new SpeciesDefinition()));
    }

    public EnvironmentDefinition getEnvironmentDefinition() {
        return environmentDefinition;
    }

    public Collection<SpeciesDefinition> getSpeciesDefs() {
        return Collections.unmodifiableCollection(speciesDefs);
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
        List<ParamDefinition> defs = new ArrayList<>(speciesDefs.size() + 1);
        defs.add(environmentDefinition);
        defs.addAll(speciesDefs);
        return Collections.unmodifiableCollection(defs);
    }

    @Override
    public long getSeed() {
        return environmentDefinition.getSeed();
    }

    @Override
    protected boolean addDefinitionInternal(ParamDefinition definition) {
        if (definition instanceof SpeciesDefinition) {
            return speciesDefs.add((SpeciesDefinition) definition);
        }
        throw new IllegalArgumentException("Cannot add definition of type " + definition.getClass()
                + ". Only instances of " + SpeciesDefinition.class.getSimpleName() + " allowed.");
    }

    @Override
    protected boolean removeDefinitionInternal(ParamDefinition definition) {
        return speciesDefs.remove(definition);
    }
}

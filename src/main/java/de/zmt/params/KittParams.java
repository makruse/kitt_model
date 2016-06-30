package de.zmt.params;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

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
    private final Collection<SpeciesDefinition> speciesDefs = new ArrayList<>(
            Collections.singleton(new SpeciesDefinition()));

    public EnvironmentDefinition getEnvironmentDefinition() {
        return environmentDefinition;
    }

    public Collection<SpeciesDefinition> getSpeciesDefs() {
        return Collections.unmodifiableCollection(speciesDefs);
    }

    @Override
    public Collection<? extends ParamDefinition> getDefinitions() {
        return Stream.concat(Stream.of(environmentDefinition), speciesDefs.stream())
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableCollection));
    }

    @Override
    public long getSeed() {
        return environmentDefinition.getSeed();
    }

    @Override
    protected Collection<Class<? extends ParamDefinition>> getAllowedDefinitionTypes() {
        return Collections.singleton(SpeciesDefinition.class);
    }

    @Override
    protected boolean addDefinitionInternal(ParamDefinition definition) {
        return speciesDefs.add((SpeciesDefinition) definition);
    }

    @Override
    protected boolean removeDefinitionInternal(ParamDefinition definition) {
        return speciesDefs.remove(definition);
    }
}

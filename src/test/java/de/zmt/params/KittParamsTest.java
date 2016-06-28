package de.zmt.params;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import org.hamcrest.CustomMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.zmt.util.ParamsUtil;

public class KittParamsTest {
    private static final String ANOTHER_DEF_NAME = "another species";

    private KittParams params;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
	params = new KittParams();
    }

    @Test
    public void xmlSerialization() throws IOException {
	Path path = folder.newFile().toPath();
	SpeciesDefinition anotherDefinition = new SpeciesDefinition();

	((SpeciesDefinition.MyPropertiesProxy) anotherDefinition.propertiesProxy()).setName(ANOTHER_DEF_NAME);
	params.addDefinition(anotherDefinition);

	ParamsUtil.writeToXml(params, path);
	Collection<SpeciesDefinition> speciesDefs = ParamsUtil.readFromXml(path, KittParams.class).getSpeciesDefs();
	assertThat(speciesDefs, hasItem(new CustomMatcher<SpeciesDefinition>("") {

	    @Override
	    public boolean matches(Object item) {
		return ((item instanceof SpeciesDefinition)
			&& ((SpeciesDefinition) item).getName().equals(ANOTHER_DEF_NAME));
	    }
	}));
    }
}

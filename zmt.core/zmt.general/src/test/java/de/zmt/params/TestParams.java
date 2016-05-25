package de.zmt.params;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlRootElement;

import de.zmt.params.BaseParams;
import de.zmt.params.SimParams;
import de.zmt.params.def.OptionalParamDefinition;
import de.zmt.params.def.ParamDefinition;
import de.zmt.params.def.TestDefinition;

@XmlRootElement(namespace = "http://www.zmt-bremen.de/")
public class TestParams extends BaseParams implements SimParams {
    private static final long serialVersionUID = 1L;

    private TestDefinition testDefinition = new TestDefinition();

    public TestDefinition getDefinition() {
	return testDefinition;
    }

    public void setDefinition(TestDefinition testDefinition) {
	this.testDefinition = testDefinition;
    }

    @Override
    public Collection<ParamDefinition> getDefinitions() {
	return Collections.singleton(testDefinition);
    }

    @Override
    public boolean addOptionalDefinition(OptionalParamDefinition optionalDef) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeOptionalDefinition(OptionalParamDefinition optionalDef) {
	throw new UnsupportedOperationException();
    }

    @Override
    public long getSeed() {
	return 0;
    }
}
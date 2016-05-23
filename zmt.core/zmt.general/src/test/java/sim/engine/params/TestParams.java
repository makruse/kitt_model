package sim.engine.params;

import java.util.Collection;
import java.util.Collections;

import javax.xml.bind.annotation.XmlRootElement;

import sim.engine.params.def.OptionalParamDefinition;
import sim.engine.params.def.ParamDefinition;

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
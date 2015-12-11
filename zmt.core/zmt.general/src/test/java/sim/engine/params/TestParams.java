package sim.engine.params;

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import sim.engine.params.def.*;

@XmlRootElement(name = "params", namespace = "http://www.zmt-bremen.de/")
@SuppressWarnings({ "unused", "serial" })
public class TestParams extends BaseParams implements SimParams {
    private TestDefinition testDefinition = new TestDefinition();

    public TestDefinition getDefinition() {
	return testDefinition;
    }

    public void setDefinition(TestDefinition testDefinition) {
	this.testDefinition = testDefinition;
    }

    @Override
    public Collection<ParamDefinition> getDefinitions() {
	return Arrays.asList((ParamDefinition) testDefinition);
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
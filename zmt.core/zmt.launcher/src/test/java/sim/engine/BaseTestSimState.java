package sim.engine;

import de.zmt.params.TestParams;

public class BaseTestSimState extends BaseZmtSimState<TestParams> {
    private static final long serialVersionUID = 1L;

    @Override
    public Class<? extends TestParams> getParamsClass() {
	return TestParams.class;
    }

}

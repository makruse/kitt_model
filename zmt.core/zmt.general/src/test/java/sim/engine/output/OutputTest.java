package sim.engine.output;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import sim.engine.SimState;
import sim.engine.output.Collector.*;

public class OutputTest {
    private Output output;
    private SimState state;
    private TestSimObject simObject;

    @Before
    public void setUp() throws Exception {
	simObject = new TestSimObject();
	output = new TestOutput(Collections.singleton(simObject));
	state = new SimState(0);
	state.schedule.scheduleRepeating(output);
    }

    @Test
    public void step() {
	Collector mockCollector = mock(Collector.class);
	output.addCollector(mockCollector);
	state.schedule.step(state);

	verify(mockCollector).beforeCollect(isA(BeforeMessage.class));

	ArgumentCaptor<CollectMessage> collectArgument = ArgumentCaptor.forClass(CollectMessage.class);
	verify(mockCollector).collect(collectArgument.capture());
	assertThat(collectArgument.getValue().getSimObject(), is((Object) simObject));

	ArgumentCaptor<AfterMessage> afterArgument = ArgumentCaptor.forClass(AfterMessage.class);
	verify(mockCollector).afterCollect(afterArgument.capture());
	assertThat(afterArgument.getValue().getSteps(), is(state.schedule.getSteps()));
    }

    private static class TestOutput extends Output {
	private static final long serialVersionUID = 1L;

	private final Iterable<?> simObjects;

	public TestOutput(Iterable<?> simObjects) {
	    this.simObjects = simObjects;
	}

	@Override
	protected Iterable<?> obtainSimObject() {
	    return simObjects;
	}
    }

    private static class TestSimObject extends Object {

    }
}

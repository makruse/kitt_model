package sim.engine.output;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import sim.engine.SimState;
import sim.engine.output.message.*;

public class OutputTest {
    private static final int COLLECTOR_STEP_INTERVAL = 2;

    private Output output;
    private SimState state;
    private TestSimObject simObject;

    @Before
    public void setUp() throws Exception {
	simObject = new TestSimObject();
	output = new TestOutput(Collections.singleton(new CollectMessage() {

	    @Override
	    public Object getSimObject() {
		return simObject;
	    }

	}));
	state = new SimState(0);
	state.schedule.scheduleRepeating(output);
    }

    @Test
    public void stepOnInterval() {
	Collector mockCollector = mock(Collector.class);
	output.addCollector(mockCollector, COLLECTOR_STEP_INTERVAL);

	state.schedule.step(state);
	// this time collector should not be called due to interval
	state.schedule.step(state);

	verify(mockCollector).beforeCollect(isA(BeforeMessage.class));

	ArgumentCaptor<CollectMessage> collectArgument = ArgumentCaptor.forClass(CollectMessage.class);
	verify(mockCollector).collect(collectArgument.capture());
	assertThat(collectArgument.getValue().getSimObject(), is((Object) simObject));

	ArgumentCaptor<AfterMessage> afterArgument = ArgumentCaptor.forClass(AfterMessage.class);
	verify(mockCollector).afterCollect(afterArgument.capture());
	// called only in first step
	assertThat(afterArgument.getValue().getSteps(), is(0l));

    }

    private static class TestOutput extends Output {
	private static final long serialVersionUID = 1L;

	private final Iterable<? extends CollectMessage> collectMessages;

	public TestOutput(Iterable<? extends CollectMessage> collectMessages) {
	    super();
	    this.collectMessages = collectMessages;
	}

	@Override
	protected Iterable<? extends CollectMessage> createCollectMessages(Collector recipient, SimState state) {
	    return collectMessages;
	}
    }

    private static class TestSimObject extends Object {

    }
}

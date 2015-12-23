package sim.engine.output;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.*;
import org.mockito.ArgumentCaptor;

import sim.engine.SimState;
import sim.engine.output.message.*;

public class OutputTest {
    private static final int COLLECTOR_STEP_INTERVAL = 2;

    private Output output;
    private SimState state;

    @Before
    public void setUp() throws Exception {
	output = new Output();
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
	assertThat(((DefaultCollectMessage) collectArgument.getValue()).getState(), is(state));

	ArgumentCaptor<AfterMessage> afterArgument = ArgumentCaptor.forClass(AfterMessage.class);
	verify(mockCollector).afterCollect(afterArgument.capture());
	// called only in first step
	assertThat(((DefaultAfterMessage) afterArgument.getValue()).getSteps(), is(0l));
    }

    @Test
    public void stepOnCustomMessages() {
	TestCollector collector = new TestCollector();
	output.addCollector(collector);
	state.schedule.step(state);
	assertTrue(collector.wasCalled());
    }

    private static class TestCollector
	    implements Collector, CreatesBeforeMessage, CreatesCollectMessages, CreatesAfterMessage {
	private static final long serialVersionUID = 1L;

	private static final BeforeMessage BEFORE_MESSAGE = new BeforeMessage() {
	};
	private static final Iterable<CollectMessage> COLLECT_MESSAGES = Collections
		.<CollectMessage> singleton(new CollectMessage() {
		});
	private static final AfterMessage AFTER_MESSAGE = new AfterMessage() {

	    @Override
	    public long getSteps() {
		return 0;
	    }
	};

	private final Set<Cycle> called = EnumSet.noneOf(Cycle.class);

	public boolean wasCalled() {
	    return called.equals(EnumSet.allOf(Cycle.class));
	}

	@Override
	public AfterMessage createAfterMessage(SimState state, AfterMessage defaultMessage) {
	    return AFTER_MESSAGE;
	}

	@Override
	public Iterable<? extends CollectMessage> createCollectMessages(SimState state,
		Iterable<? extends CollectMessage> defaultMessages) {
	    return COLLECT_MESSAGES;
	}

	@Override
	public BeforeMessage createBeforeMessage(SimState state, BeforeMessage defaultMessage) {
	    return BEFORE_MESSAGE;
	}

	@Override
	public void beforeCollect(BeforeMessage message) {
	    assertThat(message, is(BEFORE_MESSAGE));
	    called.add(Cycle.BEFORE);
	}

	@Override
	public void collect(CollectMessage message) {
	    assertThat(COLLECT_MESSAGES, contains(message));
	    called.add(Cycle.COLLECT);
	}

	@Override
	public void afterCollect(AfterMessage message) {
	    assertThat(message, is(AFTER_MESSAGE));
	    called.add(Cycle.AFTER);
	}

	@Override
	public Collectable getCollectable() {
	    return null;
	}

	private static enum Cycle {
	    BEFORE, COLLECT, AFTER
	}
    }
}

package sim.engine.output;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import sim.engine.SimState;
import sim.engine.output.message.AfterMessage;
import sim.engine.output.message.BeforeMessage;
import sim.engine.output.message.CollectMessage;
import sim.engine.output.message.CreatesAfterMessage;
import sim.engine.output.message.CreatesBeforeMessage;
import sim.engine.output.message.CreatesCollectMessages;
import sim.engine.output.message.DefaultAfterMessage;
import sim.engine.output.message.DefaultCollectMessage;
import sim.engine.output.writing.CollectorWriter;

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
	// to step 1
	state.schedule.step(state);

	Collector<?> mockCollector = mock(Collector.class);
	output.addCollector(mockCollector);
	output.putInterval(mockCollector, COLLECTOR_STEP_INTERVAL);

	// step 1: this time the collector should not be called due to interval
	state.schedule.step(state);
	verifyZeroInteractions(mockCollector);

	// step 2: this time the collector should be called
	state.schedule.step(state);
	verify(mockCollector).beforeCollect(isA(BeforeMessage.class));

	ArgumentCaptor<CollectMessage> collectArgument = ArgumentCaptor.forClass(CollectMessage.class);
	verify(mockCollector).collect(collectArgument.capture());
	assertThat(((DefaultCollectMessage) collectArgument.getValue()).getState(), is(state));

	ArgumentCaptor<AfterMessage> afterArgument = ArgumentCaptor.forClass(AfterMessage.class);
	verify(mockCollector).afterCollect(afterArgument.capture());
	// called only in step 2
	assertThat(((DefaultAfterMessage) afterArgument.getValue()).getSteps(), is(2l));
    }

    @Test
    public void stepOnCustomMessages() {
	CallTestCollector collector = new CallTestCollector();
	output.addCollector(collector);
	output.putFactory(collector, (CreatesBeforeMessage) new MessageFactory());
	output.putFactory(collector, (CreatesCollectMessages) new MessageFactory());
	output.putFactory(collector, (CreatesAfterMessage) new MessageFactory());
	state.schedule.step(state);
	assertTrue(collector.wasCalled());
    }

    @Test
    public void stepOnWriter() throws IOException {
	// to step 1
	state.schedule.step(state);

	final Collector<?> collector = new TestCollector(null);
	CollectorWriter mockWriter = mock(CollectorWriter.class);
	when(mockWriter.getCollector()).thenAnswer(new Answer<Collector<?>>() {

	    @Override
	    public Collector<?> answer(InvocationOnMock invocation) throws Throwable {
		return collector;
	    }
	});
	output.putInterval(collector, COLLECTOR_STEP_INTERVAL);

	output.addWriter(mockWriter);

	// step 1: this time the writer should not be called due to interval
	state.schedule.step(state);
	verify(mockWriter, never()).writeValues(anyLong());

	// step 2: this time the writer should be called
	state.schedule.step(state);

	ArgumentCaptor<Long> writeArgument = ArgumentCaptor.forClass(Long.class);
	verify(mockWriter).writeValues(writeArgument.capture());
	// called only in step 2
	assertThat(writeArgument.getValue(), is(2l));
    }

    private static class CallTestCollector implements Collector<Collectable<?>> {
	private static final long serialVersionUID = 1L;

	private final Set<Cycle> called = EnumSet.noneOf(Cycle.class);

	public boolean wasCalled() {
	    return called.equals(EnumSet.allOf(Cycle.class));
	}

	@Override
	public void beforeCollect(BeforeMessage message) {
	    assertThat(message, is(MessageFactory.BEFORE_MESSAGE));
	    called.add(Cycle.BEFORE);
	}

	@Override
	public void collect(CollectMessage message) {
	    assertThat(MessageFactory.COLLECT_MESSAGES, contains(message));
	    called.add(Cycle.COLLECT);
	}

	@Override
	public void afterCollect(AfterMessage message) {
	    assertThat(message, is(MessageFactory.AFTER_MESSAGE));
	    called.add(Cycle.AFTER);
	}

	@Override
	public Collectable<?> getCollectable() {
	    return null;
	}

	private static enum Cycle {
	    BEFORE, COLLECT, AFTER
	}
    }

    private static class MessageFactory implements CreatesBeforeMessage, CreatesCollectMessages, CreatesAfterMessage {

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

    }
}

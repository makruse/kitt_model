package sim.engine.output.message;

/**
 * Default implementation for {@link AfterMessage} containing the steps number.
 * 
 * @author mey
 *
 */
public class DefaultAfterMessage implements AfterMessage {
    private final long steps;

    public DefaultAfterMessage(long steps) {
	super();
	this.steps = steps;
    }

    @Override
    public long getSteps() {
	return steps;
    }

    @Override
    public String toString() {
	return AfterMessage.class.getSimpleName() + "[steps=" + steps + "]";
    }

}

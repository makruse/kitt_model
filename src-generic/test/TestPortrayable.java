package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.zmt.sim.portrayal.portrayable.*;

public class TestPortrayable {
    @Test
    public void test() {
	PortrayalProvider provider = new PortrayalProvider();
	PortrayableA portrayableA = (PortrayableA) provider
		.providePortrayable();
	PortrayableB portrayableB = (PortrayableB) provider
		.providePortrayable();

	assertEquals('A', portrayableA.returnA());
	assertEquals('B', portrayableB.returnB());
    }

    private static class PortrayalProvider implements
	    ProvidesPortrayable<Portrayable> {
	@Override
	public Portrayable providePortrayable() {
	    return new PortrayableImplementation();
	};

	private class PortrayableImplementation implements PortrayableA,
		PortrayableB {

	    @Override
	    public char returnA() {
		return 'A';
	    }

	    @Override
	    public char returnB() {
		return 'B';
	    }
	}
    }
    
    private static interface PortrayableA extends Portrayable {
	char returnA();
    }

    private static interface PortrayableB extends Portrayable {
	char returnB();
    }
}

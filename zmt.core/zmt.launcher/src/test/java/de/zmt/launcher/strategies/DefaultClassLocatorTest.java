package de.zmt.launcher.strategies;

import static org.junit.Assert.assertEquals;

import org.junit.*;
import org.junit.rules.ExpectedException;

import de.zmt.sim.display.ExampleWithUI;
import de.zmt.sim.engine.*;
import sim.display.GUIState;

public class DefaultClassLocatorTest {
    private static final ClassLocator CLASS_LOCATOR = new DefaultClassLocator();
    private static final String SIM_NAME_VALID = "example";
    private static final String SIM_NAME_INVALID = "invalid";

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void findSimStateClassOnValid() throws ClassNotFoundException {
	Class<? extends ZmtSimState> simStateClass = CLASS_LOCATOR
		.findSimStateClass(SIM_NAME_VALID);
	assertEquals(Example.class, simStateClass);
    }

    @Test
    public void findSimStateClassOnInvalid() throws ClassNotFoundException {
	thrown.expect(ClassNotFoundException.class);
	CLASS_LOCATOR.findSimStateClass(SIM_NAME_INVALID);
    }

    @Test
    public void findGuiStateClassOnValid() throws ClassNotFoundException {
	Class<? extends GUIState> guiStateClass = CLASS_LOCATOR
		.findGuiStateClass(SIM_NAME_VALID);
	assertEquals(ExampleWithUI.class, guiStateClass);
    }

    @Test
    public void findGuiStateClassOnInvalid() throws ClassNotFoundException {
	thrown.expect(ClassNotFoundException.class);
	CLASS_LOCATOR.findGuiStateClass(SIM_NAME_INVALID);
    }
}

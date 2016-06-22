package sim.display;

import org.jscience.physics.amount.AmountFormat;

import de.zmt.ecs.Entity;
import de.zmt.util.AmountUtil;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * {@link GUIState} for kitt simulation.
 * 
 * @author mey
 * 
 */
public class KittWithUI extends ZmtGUIState {
    static {
	// only exact digits when formatting amounts
	AmountFormat.setInstance(AmountUtil.FORMAT);
    }

    private DisplayHandler displayHandler;

    public KittWithUI(Kitt state) {
	super(state);
    }

    @Override
    public void init(Controller controller) {
	super.init(controller);

	displayHandler = new DisplayHandler(this);
    }

    @Override
    public Controller createController() {
	ZmtConsole console = new KittConsole(this);
	console.setVisible(true);
	return console;
    }

    @Override
    public void start() {
	super.start();
	setup();
    }

    @Override
    public void load(SimState state) {
	super.load(state);
	setup();
    }

    /**
     * Sets up portrayals in {@link DisplayHandler}.
     */
    private void setup() {
	Entity environment = ((Kitt) state).getEnvironment();
	displayHandler.setupPortrayals(environment);
    }

    @Override
    public void quit() {
	super.quit();
	displayHandler.dispose();
    }
}

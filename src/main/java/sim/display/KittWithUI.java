package sim.display;

import org.jscience.physics.amount.AmountFormat;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.GlobalFlowMap;
import de.zmt.util.AmountUtil;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.params.def.SpeciesDefinition;

/**
 * {@link GUIState} for kitt simulation.
 * 
 * @author mey
 * 
 */
public class KittWithUI extends ZmtGUIState {
    private static final String ADD_OPTIONAL_MENU_ITEM_TITLE = "Species";

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
	ZmtConsole console = new ZmtConsole(this);
	console.addOptionalDefinitionMenuItem(SpeciesDefinition.class, ADD_OPTIONAL_MENU_ITEM_TITLE);
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

	final GlobalFlowMap globalFlowMap = environment.get(GlobalFlowMap.class);
	// update global flow map before to draw the most recent version
	scheduleRepeatingImmediatelyBefore(new Steppable() {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void step(SimState state) {
		globalFlowMap.updateIfDirtyAll();
	    }
	});
    }

    @Override
    public void quit() {
	super.quit();
	displayHandler.dispose();
    }
}

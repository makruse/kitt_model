package sim.display;

import org.jscience.physics.amount.AmountFormat;

import de.zmt.ecs.Entity;
import de.zmt.util.AmountUtil;
import sim.engine.Kitt;
import sim.engine.SimState;
import sim.params.def.SpeciesDefinition;
import sim.portrayal.Inspector;

/**
 * {@link GUIState} for kitt simulation.
 * 
 * @author mey
 * 
 */
public class KittWithUI extends ZmtGUIState {
    private static final String ADD_SPECIES_MENU_ITEM_TEXT = "Species";
    private static final String INSPECT_ENVIRONMENT_MENU_ITEM_TEXT = "Environment";

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
	console.addOptionalDefinitionMenuItem(SpeciesDefinition.class, ADD_SPECIES_MENU_ITEM_TEXT);
	console.addToInspectMenu(new InspectListener(INSPECT_ENVIRONMENT_MENU_ITEM_TEXT) {

	    @Override
	    protected Inspector getInspectorToShow(GUIState state, String name) {
		return Inspector.getInspector(((Kitt) state.state).getEnvironment(), state, name);
	    }
	});
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

package sim.display;

import java.util.function.Function;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.SimulationTime;
import de.zmt.params.SpeciesDefinition;
import sim.engine.Kitt;
import sim.engine.SimState;

/**
 * {@link ZmtConsole} that adds elapsed time and date/time items to the
 * console's lower time combo box.
 * 
 * @author mey
 *
 */
public class KittConsole extends ZmtConsole {
    private static final long serialVersionUID = 1L;

    /** {@link ZmtConsole.TimeBoxItem} that displays the elapsed time. */
    private static final TimeBoxItem ELAPSED_TIME_ITEM = new SimTimeItem("Elapsed Time",
            simulationTime -> simulationTime.computeElapsedTime());
    /** {@link ZmtConsole.TimeBoxItem} that displays date/time. */
    private static final TimeBoxItem DATE_TIME_ITEM = new SimTimeItem("Date/Time", proxy -> proxy.getDateTime());

    private static final String ADD_SPECIES_MENU_ITEM_TEXT = "Species";
    private static final String INSPECT_ENVIRONMENT_MENU_ITEM_TEXT = "Environment";

    public KittConsole(GUIState gui) {
        super(gui);
        addDefinitionMenuItem(SpeciesDefinition.class, ADD_SPECIES_MENU_ITEM_TEXT);
        addInspectMenuItem(INSPECT_ENVIRONMENT_MENU_ITEM_TEXT, (Kitt state) -> state.getEnvironment());
        addTimeBoxItem(ELAPSED_TIME_ITEM);
        addTimeBoxItem(DATE_TIME_ITEM);
        selectTimeBoxItem(ELAPSED_TIME_ITEM);
    }

    /**
     * {@link ZmtConsole.TimeBoxItem} which only calls the given function for
     * creating text if the needed data is present, i.e. the simulation has
     * already started. Otherwise an empty string is returned.
     * 
     * @author mey
     *
     */
    private static class SimTimeItem implements TimeBoxItem {
        private final String itemTitle;
        private final Function<SimulationTime, Object> createTextFunction;

        public SimTimeItem(String itemTitle, Function<SimulationTime, Object> createTextFunction) {
            super();
            this.itemTitle = itemTitle;
            this.createTextFunction = createTextFunction;
        }

        @Override
        public final String createText(SimState state) {
            Entity environment = ((Kitt) state).getEnvironment();
            // only use properties proxy if environment is present
            if (environment == null) {
                return "";
            }
            return createTextFunction.apply(environment.get(SimulationTime.class)).toString();
        }

        @Override
        public String toString() {
            return itemTitle;
        }
    }
}

package sim.display;

import javax.swing.JComboBox;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.environment.SimulationTime;
import sim.engine.Kitt;

/**
 * {@link ZmtConsole} that displays simulation time in a human-readable format.
 * 
 * @author mey
 *
 */
public class KittConsole extends ZmtConsole {
    /** {@link JComboBox} item to select simulation time display. */
    private static final String SIM_TIME_ITEM = "Sim Time";

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked") // timeBox in Console is a raw type
    public KittConsole(GUIState gui) {
	super(gui);
	timeBox.addItem(SIM_TIME_ITEM);
	timeBox.setSelectedItem(SIM_TIME_ITEM);
    }

    @Override
    void updateTime(long steps, double time, double rate) {
	Entity environment = ((Kitt) getSimulation().state).getEnvironment();
	String timeString = "";
	// no need for synchronization, write to int is atomic
	if (timeBox.getSelectedItem() == SIM_TIME_ITEM) {
	    if (environment != null) {
		timeString = ((SimulationTime.MyPropertiesProxy) environment.get(SimulationTime.class)
			.propertiesProxy()).getTime().toString();
	    }
	    updateTimeText(timeString);
	} else {
	    super.updateTime(steps, time, rate);
	}
    }
}

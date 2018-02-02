package sim.display;

import org.jscience.physics.amount.AmountFormat;

import de.zmt.util.AmountUtil;
import sim.engine.Kitt;

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

    public KittWithUI(Kitt state) {
        super(state);
        addListener(new DisplayHandler(this));
    }

    @Override
    public Controller createController() {
        ZmtConsole console = new KittConsole(this);
        console.setVisible(true);
        console.setSize(600, 800);
        return console;
    }
}

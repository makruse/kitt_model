package de.zmt.params.def;

import static de.zmt.ecs.component.agent.Metabolizing.BehaviorMode.*;

import java.io.Serializable;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.util.UnitConstants;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Class associating each behavior mode with a speed factor.
 * 
 * @author mey
 *
 */
class SpeedFactors implements Serializable, ProvidesInspector {
    private static final long serialVersionUID = 1L;
    
    private final EnumToAmountMap<BehaviorMode, Frequency> map = new EnumToAmountMap<>(BehaviorMode.class,
	    UnitConstants.BODY_LENGTH_VELOCITY);

    public SpeedFactors() {
	super();

	map.put(FORAGING, 2.1);
	map.put(MIGRATING, 2.7);
	map.put(RESTING, 0);
    }

    public Amount<Frequency> get(BehaviorMode key) {
	return map.get(key);
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	Inspector inspector = Inspector.getInspector(map, state, name);
	inspector.setTitle(getClass().getSimpleName());
	return inspector;
    }
}

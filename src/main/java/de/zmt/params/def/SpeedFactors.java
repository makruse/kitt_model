package de.zmt.params.def;

import static de.zmt.ecs.component.agent.Metabolizing.BehaviorMode.*;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.util.UnitConstants;

/**
 * Class associating each behavior mode with a speed factor.
 * 
 * @author mey
 *
 */
class SpeedFactors
	extends MapParamDefinition<BehaviorMode, Amount<Frequency>, EnumToAmountMap<BehaviorMode, Frequency>> {
    private static final long serialVersionUID = 1L;

    public SpeedFactors() {
	super(new EnumToAmountMap<>(BehaviorMode.class, UnitConstants.BODY_LENGTH_VELOCITY));

	getMap().put(FORAGING, 2.1);
	getMap().put(MIGRATING, 2.7);
	getMap().put(RESTING, 0);
    }

    public Amount<Frequency> get(BehaviorMode key) {
	return getMap().get(key);
    }
}

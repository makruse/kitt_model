package de.zmt.params;

import static de.zmt.ecs.component.agent.Metabolizing.BehaviorMode.*;

import java.util.Map;

import javax.measure.quantity.Frequency;

import org.jscience.physics.amount.Amount;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import de.zmt.params.def.EnumToAmountMap;
import de.zmt.util.UnitConstants;

/**
 * Class associating each behavior mode with a speed factor.
 * 
 * @author mey
 *
 */
class SpeedFactors extends MapParamDefinition<BehaviorMode, Amount<Frequency>> {
    private static final long serialVersionUID = 1L;

    private static final double DEFAULT_FACTOR_FORAGING = 2.1;
    private static final double DEFAULT_FACTOR_MIGRATING = 2.7;
    private static final int DEFAULT_FACTOR_RESTING = 0;

    @XStreamImplicit
    private final MyMap map = new MyMap();

    public SpeedFactors() {
        super();
        getMap().put(FORAGING, Amount.valueOf(DEFAULT_FACTOR_FORAGING, UnitConstants.BODY_LENGTH_VELOCITY));
        getMap().put(MIGRATING, Amount.valueOf(DEFAULT_FACTOR_MIGRATING, UnitConstants.BODY_LENGTH_VELOCITY));
        getMap().put(RESTING, Amount.valueOf(DEFAULT_FACTOR_RESTING, UnitConstants.BODY_LENGTH_VELOCITY));
    }

    public Amount<Frequency> get(BehaviorMode key) {
        return getMap().get(key);
    }

    @Override
    public Map<BehaviorMode, Amount<Frequency>> getMap() {
        return map;
    }

    private static class MyMap extends EnumToAmountMap<BehaviorMode, Frequency> {
        private static final long serialVersionUID = 1L;

        public MyMap() {
            super(BehaviorMode.class, UnitConstants.BODY_LENGTH_VELOCITY);
        }
    }
}

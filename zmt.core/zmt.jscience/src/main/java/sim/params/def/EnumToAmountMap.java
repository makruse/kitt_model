package sim.params.def;

import java.util.EnumMap;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil;
import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.SimpleInspector;
import sim.portrayal.inspector.ProvidesInspector;
import sim.util.Properties;

/**
 * A {@link EnumMap} implementation mapping to {@link Amount} objects. An
 * {@link Inspector} is provided to make it editable in MASON GUI.
 * 
 * @author mey
 *
 * @param <K>
 *            type of enum
 * @param
 * 	   <Q>
 *            type of {@link Quantity} used
 */
public class EnumToAmountMap<K extends Enum<K>, Q extends Quantity> extends EnumMap<K, Amount<Q>>
	implements ProvidesInspector {
    private static final long serialVersionUID = 1L;

    private final K[] enumConstants;
    private final Unit<Q> storeUnit;
    private final Unit<Q> displayUnit;

    /**
     * Constructs a new {@link EnumToAmountMap} with given store and display
     * units.
     * 
     * @param enumType
     *            the type of enum to use
     * @param storeUnit
     * @param displayUnit
     */
    public EnumToAmountMap(Class<K> enumType, Unit<Q> storeUnit, Unit<Q> displayUnit) {
	super(enumType);

	this.enumConstants = enumType.getEnumConstants();
	this.storeUnit = storeUnit;
	this.displayUnit = displayUnit;
    }

    /**
     * Constructs a new {@link EnumToAmountMap} with given store unit, which
     * also acts as display unit.
     * 
     * @param enumType
     * @param storeUnit
     */
    public EnumToAmountMap(Class<K> enumType, Unit<Q> storeUnit) {
	this(enumType, storeUnit, storeUnit);
    }

    /**
     * Associates an enum constant with a primitive value <strong>in display
     * unit</strong>. The value is converted to the store unit.
     * 
     * @param enumConstant
     *            the enum constant
     * @param valueInDisplayUnit
     *            the value in display unit
     * @return previously associated value with that constant
     */
    protected Amount<Q> put(K enumConstant, double valueInDisplayUnit) {
	return put(enumConstant, Amount.valueOf(valueInDisplayUnit, getDisplayUnit()).to(getStoreUnit()));
    }

    protected Unit<Q> getStoreUnit() {
	return storeUnit;
    }

    protected Unit<Q> getDisplayUnit() {
	return displayUnit;
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	SimpleInspector inspector = new SimpleInspector(new MyProperties(), state, name);
	inspector.setTitle(getClass().getSimpleName());
	return inspector;
    }

    private class MyProperties extends Properties {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isVolatile() {
	    return false;
	}

	@Override
	public int numProperties() {
	    return size();
	}

	@Override
	public Object getValue(int index) {
	    return get(enumConstants[index]).to(displayUnit).toString();
	}

	@Override
	public boolean isReadWrite(int index) {
	    if (index < 0 || index > numProperties()) {
		return false;
	    }
	    return true;
	}

	@Override
	public String getName(int index) {
	    return enumConstants[index].name();
	}

	@Override
	public Class<?> getType(int index) {
	    return String.class;
	}

	@Override
	protected Object _setValue(int index, Object value) {
	    Amount<Q> amount = AmountUtil.parseAmount(value.toString(), displayUnit);
	    put(enumConstants[index], amount.to(storeUnit));
	    return amount;
	}

    }
}
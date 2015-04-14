package de.zmt.kitt.sim.engine.output;


/**
 * Class with encapsulated map having {@link Clearable} values.
 * 
 * @author cmeyer
 * 
 * @param <K>
 *            map key type
 * @param <V>
 *            map value type
 */
class EncapsulatedClearableMap<K, V extends Clearable> extends
	EncapsulatedMap<K, V> implements Clearable {
    private static final long serialVersionUID = 1L;

    @Override
    public void clear() {
	for (Clearable data : map.values()) {
	    data.clear();
	}
    }

}
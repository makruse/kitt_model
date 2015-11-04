/**
 * @author mey
 *
 */
// Amount is (un)marshalled via XmlAmountAdapter in this package
@XmlJavaTypeAdapter(value = XmlAmountAdapter.class, type = Amount.class)
package sim.params.def;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jscience.physics.amount.Amount;

import de.zmt.util.AmountUtil.XmlAmountAdapter;

package sim.engine.output;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import sim.display.GUIState;
import sim.portrayal.Inspector;
import sim.portrayal.inspector.ProvidesInspector;

/**
 * Abstract {@link OneShotCollectable} implementation backed by a
 * two-dimensional list. This class also implements {@link ProvidesInspector} to
 * display the data in a table.
 * 
 * @author mey
 *
 * @param <V>
 *            the type of data to be aggregated
 */
public abstract class AbstractOneShotCollectable<V> extends AbstractCollectable<List<V>>
	implements OneShotCollectable<V, List<V>>, ProvidesInspector {
    private static final long serialVersionUID = 1L;

    public AbstractOneShotCollectable(List<List<V>> values) {
	super(values);
    }

    @Override
    public Inspector provideInspector(GUIState state, String name) {
	return new MyInspector();
    }

    /**
     * {@link Inspector} showing a {@link JTable} with the data of this
     * collectable.
     * 
     * @author mey
     *
     */
    private class MyInspector extends Inspector {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel tableModel = new NonEditableTableModel();

	public MyInspector() {
	    super();
	    setLayout(new BorderLayout());
	    JTable table = new JTable(tableModel);
	    add(table.getTableHeader(), BorderLayout.PAGE_START);
	    add(table, BorderLayout.CENTER);
	}

	@Override
	public void updateInspector() {
	    List<String> headers = obtainHeaders();
	    for (int i = 0; i < AbstractOneShotCollectable.this.getSize(); i++) {
		tableModel.addColumn(headers.get(i), getValues().get(i).toArray());
	    }
	}

    }

    /**
     * {@link DefaultTableModel} with with non-editable cells.
     * 
     * @author mey
     *
     */
    private static class NonEditableTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean isCellEditable(int row, int column) {
	    return false;
	}
    }
}

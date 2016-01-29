package sim.engine.output;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Vector;

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
    public int getColumnSize() {
	return getValues().get(0).size();
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
    class MyInspector extends Inspector {
	private static final long serialVersionUID = 1L;

	private final DefaultTableModel tableModel = new NonEditableTableModel();

	public MyInspector() {
	    super();
	    setLayout(new BorderLayout());
	    JTable table = new JTable(tableModel);
	    add(table.getTableHeader(), BorderLayout.PAGE_START);
	    add(table, BorderLayout.CENTER);
	    updateInspector();
	}
	
	/**
	 * Gets the data vector used in the inspector's table. Use for testing.
	 * 
	 * @see DefaultTableModel#getDataVector()
	 * @return data vector from table model
	 */
	@SuppressWarnings("unchecked")
	Vector<Vector<V>> getDataVector() {
	    return tableModel.getDataVector();
	}

	@Override
	public void updateInspector() {
	    // row size = number of columns (in a row)
	    int columnCount = AbstractOneShotCollectable.this.getSize();
	    // column size = number of rows (in a column)
	    int rowCount = getColumnSize();

	    tableModel.setRowCount(rowCount);
	    tableModel.setColumnCount(columnCount);

	    // set headers
	    tableModel.setColumnIdentifiers(obtainHeaders().toArray());

	    // set values
	    for (int row = 0; row < rowCount; row++) {
		for (int column = 0; column < columnCount; column++) {
		    tableModel.setValueAt(getValues().get(column).get(row), row, column);
		}
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

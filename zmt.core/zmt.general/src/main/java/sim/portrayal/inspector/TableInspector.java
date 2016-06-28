package sim.portrayal.inspector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import sim.portrayal.Inspector;

/**
 * Inspector for displaying values in a {@link JTable}.
 * 
 * @author mey
 * @author jle
 *
 * @param <ColumnK>
 *            the type for the column keys
 * @param <RowK>
 *            the type for the row keys
 * @param <V>
 *            the type of data contained
 */
public abstract class TableInspector<ColumnK, RowK, V> extends Inspector {
    private static final long serialVersionUID = 1L;

    private final List<ColumnK> columnKeys;
    private final List<RowK> rowKeys;
    private final Class<V> dataClass;

    private final AbstractTableModel tableModel;

    /**
     * Constructs a new {@link TableInspector}. The table will be sized
     * according to the number of row and column keys.
     * 
     * @param columnKeys
     *            the column keys
     * @param rowKeys
     *            the row keys
     * @param dataClass
     *            the class of data contained
     * @param name
     *            the displayed name
     */
    public TableInspector(Collection<ColumnK> columnKeys, Collection<RowK> rowKeys, Class<V> dataClass, String name) {
        this.rowKeys = new ArrayList<>(rowKeys);
        this.columnKeys = new ArrayList<>(columnKeys);
        this.dataClass = dataClass;

        // to resize table according to window
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        tableModel = new MyTableModel();
        tableModel.addTableModelListener(new MyTableModelListener());

        JTable table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setModel(tableModel);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        // scroll pane needed, otherwise headers invisible
        JScrollPane scrollPane = new JScrollPane(table);

        scrollPane.setRowHeaderView(new MyRowHeaderTable(table));
        add(scrollPane);
        setBorder(new TitledBorder(name));
    }

    /**
     * Returns the value from the inspected object for the given keys.
     * 
     * @param columnKey
     *            the column key
     * @param rowKey
     *            the row key
     * @return the value
     */
    protected abstract V getValue(ColumnK columnKey, RowK rowKey);

    /**
     * Sets the value on the inspected object for the given keys.
     * 
     * @param columnKey
     *            the column key
     * @param rowKey
     *            the row key
     * @param value
     *            the value to set
     */
    protected abstract void setValue(ColumnK columnKey, RowK rowKey, V value);

    @Override
    public void updateInspector() {
        tableModel.fireTableDataChanged();
    }

    private class MyTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        @Override
        public int getRowCount() {
            return rowKeys.size();
        }

        @Override
        public int getColumnCount() {
            return columnKeys.size();
        }

        @Override
        public Class<V> getColumnClass(int columnIndex) {
            return dataClass;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public V getValueAt(int rowIndex, int columnIndex) {
            return getValue(columnKeys.get(columnIndex), rowKeys.get(rowIndex));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            setValue(columnKeys.get(columnIndex), rowKeys.get(rowIndex), (V) aValue);
        }

        @Override
        public String getColumnName(int column) {
            return columnKeys.get(column).toString();
        }
    }

    private class MyTableModelListener implements TableModelListener {
        @SuppressWarnings("unchecked")
        @Override
        public void tableChanged(TableModelEvent e) {
            int rowIndex = e.getFirstRow();
            int columnIndex = e.getColumn();
            // new data was set, do not update
            if (columnIndex == TableModelEvent.ALL_COLUMNS || rowIndex == TableModelEvent.HEADER_ROW) {
                return;
            }

            TableModel model = (TableModel) e.getSource();
            System.out.println(rowIndex + ", " + columnIndex);
            assert rowIndex >= 0;
            assert columnIndex >= 0;
            V value = (V) model.getValueAt(rowIndex, columnIndex);

            ColumnK columnKey = columnKeys.get(columnIndex);
            RowK rowKey = rowKeys.get(rowIndex);
            setValue(columnKey, rowKey, value);
        }
    }

    /**
     * {@link RowHeaderTable} displaying row keys.
     * 
     * @author mey
     *
     */
    private class MyRowHeaderTable extends RowHeaderTable {
        private static final long serialVersionUID = 1L;

        private MyRowHeaderTable(JTable table) {
            super(table);
        }

        @Override
        public Object getValueAt(int row, int column) {
            return rowKeys.get(row);
        }
    }
}

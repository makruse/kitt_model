package de.zmt.output;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

public class AbstractOneShotCollectableTest {
    private static final int ROW_COUNT = 3;
    private static final int COLUMN_COUNT = 2;

    private static final String INITIAL_COMMENT = "initial";
    private static final String CHANGED_COMMENT = "changed";

    private AbstractOneShotCollectable<TestValue> collectable;

    @Before
    public void setUp() throws Exception {
        collectable = new TestAbstractOneShotCollectable(createValues(), createHeaders());
    }

    @Test
    public void testInspector() {
        @SuppressWarnings("unchecked")
        AbstractOneShotCollectable<TestValue>.MyInspector inspector = (AbstractOneShotCollectable<TestValue>.MyInspector) collectable
                .provideInspector(null, null);

        // verify initial setup
        verifyTableData(inspector.getDataVector(), INITIAL_COMMENT);

        // change data in collectable
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                collectable.getValues().get(column).set(row, new TestValue(row, column, CHANGED_COMMENT));
            }
        }
        inspector.updateInspector();

        // verify changes
        verifyTableData(inspector.getDataVector(), CHANGED_COMMENT);
    }

    private static void verifyTableData(Vector<Vector<TestValue>> dataVector, String comment) {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                assertThat(dataVector.get(row).get(column), is(new TestValue(row, column, comment)));
            }
        }
    }

    /**
     * Creates headers named by column index.
     * 
     * @return headers
     */
    private static List<String> createHeaders() {
        List<String> headers = new ArrayList<>(COLUMN_COUNT);
        for (int i = 0; i < COLUMN_COUNT; i++) {
            headers.add("h" + i);
        }
        return Collections.unmodifiableList(headers);
    }

    /**
     * Creates values named by their indices.
     * 
     * @return values
     */
    private static List<List<TestValue>> createValues() {
        List<List<TestValue>> values = new ArrayList<>(ROW_COUNT);

        for (int j = 0; j < COLUMN_COUNT; j++) {
            ArrayList<TestValue> column = new ArrayList<>(COLUMN_COUNT);
            for (int i = 0; i < ROW_COUNT; i++) {
                column.add(new TestValue(i, j, INITIAL_COMMENT));
            }
            values.add(column);
        }

        return values;
    }

    private static class TestAbstractOneShotCollectable extends AbstractOneShotCollectable<TestValue> {
        private static final long serialVersionUID = 1L;
        private final List<String> headers;

        public TestAbstractOneShotCollectable(List<List<TestValue>> values, List<String> headers) {
            super(values);
            this.headers = headers;
        }

        @Override
        public List<String> obtainHeaders() {
            return headers;
        }
    }

    private static class TestValue {
        private final int row;
        private final int column;
        private final String comment;

        public TestValue(int row, int column, String comment) {
            super();
            this.row = row;
            this.column = column;
            this.comment = comment;
        }

        @Override
        public String toString() {
            return "[row=" + row + ", column=" + column + ", comment=" + comment + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + column;
            result = prime * result + ((comment == null) ? 0 : comment.hashCode());
            result = prime * result + row;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestValue other = (TestValue) obj;
            if (column != other.column) {
                return false;
            }
            if (comment == null) {
                if (other.comment != null) {
                    return false;
                }
            } else if (!comment.equals(other.comment)) {
                return false;
            }
            if (row != other.row) {
                return false;
            }
            return true;
        }

    }
}

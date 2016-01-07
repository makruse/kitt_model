package sim.engine.output.writing;

import java.io.*;
import java.util.*;

import de.zmt.io.CsvWriter;
import sim.engine.output.*;
import sim.engine.output.message.AfterMessage;

/**
 * Decorator class writing data from {@link OneShotCollectable} into a step
 * indexed file.
 * 
 * @author mey
 * @param <T>
 *            the type of the contained {@link Collectable}
 *
 */
public class OneShotWritingCollector<T extends Collectable<?>> extends AbstractWritingCollector<T> {
    private static final long serialVersionUID = 1L;

    /** Separator appearing in file names different items. */
    private static final String FILENAME_SEPERATOR = "_";

    private CsvWriter writer;
    private final File outputFile;

    public OneShotWritingCollector(Collector<T> collector, File outputFile) {
	super(collector);
	this.outputFile = outputFile;
    }

    @Override
    protected CsvWriter getWriter() {
	return writer;
    }

    @Override
    protected void writeValues(AfterMessage message) throws IOException {
	long steps = message.getSteps();
	File outputFileWithSteps = CsvWriter.generateIndexedFile(outputFile, FILENAME_SEPERATOR, (int) steps);
	writer = new CsvWriter(outputFileWithSteps);
	writer.setStepsWriting(false);
	writeHeaders();

	for (Iterable<Object> row : new RowsIterable(getCollectable())) {
	    writer.writeValues(row, steps);
	}
    }

    /**
     * {@code Iterator} that iterates a {@code OneShotCollectable} row by row so
     * that it can be fed into a {@code CsvWriter}.
     * 
     * @author mey
     *
     */
    private static class RowsIterable implements Iterable<RowsIterable.ValuesIterable> {
	private final Collectable<?> collectable;

	private RowsIterable(Collectable<?> collectable) {
	    this.collectable = collectable;
	}

	@Override
	public RowsIterator iterator() {
	    final Collection<Iterator<?>> columnIterators = new ArrayList<>(collectable.getSize());
	    int columnSize;
	    // add the iterator of every column
	    if (collectable instanceof OneShotCollectable) {
		OneShotCollectable<?, ?> oneShotCollectable = (OneShotCollectable<?, ?>) collectable;
		columnSize = oneShotCollectable.getColumnSize();

		for (Iterable<?> column : oneShotCollectable.obtainValues()) {
		    columnIterators.add(column.iterator());
		}
	    }
	    // treat items as singleton columns
	    else {
		columnSize = 1;
		for (Object column : collectable.obtainValues()) {
		    columnIterators.add(Collections.singleton(column).iterator());
		}
	    }

	    return new RowsIterator(columnIterators, columnSize);
	}

	/**
	 * {@code Iterator} of {@code RowsIterable} creating a
	 * {@code ValuesIterable} for every row.
	 * 
	 * @author mey
	 *
	 */
	private static class RowsIterator implements Iterator<ValuesIterable> {
	    private final Collection<Iterator<?>> columnIterators;
	    private final int columnSize;
	    private int columnIndex = 0;

	    public RowsIterator(Collection<Iterator<?>> columnIterators, int columnSize) {
		super();
		this.columnIterators = columnIterators;
		this.columnSize = columnSize;
	    }

	    @Override
	    public ValuesIterable next() {
		columnIndex++;
		return new ValuesIterable(columnIterators);
	    }

	    @Override
	    public boolean hasNext() {
		return columnIndex < columnSize;
	    }
	}

	/**
	 * {@code Iterable} that picks the next item of each column, containing
	 * the items of a single row to be fed into a {@code CsvWriter}.
	 * 
	 * @author mey
	 *
	 */
	private static class ValuesIterable implements Iterable<Object> {
	    private final Iterable<Iterator<?>> columnIterators;

	    public ValuesIterable(Iterable<Iterator<?>> columnIterators) {
		super();
		this.columnIterators = columnIterators;
	    }

	    @Override
	    public Iterator<Object> iterator() {
		final Iterator<Iterator<?>> columnIteratorsIterator = columnIterators.iterator();
		return new Iterator<Object>() {

		    @Override
		    public Object next() {
			return columnIteratorsIterator.next().next();
		    }

		    @Override
		    public boolean hasNext() {
			return columnIteratorsIterator.hasNext();
		    }
		};
	    }
	}
    }
}

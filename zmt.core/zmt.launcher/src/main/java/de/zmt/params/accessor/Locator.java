package de.zmt.params.accessor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import de.zmt.params.accessor.DefinitionAccessor.Identifier;

/**
 * This class is a wrapper to hold the identifiers needed to locate a parameter
 * within a {@link DefinitionAccessor}.
 * 
 * @author mey
 *
 */
@XStreamAlias("Locator")
public class Locator implements Serializable {
    private static final long serialVersionUID = 1L;

    /** The identifiers as raw objects as they are appear in XML. */
    @XStreamImplicit
    private final Collection<Object> identifiers;

    /**
     * Constructs new {@link Locator} from given identifiers.
     * 
     * @param identifiers
     *            the identifiers to use
     */
    public Locator(Stream<? extends Identifier<?>> identifiers) {
        super();
        // unwrap identifiers and keep raw objects
        this.identifiers = identifiers.map(identifier -> identifier.get()).collect(Collectors.toList());
    }

    /**
     * Constructs new {@link Locator} from given identifiers.
     * 
     * @param identifiers
     *            the identifiers to use
     */
    public Locator(Collection<? extends Identifier<?>> identifiers) {
        this(identifiers.stream());
    }

    /**
     * Constructs new {@link Locator} from given raw identifiers.
     * 
     * @param rawIdentifiers
     *            the raw identifiers to use
     */
    public Locator(Object... rawIdentifiers) {
        super();
        identifiers = Arrays.asList(rawIdentifiers);
    }

    public Stream<? extends Identifier<?>> getIdentifiers() {
        // wrap the raw identifiers
        return identifiers.stream().map(Identifier::create);
    }

    /**
     * Returns <code>true</code> if this locator does not contain any
     * identifiers.
     * 
     * @return <code>true</code> if this locator does not contain any
     *         identifiers
     */
    public boolean isEmpty() {
        return identifiers.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifiers == null) ? 0 : identifiers.hashCode());
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
        Locator other = (Locator) obj;
        if (identifiers == null) {
            if (other.identifiers != null) {
                return false;
            }
        } else if (!identifiers.equals(other.identifiers)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + identifiers + "]";
    }
}
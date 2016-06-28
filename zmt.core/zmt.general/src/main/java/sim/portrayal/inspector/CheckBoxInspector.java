package sim.portrayal.inspector;

import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;

import sim.display.Console;
import sim.display.GUIState;
import sim.portrayal.Inspector;

/**
 * An {@link Inspector} that allows to toggle the presence of elements in a
 * {@link Set} within a GUI. It displays a check box for each of the possible
 * elements and the state is represented in the associated set. This works well
 * with enum constants using an {@link EnumSet}.
 * 
 * @author mey
 *
 * @param <T>
 *            element type
 */
public class CheckBoxInspector<T> extends Inspector {
    private static final long serialVersionUID = 1L;

    /**
     * Default layout wraps check boxes before letting scroll bars to be
     * displayed.
     */
    private static final LayoutManager DEFAULT_LAYOUT = new WrapLayout(FlowLayout.LEADING);

    /** Inspected set. */
    private final Set<T> set;
    /** {@link JCheckBox} for every element. */
    private final Map<T, JCheckBox> checkBoxes;
    /**
     * Reference to model inspector to be updated when set contents are changed.
     * Can be <code>null</code>.
     */
    private final Inspector modelInspector;

    /**
     * Instantiates a new {@link CheckBoxInspector}.
     *
     * @param set
     *            the set this inspector associates with
     * @param allPossibleElements
     *            all possible elements the set could have, a check box is
     *            created for each of them
     * @param state
     *            the gui state
     * @param title
     *            the title to be displayed in top bar
     */
    public CheckBoxInspector(Set<T> set, Collection<T> allPossibleElements, GUIState state, String title) {
        this.set = set;
        this.checkBoxes = new HashMap<>();
        if (state.controller instanceof Console) {
            modelInspector = ((Console) state.controller).getModelInspector();
        } else {
            modelInspector = null;
        }

        // create a check box for every enum value
        for (T value : allPossibleElements) {
            JCheckBox checkBox = new JCheckBox(value.toString());
            checkBox.addItemListener(new MyItemListener(value));
            checkBoxes.put(value, checkBox);
            add(checkBox);
        }

        setLayout(DEFAULT_LAYOUT);
        setTitle(title);
        updateInspector();
    }

    @Override
    public void updateInspector() {
        for (T value : checkBoxes.keySet()) {
            JCheckBox checkBox = checkBoxes.get(value);
            checkBox.setSelected(set.contains(value));
        }
    }

    /**
     * Listener that updates associated enum set when changing values of check
     * boxes.
     * 
     * @author mey
     *
     */
    private class MyItemListener implements ItemListener {
        private final T value;

        public MyItemListener(T value) {
            super();
            this.value = value;
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                set.add(value);
            }
            // deselected
            else {
                set.remove(value);
            }
            if (modelInspector != null) {
                modelInspector.updateInspector();
            }
        }
    }

    /**
     * Helper class to wrap the {@code CheckBoxInspector} into the
     * {@link ProvidesInspector} interface.
     * 
     * @author mey
     *
     * @param <T>
     */
    public static class ProvidesCheckBoxInspector<T> implements ProvidesInspector {
        private final Set<T> set;
        private final Collection<T> allPossibleValues;
        private final String title;

        /**
         * Constructs a wrapper providing a {@code CheckBoxInspector} with given
         * parameters.
         * 
         * @see CheckBoxInspector#CheckBoxInspector(Set, Collection, GUIState,
         *      String)
         * @param set
         * @param allPossibleValues
         * @param name
         */
        public ProvidesCheckBoxInspector(Set<T> set, Collection<T> allPossibleValues, String name) {
            super();
            this.set = set;
            this.allPossibleValues = allPossibleValues;
            this.title = name;
        }

        @Override
        public Inspector provideInspector(GUIState state, String name) {
            return new CheckBoxInspector<>(set, allPossibleValues, state, name != null ? name : this.title);
        }

        @Override
        public String toString() {
            return set.toString();
        }
    }
}

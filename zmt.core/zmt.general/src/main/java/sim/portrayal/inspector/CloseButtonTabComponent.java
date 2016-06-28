package sim.portrayal.inspector;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.Border;

/**
 * Tab component with a label displaying the definition's title and a close
 * button that removes the associated tab.
 * 
 * @see <a href=
 *      "http://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html">
 *      Tutorial: How to Use Tabbed Panes</a>
 * @author mey
 * 
 */
public class CloseButtonTabComponent extends JComponent {

    private static final long serialVersionUID = 1L;

    private static final FlowLayout LAYOUT_NO_GAPS = new FlowLayout(FlowLayout.LEFT, 0, 0);
    private static final String CLOSE_TAB_TOOLTIP = "close this tab";
    private static final Border BORDER_WITH_EXTRA_SPACE = BorderFactory.createEmptyBorder(0, 8, 0, 0);
    private static final String CLOSE_BUTTON_TEXT = "x";

    /**
     * Constructs a new {@link CloseButtonTabComponent} placing the close button
     * next to the given child component.
     * 
     * @param child
     *            the child component
     * @param closeButtonListener
     *            the listener for the close button
     */
    public CloseButtonTabComponent(Component child, ActionListener closeButtonListener) {
        setLayout(LAYOUT_NO_GAPS);

        add(child);
        CloseButton closeButton = new CloseButton();
        closeButton.addActionListener(closeButtonListener);
        add(closeButton);
    }

    private class CloseButton extends JButton {
        private static final long serialVersionUID = 1L;

        public CloseButton() {
            setText(CLOSE_BUTTON_TEXT);
            setToolTipText(CLOSE_TAB_TOOLTIP);
            setContentAreaFilled(false);
            setFocusable(false);
            setBorder(BORDER_WITH_EXTRA_SPACE);
            setBorderPainted(true);
            setRolloverEnabled(true);
        }
    }
}

package sim.portrayal.inspector;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import de.zmt.params.ParamDefinition;
import sim.display.GUIState;
import sim.portrayal.Inspector;

/**
 * {@link TabbedInspector} with tabs from inspectors of {@link ParamDefinition}
 * objects
 * 
 * @author mey
 * 
 */
public class ParamsInspector extends TabbedInspector {
    private static final long serialVersionUID = 1L;

    /** {@link GUIState} of this inspector. */
    private final GUIState gui;
    /**
     * Consumer to be called when a {@link InspectorRemovable} definition is
     * removed.
     */
    private final Consumer<ParamDefinition> onRemove;
    /** Definitions mapped to their added inspector. */
    private final Map<ParamDefinition, Inspector> inspectors = new HashMap<>();

    /**
     * Constructs a {@code ParamsInspector} without any tabs.
     * 
     * @param gui
     *            GUI state of this inspector
     * @param onRemove
     *            the consumer to be called when a {@link InspectorRemovable}
     *            definition is removed
     */
    public ParamsInspector(GUIState gui, Consumer<ParamDefinition> onRemove) {
        super();
        this.gui = gui;
        this.onRemove = onRemove;

        setVolatile(false);
        // scroll buttons in tab bar
        tabs.setTabPlacement(JTabbedPane.TOP);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    /**
     * Constructs a {@code ParamsInspector} populated from given definitions.
     * 
     * @param gui
     *            GUI state of this inspector
     * @param onRemove
     *            the consumer to be called when a {@link InspectorRemovable}
     *            definition is removed
     * @param definitions
     *            the definitions added to the inspector initially
     */
    public ParamsInspector(GUIState gui, Consumer<ParamDefinition> onRemove,
            Collection<? extends ParamDefinition> definitions) {
        this(gui, onRemove);
        addDefinitionTabs(definitions);
    }

    /**
     * Adds an inspector for every given definition as a new tab.
     * 
     * @param definitions
     * @see #addDefinitionTab(ParamDefinition)
     */
    public void addDefinitionTabs(Collection<? extends ParamDefinition> definitions) {
        for (ParamDefinition definition : definitions) {
            addDefinitionTab(definition);
        }
    }

    /**
     * Adds inspector of given {@link ParamDefinition} as a new tab. If
     * annotated with {@link InspectorRemovable}, a close button is added next
     * to the label triggering the definition's removal.
     * 
     * @param definition
     *            the parameter definition
     * @return the added {@link Inspector} from given {@link ParamDefinition}
     */
    public Inspector addDefinitionTab(ParamDefinition definition) {
        Inspector inspector = Inspector.getInspector(definition, gui, null);
        addInspector(inspector);
        inspectors.put(definition, inspector);

        Component tabComponent = new JLabel() {
            private static final long serialVersionUID = 1L;

            @Override
            public String getText() {
                // text changes with definition's title
                if (definition.getTitle() != null && !super.getText().equals(definition.getTitle())) {
                    // need call to update internal state (e.g. to change width)
                    setText(definition.getTitle());
                }

                return super.getText();
            }
        };
        if (definition.getClass().isAnnotationPresent(InspectorRemovable.class)) {
            // place the close button next to the label
            tabComponent = new CloseButtonTabComponent(tabComponent, new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // remove tab with this component
                    removeInspector(inspector);
                    onRemove.accept(definition);
                }
            });
        }
        tabs.setTabComponentAt(tabs.indexOfComponent(inspector), tabComponent);

        // switch to newly added inspector
        tabs.setSelectedComponent(inspector);

        return inspector;
    }

    /**
     * Removes inspector of given {@link ParamDefinition} from the tabs.
     * 
     * @param definition
     * @return the removed {@link Inspector} of given {@link ParamDefinition}
     */
    public Inspector removeDefinitionTab(ParamDefinition definition) {
        if (inspectors.containsKey(definition)) {
            return removeInspector(inspectors.get(definition));
        }
        return null;
    }

    /**
     * Indicates that a {@link ParamDefinition} is optional and can be removed
     * by the user. {@link ParamsInspector} will place a close button on each
     * definition with this annotation.
     * 
     * @author mey
     *
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface InspectorRemovable {
    }
}

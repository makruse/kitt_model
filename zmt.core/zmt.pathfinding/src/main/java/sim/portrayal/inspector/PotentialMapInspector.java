package sim.portrayal.inspector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import de.zmt.pathfinding.PotentialMap;
import sim.display.GUIState;
import sim.engine.Stoppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.util.gui.ColorMap;
import sim.util.gui.ColorMapFactory;
import sim.util.gui.NumberTextField;

/**
 * {@link Inspector} to display a {@link PotentialMap} within the MASON GUI.
 * Attractive potential values are displayed in blue getting brighter towards
 * neutral ones while repulsive values are displayed in red.
 * 
 * @author mey
 *
 */
public class PotentialMapInspector extends Inspector {
    private static final int SCROLL_UNIT_INCREMENT = 10;

    private static final long serialVersionUID = 1L;

    private final PotentialMap map;
    private final FastValueGridPortrayal2D portrayal;
    private final GUIState guiState;

    private final NumberTextField scaleField;
    private final JScrollPane scrollPane;
    private final JComponent displayComponent;

    private double scale = 1;

    /**
     * Instantiates a {@link PotentialMapInspector} displaying the given map.
     * 
     * @param state
     * @param map
     */
    public PotentialMapInspector(GUIState state, PotentialMap map) {
	this.map = map;
	portrayal = new FastValueGridPortrayal2D(true);
	portrayal.setField(map.providePortrayable().getField());
	portrayal.setMap(ColorMapFactory.createForPotentials());
	this.guiState = state;
	setTitle(map.toString());

	setLayout(new BorderLayout());
	scaleField = new NumberTextField("  Scale: ", 1.0, true) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public double newValue(double newValue) {
		if (newValue <= 0.0) {
		    newValue = currentValue;
		}
		setScale(newValue);
		return newValue;
	    }
	};
	scaleField.setToolTipText("Zoom in and out");
	scaleField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
	add(scaleField, BorderLayout.PAGE_START);

	displayComponent = new DisplayComponent();
	displayComponent.setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
	scrollPane = new JScrollPane(displayComponent);
	scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
	scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT_INCREMENT);
	add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Sets the scale (the zoom value) of the Display2D
     * 
     * @param scale
     *            the scale value
     */
    // most code from Display2D
    public void setScale(double scale) {
	double oldScale = this.scale;

	if (scale > 0.0) {
	    this.scale = scale;
	    scaleField.setValue(scale);
	} else {
	    throw new RuntimeException("scale requires a value which is > 0.");
	}

	// grab the original location
	JViewport viewport = scrollPane.getViewport();
	Rectangle r = viewport.getViewRect();

	// scroll to keep the zoomed-in region centered -- this is prettier
	double centerx = r.x + r.width / 2.0;
	double centery = r.y + r.height / 2.0;
	centerx *= scale / oldScale;
	centery *= scale / oldScale;
	Point topleft = new Point((int) (centerx - r.width / 2.0), (int) (centery - r.height / 2.0));
	if (topleft.x < 0) {
	    topleft.x = 0;
	}
	if (topleft.y < 0) {
	    topleft.y = 0;
	}

	viewport.setViewPosition(topleft);

	Dimension displayPreferredSize = new Dimension();
	displayPreferredSize.setSize(map.getWidth() * scale, map.getHeight() * scale);
	displayComponent.setPreferredSize(displayPreferredSize);
	displayComponent.revalidate(); // update scrollbars

	repaint();
    }

    /**
     * Overrides default {@link ColorMap} with a custom one.
     * 
     * @see ColorMapFactory
     * @param map
     */
    public void setColorMap(ColorMap map) {
	portrayal.setMap(map);
    }

    @Override
    public void updateInspector() {
	portrayal.setDirtyField(true);
    }

    @Override
    public JFrame createFrame(Stoppable stopper) {
	JFrame frame = super.createFrame(stopper);
	// get rid of scroller from super, we have our own
	frame.setContentPane(this);
	frame.pack();
	return frame;
    }

    /**
     * Displays portrayal according to scale.
     * 
     * @author mey
     *
     */
    private class DisplayComponent extends JComponent {
	private static final long serialVersionUID = 1L;

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);

	    Dimension preferredSize = getPreferredSize();
	    Rectangle2D.Double draw = new Rectangle2D.Double(0, 0, preferredSize.getWidth(), preferredSize.getHeight());
	    // draw everything, draw and clip is the same
	    portrayal.draw(portrayal.getField(), (Graphics2D) g, new DrawInfo2D(guiState, portrayal, draw, draw));
	}
    }
}
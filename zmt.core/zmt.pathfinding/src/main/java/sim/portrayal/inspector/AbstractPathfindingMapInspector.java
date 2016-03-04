package sim.portrayal.inspector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;

import de.zmt.pathfinding.PathfindingMap;
import sim.display.GUIState;
import sim.engine.Stoppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.util.Bag;
import sim.util.gui.NumberTextField;

/**
 * Abstract implementation for an {@link Inspector} to display a
 * {@link PathfindingMap} within the MASON GUI.
 * 
 * @author mey
 * @param <T>
 *            the type of pathfinding map
 *
 */
abstract class AbstractPathfindingMapInspector<T extends PathfindingMap> extends Inspector {
    private static final long serialVersionUID = 1L;

    private static final int SCROLL_UNIT_INCREMENT = 10;
    private static final String DEFAULT_INFO_FIELD_TEXT = "Click on map to display value.";

    private final T map;
    private final GUIState guiState;
    private double scale = 1;

    private final NumberTextField scaleField = new NumberTextField("  Scale: ", 1.0, true) {
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
    private final JScrollPane scrollPane = new JScrollPane();
    private final DisplayComponent displayComponent = new DisplayComponent();
    private final JTextField infoField = new JTextField();

    /**
     * Instantiates an {@link AbstractPathfindingMapInspector} displaying the
     * given map.
     * 
     * @param state
     * @param map
     */
    public AbstractPathfindingMapInspector(GUIState state, T map) {
	this.map = map;
	this.guiState = state;
	setTitle(map.toString());

	setLayout(new BorderLayout());
	JPanel header = new JPanel(new BorderLayout());
	add(header, BorderLayout.PAGE_START);

	infoField.setEditable(false);
	infoField.setText(DEFAULT_INFO_FIELD_TEXT);
	header.add(infoField, BorderLayout.CENTER);
	scaleField.setToolTipText("Zoom in and out");
	scaleField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
	// allow scale field display at least two numbers
	scaleField.getField().setColumns(2);
	header.add(scaleField, BorderLayout.LINE_END);

	displayComponent.setPreferredSize(new Dimension(map.getWidth(), map.getHeight()));
	scrollPane.setViewportView(displayComponent);
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
    private void setScale(double scale) {
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
     * Returns a portrayal suitable for a specific pathfinding map. Portrayal
     * needs to be created in implementing classes and returned here.
     * 
     * @return the portrayal for the pathfinding map
     */
    protected abstract FieldPortrayal2D getPortrayal();

    /**
     * Returns the String to be displayed within the info text field. Returns
     * toString as default. Override this for a custom representation.
     * 
     * @param wrapper
     *            the location wrapper containing the object
     * @return {@link String} representation for the wrapped object
     */
    protected String getObjectInfo(LocationWrapper wrapper) {
	return wrapper.getObject().toString();
    }

    @Override
    public void updateInspector() {
	getPortrayal().setDirtyField(true);
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
     * Displays portrayal according to scale. Updates info text field on
     * clicking or dragging.
     * 
     * @author mey
     *
     */
    private class DisplayComponent extends JComponent {
	private static final long serialVersionUID = 1L;

	public DisplayComponent() {
	    addMouseListener(new MyMouseListener());
	    addMouseMotionListener(new MyMouseMotionListener());
	}

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	
	    Graphics2D graphics2d = (Graphics2D) g;
	    graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    DrawInfo2D drawInfo = getDrawInfo2D(scrollPane.getViewport().getViewRect());
	    getPortrayal().draw(getPortrayal().getField(), graphics2d, drawInfo);
	}

	/**
	 * Displays object info for given coordinates relative to
	 * {@link #displayComponent}.
	 * 
	 * @param x
	 * @param y
	 */
	private void displayObjectInfo(int x, int y) {
	    // use the mouse pointer's position to create clip rectangle
	    Rectangle2D clip = new Rectangle2D.Double(x, y, 1, 1);
	    Bag objectLocationWrapper = new Bag(1);
	    // get location wrapper for objects at this location
	    getPortrayal().hitObjects(displayComponent.getDrawInfo2D(clip), objectLocationWrapper);
	    if (objectLocationWrapper.size() > 0) {
	        infoField.setText(getObjectInfo((LocationWrapper) objectLocationWrapper.get(0)));
	    } else {
	        infoField.setText(DEFAULT_INFO_FIELD_TEXT);
	    }
	    revalidate();
	}

	/**
	 * @param clip
	 *            the clip rectangle for returned draw info
	 * @return {@link DrawInfo2D} from viewed portion of component
	 */
	public DrawInfo2D getDrawInfo2D(Rectangle2D clip) {
	    Dimension preferredSize = getPreferredSize();
	    Rectangle2D.Double draw = new Rectangle2D.Double(0, 0, preferredSize.getWidth(), preferredSize.getHeight());
	    return new DrawInfo2D(guiState, getPortrayal(), draw, clip);
	}

	private class MyMouseMotionListener implements MouseMotionListener, Serializable {
	    private static final long serialVersionUID = 1L;
	
	    @Override
	    public void mouseDragged(MouseEvent e) {
		displayObjectInfo(e.getX(), e.getY());
	    }
	
	    @Override
	    public void mouseMoved(MouseEvent e) {
	    }
	}

	private class MyMouseListener implements MouseListener, Serializable {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void mouseClicked(MouseEvent e) {
	    }
	
	    @Override
	    public void mousePressed(MouseEvent e) {
		displayObjectInfo(e.getX(), e.getY());
	    }
	
	    @Override
	    public void mouseReleased(MouseEvent e) {
	    }
	
	    @Override
	    public void mouseEntered(MouseEvent e) {
	    }
	
	    @Override
	    public void mouseExited(MouseEvent e) {
	    }
	}
    }
}
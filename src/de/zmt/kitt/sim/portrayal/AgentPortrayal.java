package de.zmt.kitt.sim.portrayal;

import static javax.measure.unit.SI.GRAM;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import sim.display.GUIState;
import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.Double2D;
import de.zmt.kitt.ecs.component.agent.*;
import de.zmt.kitt.sim.params.def.SpeciesDefinition;
import de.zmt.sim.portrayal.inspector.CombinedInspector;
import ec.util.MersenneTwisterFast;
import ecs.*;
import ecs.Component;

/**
 * Portrays agent as a filled oval. When selected, foraging and resting
 * attraction centers are drawn, as well as the agent's position history.
 * 
 * @author cmeyer
 * 
 */
public class AgentPortrayal extends CircledPortrayal2D {
    private static final long serialVersionUID = 1L;

    /** Minimum value in random color generation of a component. */
    private static final int COLOR_MINIMUM = 16;
    /** Range in random color generation of a component. */
    private static final int COLOR_RANGE = 240;

    private static final Color CIRCLE_COLOR = Color.BLACK;

    private static final double DRAW_SCALE_MIN = 8;
    private static final double DRAW_SCALE_MAX = 20;
    private static final double DRAW_SCALE_DEFAULT = 10;
    /** Default value for biomass drawn with {@link #DRAW_SCALE_MIN} */
    private static final double PORTRAYED_DEFAULT_MIN_BIOMASS_G = 10;
    /** Default value for biomass drawn with {@link #DRAW_SCALE_MAX} */
    private static final double PORTRAYED_DEFAULT_MAX_BIOMASS_G = 1000;

    private static final double ATTR_RECT_SIZE = 40;
    private static final double ATTR_RECT_ARC_SIZE = 9;

    /** Component classes to portrayed when agent is inspected */
    private static final Collection<Class<? extends Component>> CLASSES_TO_INSPECT = Arrays
	    .<Class<? extends Component>> asList(Moving.class,
		    Metabolizing.class, Reproducing.class, Aging.class,
		    MassComponent.class, Growing.class, Compartments.class);

    private final MemoryPortrayal memoryPortrayal;
    private final OvalPortrayal2D oval = new OvalPortrayal2D();
    private static final Map<SpeciesDefinition, Color> drawColors = new HashMap<>();

    /** Biomass in g to draw at {@link #DRAW_SCALE_MIN} */
    private final double portrayedMinBiomass_g;
    /**
     * {@link #portrayedMinBiomass_g} + {@link #portrayedRangeBiomass_g} is
     * drawn at {@link #DRAW_SCALE_MAX}
     */
    private final double portrayedRangeBiomass_g;

    private AgentPortrayal(MemoryPortrayal memoryPortrayal,
	    double portrayedMinBiomass_g, double portrayedMaxBiomass_g) {
	super(null);
	super.child = oval;
	this.paint = CIRCLE_COLOR;
	this.memoryPortrayal = memoryPortrayal;
	this.portrayedMinBiomass_g = portrayedMinBiomass_g;
	this.portrayedRangeBiomass_g = portrayedMaxBiomass_g
		- portrayedMinBiomass_g;
    }

    public AgentPortrayal(MemoryPortrayal memoryPortrayal,
	    Amount<Mass> agentMinBiomass, Amount<Mass> agentMaxBiomass) {
	this(memoryPortrayal, agentMinBiomass.doubleValue(GRAM),
		agentMaxBiomass.doubleValue(GRAM));
    }

    public AgentPortrayal(MemoryPortrayal memoryPortrayal) {
	this(memoryPortrayal, PORTRAYED_DEFAULT_MIN_BIOMASS_G,
		PORTRAYED_DEFAULT_MAX_BIOMASS_G);
    }

    @Override
    public void draw(Object object, final Graphics2D graphics,
	    final DrawInfo2D info) {
	Entity entity = (Entity) object;

	determineDrawScale(entity.get(MassComponent.class));

	// get color from map
	Color drawColor = obtainDrawColor(info,
		entity.get(SpeciesDefinition.class));

	// if selected, draw in brighter color
	if (info.selected) {
	    oval.paint = drawColor.brighter();

	    // draw optional attraction centers
	    if (entity.has(AttractionCenters.class)) {
		AttractionCenters centers = entity.get(AttractionCenters.class);
		drawAttractionRect(graphics, info, centers.getForagingCenter(),
			"foraging");
		drawAttractionRect(graphics, info, centers.getRestingCenter(),
			"resting");
	    }
	} else {
	    oval.paint = drawColor;
	}

	// do not scale agent when zooming in
	DrawInfo2D unscaledInfo = new DrawInfo2D(info);
	unscaledInfo.draw.width = 1;
	unscaledInfo.draw.height = 1;

	super.draw(object, graphics, unscaledInfo);
    }

    /**
     * Map biomass to value between {@link #DRAW_SCALE_MIN} and
     * {@link #DRAW_SCALE_MAX} and set circle and oval scale to that value.
     * 
     * @param biomass_g
     */
    private void determineDrawScale(MassComponent massComponent) {
	double drawScale;
	if (massComponent != null) {
	    drawScale = (massComponent.getBiomass().doubleValue(GRAM) - portrayedMinBiomass_g)
		    / portrayedRangeBiomass_g * DRAW_SCALE_MAX + DRAW_SCALE_MIN;

	} else {
	    drawScale = DRAW_SCALE_DEFAULT;
	}

	this.scale = drawScale;
	oval.scale = drawScale;
    }

    /**
     * Obtain draw color associated with species definition from map. If there
     * is none, generate new one and safe it in map.
     * 
     * @param info
     * @param speciesDefinition
     * @return draw color
     */
    private static Color obtainDrawColor(final DrawInfo2D info,
	    SpeciesDefinition speciesDefinition) {
	Color drawColor = drawColors.get(speciesDefinition);
	// otherwise create a random one and store it in the map
	if (drawColor == null) {
	    MersenneTwisterFast guirandom = info.gui.guirandom;
	    int r = generateRandomColorComponent(guirandom);
	    int g = generateRandomColorComponent(guirandom);
	    int b = generateRandomColorComponent(guirandom);
	    drawColor = new Color(r, g, b);
	    drawColors.put(speciesDefinition, drawColor);
	}
	return drawColor;
    }

    /**
     * 
     * @param guirandom
     * @return random color component from {@link #COLOR_MINIMUM} with
     *         {@link #COLOR_RANGE}.
     */
    private static int generateRandomColorComponent(
	    MersenneTwisterFast guirandom) {
	return COLOR_MINIMUM + guirandom.nextInt(COLOR_RANGE);
    }

    /**
     * Draws rounded rectangle for an attraction center.
     * 
     * @param graphics
     * @param attractionCenter
     * @param description
     */
    private void drawAttractionRect(final Graphics2D graphics, DrawInfo2D info,
	    Double2D attractionCenter, String description) {
	// entity did not set given attraction center, draw nothing here
	if (attractionCenter == null) {
	    return;
	}

	double scaleX = info.draw.width;
	double scaleY = info.draw.height;

	double x = (attractionCenter.x - ATTR_RECT_SIZE / 2) * scaleX;
	double y = (attractionCenter.y - ATTR_RECT_SIZE / 2) * scaleY;
	double width = ATTR_RECT_SIZE * scaleX;
	double height = ATTR_RECT_SIZE * scaleX;
	double arcWidth = ATTR_RECT_ARC_SIZE * scaleX;
	double arcHeight = ATTR_RECT_ARC_SIZE * scaleY;
	if (info.precise) {
	    RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width,
		    height, arcWidth, arcHeight);
	    graphics.draw(rect);
	} else {
	    graphics.drawRoundRect((int) x, (int) y, (int) width, (int) height,
		    (int) arcWidth, (int) arcHeight);
	}
	graphics.drawString(description, (int) x, (int) y);
    }

    /**
     * Sets memory portrayal to portray memory of selected entity.
     */
    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
	Entity agent = (Entity) wrapper.getObject();

	if (selected && agent.has(Memorizing.class)) {
	    memoryPortrayal.setPortrayable(agent.get(Memorizing.class)
		    .providePortrayable());
	} else {
	    memoryPortrayal.setPortrayable(null);
	}
	return super.setSelected(wrapper, selected);
    }

    /**
     * @return combined inspector displaying a selection of the agent's
     *         components
     */
    @Override
    public Inspector getInspector(LocationWrapper wrapper, GUIState state) {
	Entity agent = (Entity) wrapper.getObject();
	Collection<Inspector> inspectors = new LinkedList<>();
	for (Component component : agent.get(CLASSES_TO_INSPECT)) {
	    inspectors.add(Inspector.getInspector(component, state, component
		    .getClass().getSimpleName()));
	}

	return new CombinedInspector(inspectors);
    }
}

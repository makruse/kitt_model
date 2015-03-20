package de.zmt.kitt.sim.portrayal;

import static javax.measure.unit.SI.GRAM;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.Double2D;
import de.zmt.kitt.sim.portrayal.MemoryPortrayal.MemoryPortrayable;
import de.zmt.sim.portrayal.portrayable.*;
import ec.util.MersenneTwisterFast;

/**
 * Portrays fish as a filled oval. When selected, foraging and resting
 * attraction centers are drawn, as well as the fish's position history.
 * 
 * @author cmeyer
 * 
 */
public class FishPortrayal extends CircledPortrayal2D {
    private static final long serialVersionUID = 1L;

    /** Minimum value in random color generation of a component. */
    private static final int FISH_COLOR_MINIMUM = 64;
    /** Range in random color generation of a component. */
    private static final int FISH_COLOR_RANGE = 128;

    private static final Color CIRCLE_COLOR = Color.BLACK;
    private static final Color FISH_COLOR_TRAIL = Color.GRAY;

    private static final double FISH_DRAW_SCALE_MIN = 6;
    private static final double FISH_DRAW_SCALE_MAX = 20;
    /** Default value for biomass drawn with {@link #FISH_DRAW_SCALE_MIN} */
    private static final double FISH_DEFAULT_MIN_BIOMASS_G = 10;
    /** Default value for biomass drawn with {@link #FISH_DRAW_SCALE_MAX} */
    private static final double FISH_DEFAULT_MAX_BIOMASS_G = 1000;

    private static final double ATTR_RECT_SIZE = 40;
    private static final double ATTR_RECT_ARC_SIZE = 9;

    private final MemoryPortrayal memoryPortrayal;
    private final OvalPortrayal2D oval = new OvalPortrayal2D();
    private final Map<Integer, Color> drawColors = new HashMap<Integer, Color>();

    /** Biomass in g to draw at {@link #FISH_SCALE_MIN} */
    private final double fishMinBiomass_g;
    /**
     * {@link #fishMinBiomass_g} + {@link #fishRangeBiomass_g} is drawn at
     * {@link #FISH_SCALE_MAX}
     */
    private final double fishRangeBiomass_g;

    private FishPortrayal(MemoryPortrayal memoryPortrayal,
	    double fishMinBiomass_g, double fishMaxBiomass_g) {
	super(null);
	super.child = oval;
	this.paint = CIRCLE_COLOR;
	this.memoryPortrayal = memoryPortrayal;
	this.fishMinBiomass_g = fishMinBiomass_g;
	this.fishRangeBiomass_g = fishMaxBiomass_g - fishMinBiomass_g;
    }

    public FishPortrayal(MemoryPortrayal memoryPortrayal,
	    Amount<Mass> fishMinBiomass, Amount<Mass> fishMaxBiomass) {
	this(memoryPortrayal, fishMinBiomass.doubleValue(GRAM), fishMaxBiomass
		.doubleValue(GRAM));
    }

    public FishPortrayal(MemoryPortrayal memoryPortrayal) {
	this(memoryPortrayal, FISH_DEFAULT_MIN_BIOMASS_G,
		FISH_DEFAULT_MAX_BIOMASS_G);
    }

    @Override
    public final void draw(Object object, final Graphics2D graphics,
	    final DrawInfo2D info) {
	FishPortrayable fishPortrayable = (FishPortrayable) ((ProvidesPortrayable<?>) object)
		.providePortrayable();

	determineDrawScale(fishPortrayable.provideMetabolismPortrayble()
		.getBiomass().doubleValue(GRAM));

	// get color from map
	Color drawColor = obtainDrawColor(info,
		fishPortrayable.getSpeciesHash());

	// if selected, draw in brighter color
	if (info.selected) {
	    drawSelected(graphics, info, fishPortrayable, drawColor);
	} else {
	    oval.paint = drawColor;
	}

	super.draw(object, graphics, info);
    }

    /**
     * Map biomass to value between {@link #FISH_SCALE_MIN} and
     * {@link #FISH_SCALE_MAX} and set circle and oval scale to that value.
     * 
     * @param biomass_g
     */
    private void determineDrawScale(double biomass_g) {
	double drawScale = (biomass_g - fishMinBiomass_g) / fishRangeBiomass_g
		* FISH_DRAW_SCALE_MAX + FISH_DRAW_SCALE_MIN;

	this.scale = drawScale;
	oval.scale = drawScale;
    }

    /**
     * Obtain draw color associated with species definition from map. If there
     * is none, generate new one and safe it in map.
     * 
     * @param info
     * @param speciesHash
     * @return
     */
    private Color obtainDrawColor(final DrawInfo2D info, int speciesHash) {
	Color drawColor = drawColors.get(speciesHash);
	// otherwise create a random one and store it in the map
	if (drawColor == null) {
	    MersenneTwisterFast guirandom = info.gui.guirandom;
	    int r = generateRandomColorComponent(guirandom);
	    int g = generateRandomColorComponent(guirandom);
	    int b = generateRandomColorComponent(guirandom);
	    drawColor = new Color(r, g, b);
	    drawColors.put(speciesHash, drawColor);
	}
	return drawColor;
    }

    /**
     * 
     * @param guirandom
     * @return random color component from {@link #FISH_COLOR_MINIMUM} with
     *         {@link #FISH_COLOR_RANGE}.
     */
    private int generateRandomColorComponent(MersenneTwisterFast guirandom) {
	return FISH_COLOR_MINIMUM + guirandom.nextInt(FISH_COLOR_RANGE);
    }

    /**
     * Draw selected fish in brighter color with position history. If attraction
     * centers are available, these are drawn as well.
     * 
     * @param graphics
     * @param info
     * @param fishPortrayable
     * @param drawColor
     */
    private void drawSelected(final Graphics2D graphics, final DrawInfo2D info,
	    FishPortrayable fishPortrayable, Color drawColor) {
	oval.paint = drawColor.brighter();
	graphics.setPaint(paint);

	drawAttractionRect(graphics, info,
		fishPortrayable.getAttrCenterForaging(), "foraging");
	drawAttractionRect(graphics, info,
		fishPortrayable.getAttrCenterResting(), "resting");
	drawPositionHistory(graphics, info, fishPortrayable.getPosHistory());
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
	// fish did not set given attraction center, draw nothing here
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
     * Draw lines connecting previous locations visited.
     * 
     * @param graphics
     * @param posHistory
     */
    private void drawPositionHistory(final Graphics2D graphics,
	    DrawInfo2D info, Collection<Double2D> posHistory) {
	double scaleX = info.draw.width;
	double scaleY = info.draw.height;

	Double2D previous = null;
	graphics.setColor(FISH_COLOR_TRAIL);

	for (Double2D current : posHistory) {
	    if (previous != null) {

		graphics.drawLine((int) (previous.x * scaleX),
			(int) (previous.y * scaleY),
			(int) (current.x * scaleX), (int) (current.y * scaleY));
	    }
	    previous = current;
	}
    }

    /**
     * Sets memory portrayal to portray memory of selected fish.
     */
    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
	FishPortrayable portrayable = (FishPortrayable) ((ProvidesPortrayable<?>) wrapper
		.getObject()).providePortrayable();

	if (selected) {
	    memoryPortrayal.setPortrayable(portrayable
		    .provideMemoryPortrayable());
	} else {
	    memoryPortrayal.setPortrayable(null);
	}
	return super.setSelected(wrapper, selected);
    }

    public static interface FishPortrayable extends Portrayable {
	int getSpeciesHash();

	Double2D getAttrCenterForaging();

	Double2D getAttrCenterResting();

	Collection<Double2D> getPosHistory();

	MetabolismPortrayable provideMetabolismPortrayble();

	MemoryPortrayable provideMemoryPortrayable();
    }

    public static interface MetabolismPortrayable extends Portrayable {
	Amount<Mass> getBiomass();
    }
}

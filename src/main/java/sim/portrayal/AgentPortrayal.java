package sim.portrayal;

import static javax.measure.unit.SI.GRAM;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import javax.measure.quantity.Mass;

import org.jscience.physics.amount.Amount;

import de.zmt.ecs.Entity;
import de.zmt.ecs.component.agent.Growing;
import de.zmt.ecs.component.agent.LifeCycling;
import de.zmt.ecs.component.agent.LifeCycling.Phase;
import de.zmt.ecs.component.agent.Memorizing;
import de.zmt.ecs.system.agent.move.MoveSystem.MoveMode;
import de.zmt.params.SpeciesDefinition;
import de.zmt.util.ShapeUtil;
import de.zmt.util.UnitConstants;
import ec.util.MersenneTwisterFast;
import sim.portrayal.simple.OrientedPortrayal2D;

/**
 * Portrays agent as a filled oval. When selected, foraging and resting
 * attraction centers are drawn, as well as the agent's position history.
 * 
 * @author mey
 * 
 */
public class AgentPortrayal extends SimplePortrayal2D {
    private static final long serialVersionUID = 1L;

    /** Minimum value in random color generation of a component. */
    private static final int COLOR_MINIMUM = 16;
    /** Range in random color generation of a component. */
    private static final int COLOR_RANGE = 240;

    private static final Color STROKE_COLOR_JUVENILE = Color.ORANGE;
    private static final Color STROKE_COLOR_ADULT_FEMALE = Color.RED;
    private static final Color STROKE_COLOR_ADULT_MALE = Color.BLUE;
    private static final Color DRAW_COLOR_PERCEPTION_RADIUS = Color.GRAY;

    private static final double DRAW_SCALE_MIN = 5;
    private static final double DRAW_SCALE_MAX = 12;
    private static final double DRAW_SCALE_DEFAULT = 10;
    private static final int DRAW_SHAPE = OrientedPortrayal2D.SHAPE_COMPASS;

    /** Default value for biomass drawn with {@link #DRAW_SCALE_MIN} */
    private static final double PORTRAYED_DEFAULT_MIN_BIOMASS_G = 10;
    /** Default value for biomass drawn with {@link #DRAW_SCALE_MAX} */
    private static final double PORTRAYED_DEFAULT_MAX_BIOMASS_G = 1000;

    /** Color for each species */
    private static final Map<SpeciesDefinition, Color> DRAW_COLORS = new HashMap<>();

    private final MemoryPortrayal memoryPortrayal;
    private final OrientedPortrayal2D fill = new OrientedPortrayal2D(new SimplePortrayal2D());
    private final OrientedPortrayal2D stroke = new OrientedPortrayal2D(new SimplePortrayal2D(), STROKE_COLOR_JUVENILE);
    /** Biomass in g to draw at {@link #DRAW_SCALE_MIN} */
    private final double portrayedMinBiomass_g;
    /**
     * {@link #portrayedMinBiomass_g} + {@link #portrayedRangeBiomass_g} is
     * drawn at {@link #DRAW_SCALE_MAX}
     */
    private final double portrayedRangeBiomass_g;

    private AgentPortrayal(MemoryPortrayal memoryPortrayal, double portrayedMinBiomass_g,
            double portrayedMaxBiomass_g) {
        this.memoryPortrayal = memoryPortrayal;
        this.portrayedMinBiomass_g = portrayedMinBiomass_g;
        this.portrayedRangeBiomass_g = portrayedMaxBiomass_g - portrayedMinBiomass_g;

        // use compass shape for drawing agents
        fill.setShape(DRAW_SHAPE);
        stroke.setShape(DRAW_SHAPE);
        stroke.setDrawFilled(false);
    }

    public AgentPortrayal(MemoryPortrayal memoryPortrayal, Amount<Mass> agentMinBiomass, Amount<Mass> agentMaxBiomass) {
        this(memoryPortrayal, agentMinBiomass.doubleValue(GRAM), agentMaxBiomass.doubleValue(GRAM));
    }

    public AgentPortrayal(MemoryPortrayal memoryPortrayal) {
        this(memoryPortrayal, PORTRAYED_DEFAULT_MIN_BIOMASS_G, PORTRAYED_DEFAULT_MAX_BIOMASS_G);
    }

    @Override
    public void draw(Object object, final Graphics2D graphics, final DrawInfo2D info) {
        Entity entity = (Entity) object;
        SpeciesDefinition definition = entity.get(SpeciesDefinition.class);
        LifeCycling lifeCycling = entity.get(LifeCycling.class);

        determineDrawScale(entity);

        // get color from map
        Color fillColor = obtainFillColor(info, definition);

        // if selected, draw in brighter color
        if (info.selected) {
            fill.paint = fillColor.brighter();

            // if move mode is perception: draw perception radius
            if (definition.getMoveMode() == MoveMode.PERCEPTION) {
                double perceptionDiameter = definition.getPerceptionRadius().doubleValue(UnitConstants.WORLD_DISTANCE)
                        * 2;
                drawDistanceCircle(graphics, info, perceptionDiameter, DRAW_COLOR_PERCEPTION_RADIUS);
            }
        } else {
            fill.paint = fillColor;
        }

        // set stroke color
        stroke.paint = lifeCycling.getPhase() == Phase.JUVENILE ? STROKE_COLOR_JUVENILE
                : lifeCycling.isAdultFemale() ? STROKE_COLOR_ADULT_FEMALE : STROKE_COLOR_ADULT_MALE;

        // do not scale agent when zooming in
        DrawInfo2D unscaledInfo = new DrawInfo2D(info);
        unscaledInfo.draw.width = 1;
        unscaledInfo.draw.height = 1;

        fill.draw(object, graphics, unscaledInfo);
        stroke.draw(object, graphics, unscaledInfo);
    }

    /**
     * Map biomass to value between {@link #DRAW_SCALE_MIN} and
     * {@link #DRAW_SCALE_MAX} and set circle and oval scale to that value.
     * 
     * @param entity
     */
    private void determineDrawScale(Entity entity) {
        double drawScale;
        if (entity.has(Growing.class)) {
            drawScale = (entity.get(Growing.class).getBiomass().doubleValue(GRAM) - portrayedMinBiomass_g)
                    / portrayedRangeBiomass_g * DRAW_SCALE_MAX + DRAW_SCALE_MIN;

        } else {
            drawScale = DRAW_SCALE_DEFAULT;
        }

        fill.scale = drawScale;
        stroke.scale = drawScale;
    }

    /**
     * Obtain fill color associated with species definition from map. If there
     * is none, generate new one and safe it in map.
     * 
     * @param info
     * @param speciesDefinition
     * @return draw color
     */
    private static Color obtainFillColor(final DrawInfo2D info, SpeciesDefinition speciesDefinition) {
        Color drawColor = DRAW_COLORS.get(speciesDefinition);
        // otherwise create a random one and store it in the map
        if (drawColor == null) {
            MersenneTwisterFast guirandom = info.gui.guirandom;
            int r = generateRandomColorComponent(guirandom);
            int g = generateRandomColorComponent(guirandom);
            int b = generateRandomColorComponent(guirandom);
            drawColor = new Color(r, g, b);
            DRAW_COLORS.put(speciesDefinition, drawColor);
        }
        return drawColor;
    }

    /**
     * 
     * @param guirandom
     * @return random color component from {@link #COLOR_MINIMUM} with
     *         {@link #COLOR_RANGE}.
     */
    private static int generateRandomColorComponent(MersenneTwisterFast guirandom) {
        return COLOR_MINIMUM + guirandom.nextInt(COLOR_RANGE);
    }

    private static void drawDistanceCircle(Graphics2D graphics, DrawInfo2D info, double diameter, Paint paint) {
        Rectangle2D frame = ShapeUtil.scaleRectangle(info.draw, diameter);

        if (info.precise) {
            Ellipse2D circle = new Ellipse2D.Double();

            circle.setFrame(frame);

            graphics.setPaint(paint);
            graphics.draw(circle);
        } else {
            graphics.setPaint(paint);
            graphics.drawOval((int) frame.getX(), (int) frame.getY(), (int) frame.getWidth(), (int) frame.getHeight());
        }
    }

    /**
     * Sets memory portrayal to portray memory of selected entity.
     */
    @Override
    public boolean setSelected(LocationWrapper wrapper, boolean selected) {
        Entity agent = (Entity) wrapper.getObject();

        if (selected && agent.has(Memorizing.class)) {
            memoryPortrayal.setPortrayable(agent.get(Memorizing.class).providePortrayable());
        } else {
            memoryPortrayal.setPortrayable(null);
        }
        return super.setSelected(wrapper, selected);
    }

    @Override
    public boolean hitObject(Object object, DrawInfo2D range) {
        // use fill for hit detection
        return fill.hitObject(object, range);
    }

}

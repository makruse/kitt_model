package de.zmt.kitt.gui.portrayal;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.logging.*;

import javax.imageio.ImageIO;

import sim.portrayal.*;
import de.zmt.kitt.sim.Sim;

/**
 * Draws habitats in different colors.
 * 
 * @author cmeyer
 * @author oth
 * 
 */
// TODO display actual data loaded from image not the image itself again
public class HabitatMapPortrayal extends FieldPortrayal2D {
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
	    .getLogger(HabitatMapPortrayal.class.getName());

    private static final String MAP_IMAGE_FILENAME = "CoralEyeHabitatMapGUI.jpg";

    private BufferedImage mapImageReady = null;

    /**
     * @param c
     *            defines the color of the agent
     * @param width
     *            defines the radius of the displayed agent
     */
    public HabitatMapPortrayal() {
	super();

	String path = Sim.DEFAULT_INPUT_DIR + MAP_IMAGE_FILENAME;
	BufferedImage mapImage;
	try {
	    mapImage = ImageIO.read(new File(path));
	} catch (IOException e) {
	    logger.log(Level.SEVERE, "Error while loading picture " + path, e);
	    return;
	}

	// TODO do we need this? what needs to be transparent here?
	Image transpImg = TransformColorToTransparency(mapImage, new Color(0,
		0, 0), new Color(0, 0, 0));
	mapImageReady = ImageToBufferedImage(transpImg, mapImage.getWidth(),
		mapImage.getHeight());
    }

    protected Image TransformColorToTransparency(BufferedImage image, Color c1,
	    Color c2) {
	// Primitive test, just an example
	final int r1 = c1.getRed();
	final int g1 = c1.getGreen();
	final int b1 = c1.getBlue();
	final int r2 = c2.getRed();
	final int g2 = c2.getGreen();
	final int b2 = c2.getBlue();
	ImageFilter filter = new RGBImageFilter() {
	    @Override
	    public final int filterRGB(int x, int y, int rgb) {
		int r = (rgb & 0xFF0000) >> 16;
		int g = (rgb & 0xFF00) >> 8;
		int b = rgb & 0xFF;
		if (r >= r1 && r <= r2 && g >= g1 && g <= g2 && b >= b1
			&& b <= b2) {
		    // Set fully transparent but keep color
		    return rgb & 0xFFFFFF;
		}
		return rgb;
	    }
	};

	ImageProducer ip = new FilteredImageSource(image.getSource(), filter);
	return Toolkit.getDefaultToolkit().createImage(ip);
    }

    private BufferedImage ImageToBufferedImage(Image image, int width,
	    int height) {
	BufferedImage dest = new BufferedImage(width, height,
		BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2 = dest.createGraphics();
	g2.drawImage(image, 0, 0, null);
	g2.dispose();
	return dest;
    }

    /**
     * is called by the mason display
     * 
     * @param object
     *            holds a reference to the Agent that has to be drawn
     * @param g
     *            holds the swing Graphics object
     * @param info
     *            holds amongst others the position of the agent
     * @see sim.portrayal.simple.OvalPortrayal2D#draw(java.lang.Object,
     *      java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
     */
    @Override
    public final void draw(Object object, final Graphics2D g,
	    final DrawInfo2D info) {
	int x = (int) info.draw.x;
	int y = (int) info.draw.y;

	g.drawImage(mapImageReady, x, y, null);
    }
}

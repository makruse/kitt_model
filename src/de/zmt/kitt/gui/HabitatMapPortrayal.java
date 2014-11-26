package de.zmt.kitt.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import de.zmt.kitt.sim.Environment;
import de.zmt.kitt.sim.Fish;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.Bag;

public class HabitatMapPortrayal extends SparseGridPortrayal2D implements ImageObserver{

	Environment env;
		
	BufferedImage mapImage=null;
	BufferedImage mapImageReady=null;
	
	/**
	 * @param c defines the color of the agent
	 * @param width defines the radius of the displayed agent
	 */
	public HabitatMapPortrayal( ){
		super( );
			
		try {
//				InputStream in = getClass().getResourceAsStream("habitatmap.png");
//				PNGDecoder decoder = new PNGDecoder(in);
//				mapImage = decoder.decode();
							
				//InputStream in = getClass().getResourceAsStream( "CoralEyeHabitatMapGUI.jpg");
				//JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
				//mapImage = decoder.decodeAsBufferedImage();
				
				mapImage =ImageIO.read(new File( "resource/CoralEyeHabitatMapGUI.jpg"));
				
				Image transpImg = TransformColorToTransparency(mapImage, new Color(0, 0, 0), new Color(0, 0, 0));
				mapImageReady = ImageToBufferedImage(transpImg, mapImage.getWidth(), mapImage.getHeight());
								
				//in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
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
	 * is called by  the mason display
	 * 
	 * @param object holds a reference to the Agent that has to be drawn
	 * @param g holds the swing Graphics object
	 * @param info holds amongst others the position of the agent
	 * @see sim.portrayal.simple.OvalPortrayal2D#draw(java.lang.Object, java.awt.Graphics2D, sim.portrayal.DrawInfo2D)
	 */
	public final void draw(Object object,  final Graphics2D g, final DrawInfo2D info )
	{		
		int x= (int)info.draw.x;
		int y= (int)info.draw.y;
				
	    g.drawImage(mapImageReady, x, y, null);
	    //g.dispose();
	}

		
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return false;
	}
}

package de.zmt.kitt.gui;


import java.awt.*;
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

import de.zmt.kitt.sim.Fish;

import sim.portrayal.simple.*;
import sim.portrayal.DrawInfo2D;

/**
 * draws an agent in the field for the mason display.<br />
 * the agent is visualized by the direction <br />vector of the last movement of the agent <br />
 * and an oval as the head of the agent.
 * 
 * @author oth
 */
public class FishView extends OvalPortrayal2D implements ImageObserver
{
	/**
	 * @uml.property  name="xScale"
	 */
	public double xScale=1.0;
	/**
	 * @uml.property  name="yScale"
	 */
	public double yScale=1.0;
	/**
	 * @uml.property  name="lineFactor"
	 */
	public double lineFactor=1.0;
	/**
	 * @uml.property  name="width"
	 */
	double width;
	BufferedImage[] images =null;
	BufferedImage[] imagesReady=null;
	String[] imagesNames=null;  
	
	/**
	 * @param c defines the color of the agent
	 * @param width defines the radius of the displayed agent
	 */
	public FishView( Color c, double width, int numSpecies){
		super( c);
		this.width=width;
		
		imagesNames= new String[numSpecies];
		images = new BufferedImage[numSpecies];
		imagesReady = new BufferedImage[numSpecies];

		for( int i=0; i< numSpecies; i++){
			
		try {
				//	InputStream in = getClass().getResourceAsStream("barsch_transparent.png");
				//	PNGDecoder decoder = new PNGDecoder(in);
				//	image = decoder.decode();
							
				//InputStream in = getClass().getResourceAsStream( "pic" + i + ".jpg");
				//JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);
				//images[i] = decoder.decodeAsBufferedImage();
				
				images[i] = ImageIO.read(new File( "pic" + i + ".jpg"));
								
				Image transpImg = TransformColorToTransparency(images[i], new Color(0, 0, 0), new Color(0, 0, 0));
				imagesReady[i] = ImageToBufferedImage(transpImg, images[i].getWidth(), images[i].getHeight());
				//in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
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
		
		Fish fish = (Fish)object;
		//super.draw(object, g, info);
		//Color c= new Color(0,0,100 +(a.id*10));
		//g.setColor(c);
		//g.drawOval( (int)a.pos.x,(int)a.pos.y, 3, 5);

// 		Paint the loaded image onto the buffered image
//	    g.drawImage(image, (int)(a.pos.x), (int)(a.pos.y), null);
//      final BufferedImage source = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
//      Graphics2D gSource = (Graphics2D)source.getGraphics();
//      gSource.drawImage( result , x,y , this);	    
		
	    g.drawImage(imagesReady[fish.speciesDefinition.speciesId], x, y, null);
	    //g.dispose();
	}

		
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return false;
	}
}

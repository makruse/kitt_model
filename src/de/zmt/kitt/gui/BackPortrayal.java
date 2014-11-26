package de.zmt.kitt.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;



import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.SparseGridPortrayal2D;

public class BackPortrayal extends SparseGridPortrayal2D implements ImageObserver{

	BufferedImage image = null;	
	/**
	 * @param c defines the color of the agent
	 * @param width defines the radius of the displayed agent
	 */
	public BackPortrayal( ){
		super( );
//		try {
//			//InputStream in = getClass().getResourceAsStream("UkombeReef.JPG");
//			//JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(in);		
//			//image = decoder.decodeAsBufferedImage();
//			//in.close();
//			//"UkombeReef.JPG"
//			//image =ImageIO.read(new File( "resource/UkombeReef.JPG"));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			//e.printStackTrace();
//			return;
//		}
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
		
	    g.drawImage(image, x, y, null);
	}

	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		// TODO Auto-generated method stub
		return false;
	}
	
}

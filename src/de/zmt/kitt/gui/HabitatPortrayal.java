package de.zmt.kitt.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.io.InputStream;


import de.zmt.kitt.sim.Environment;

import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.util.Bag;

public class HabitatPortrayal extends SparseGridPortrayal2D implements ImageObserver{

	Environment env;
		
	static final Color clrCoral= new Color(0,30,190);
	static final Color clrSand= new Color(0,190,70);
	static final Color clrN= new Color(70,70,70);
	
	/**
	 * @param c defines the color of the agent
	 * @param width defines the radius of the displayed agent
	 */
	public HabitatPortrayal( Environment env ){
		super( );
		this.env=env;		
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
		
		if( env == null || env.getHabitatField() == null || env.getFoodField() == null)
			return;
		
		final BufferedImage source = new BufferedImage( (int)env.getFieldSizeX(), (int)env.getFieldSizeY(), BufferedImage.TYPE_INT_RGB);
        Graphics2D gSource = (Graphics2D)source.getGraphics();
		
		for( int yi=0; yi < env.getHabitatFieldSizeY(); yi++){
			for( int xi=0; xi < env.getHabitatFieldSizeX(); xi++){
				int iHabitat=env.getHabitatField().get(xi, yi);
				if( iHabitat==1) 
					gSource.setPaint(clrCoral);
				else if( iHabitat==2) 
					gSource.setPaint( clrSand);
				else if( iHabitat==0) 
					gSource.setPaint(clrN);
				
				//gSource.fillRect(xi*env.getFieldSizeX()/Environment.txtFieldXSize, yi*env.getFieldSizeY()/Environment.txtFieldYSize, 
				//		(xi+1)*env.getFieldSizeX()/(Environment.txtFieldXSize) , (yi+1)*env.getFieldSizeY()/(Environment.txtFieldYSize));
				//gSource.drawRect(xi*env.getFieldSizeX()/Environment.txtFieldXSize, yi*env.getFieldSizeY()/Environment.txtFieldYSize, 
				//		env.getFieldSizeX()/(Environment.txtFieldXSize) , env.getFieldSizeY()/(Environment.txtFieldYSize));
				gSource.drawRect( (int)(xi*env.getFieldSizeX()/env.getHabitatFieldSizeX()), (int)(yi*env.getFieldSizeY()/env.getHabitatFieldSizeY()), 
						(int)env.getFieldSizeX(), (int)env.getFieldSizeY());
			}
		}
        BufferedImage result = new BufferedImage((int)env.getFieldSizeX(), (int)env.getFieldSizeY(), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D gResult = (Graphics2D)result.getGraphics();
        //g.clearRect(0, 0, (int)(width), (int)(height));
	    gResult.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC); //VALUE_INTERPOLATION_BILINEAR
        
	    gResult.drawImage(source, 0, 0, this);
	    gResult.dispose();
	    g.drawImage(result, x,y, this);	 
	}

	@Override
	public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5) {
		
		return false;
	}
}

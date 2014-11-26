package de.zmt.kitt.gui;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.io.InputStream;

import de.zmt.kitt.sim.Fish;
import de.zmt.kitt.sim.Sim;

import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.simple.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.Inspector;
import sim.portrayal.LocationWrapper;
import sim.portrayal.SimpleInspector;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Double2D;



public class FishViewSimple extends OvalPortrayal2D implements ImageObserver
{
	double myScalingX=1.0;
	double myScalingY=1.0;
	Sim sim;
	
	public FishViewSimple( Sim sim){
		super( );
		this.sim=sim;
	}
	
	
	public final void draw(Object object,  final Graphics2D g, final DrawInfo2D info )
	{	
		Fish fish= (Fish) object;		
//		if( fish.speciesDefinition.speciesId==0){
//			g.setPaint( new Color( 90,180,90)); //(int) (3.0 * fish.giveAge() )));
//		}
//		else if( fish.speciesDefinition.speciesId==1){
//			g.setPaint( new Color( 220, 110, 0  ));
//		}

		//int ofsX=(int) info.draw.x;
		//int ofsY=(int) info.draw.y;
				
		if( sim.getIdInFocus()== fish.id){
			this.paint =	new Color( 250,80,80);	
			g.setPaint( new Color( 250,80,80) );
			g.drawRoundRect( (int)fish.centerOfAttrForaging.x-20, (int)fish.centerOfAttrForaging.y-20, 40, 40,9,9);
			g.drawString("feeding", (int)fish.centerOfAttrForaging.x, (int)fish.centerOfAttrForaging.y);
			g.drawRoundRect( (int)fish.centerOfAttrResting.x-20, (int)fish.centerOfAttrResting.y-20, 40, 40,9,9);
			g.drawString("sleeping", (int)fish.centerOfAttrResting.x, (int)fish.centerOfAttrResting.y);
			
			int cnt=0;
			Double2D previous=null;
			for( Double2D vector: fish.history){
				if( previous!=null){
					g.setColor( new Color(250-(cnt*17),90-(cnt*6),90-(cnt*6)));
					
					g.drawLine( (int)((double)previous.x/myScalingX),  (int)((double)previous.y/myScalingY), 
							 (int)((double)vector.x/myScalingX),  (int)((double)vector.y/myScalingY));
				}
				previous= vector;
				cnt++;
			}		
		}	
		else{
			this.paint = new Color( 140,140,140);
		}
		
		info.draw.height=6;
		info.draw.width=9; //fish.giveSize()*0.5;
						
		super.draw(object, g, info);					
		//g.drawOval( (int)fish.pos.x-2, (int)fish.pos.y-2,(int)info.draw.width, (int)info.draw.height);
	}
	
	
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y,
			int width, int height) {
		return false;
	}
	
	/*
	//Return true if the given object, when drawn, intersects with a provided rectangle, 
	public boolean hitObject(java.lang.Object object,  DrawInfo2D range){
		return super.hitObject(object,range);
	}

	public Inspector getInspector(LocationWrapper wrapper, GUIState gui){
		return new SimpleInspector(this, gui);
	}
	//createInspector(...)
	  */
	 
}

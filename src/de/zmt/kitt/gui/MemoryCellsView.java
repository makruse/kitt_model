package de.zmt.kitt.gui;
import sim.portrayal.*;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.util.Bag;
import sim.util.Int2D;

import java.awt.*;
import java.awt.geom.*;
import java.awt.font.*;

import javax.swing.JPanel;

import de.zmt.kitt.sim.Fish;
import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.Sim;
import sim.display.*;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

/** A JPanel that draws the distribution of the Coral species 
 *  for each zone in the field on the screen.  
 *  */

public class MemoryCellsView extends FieldPortrayal2D    //FieldPortrayal2D
{
	Gui ui;
	/** The timer for a periodic display.*/
	Thread displayTimer = null;
    ModelParams cfg;
    double diplayWidth;
	double displayHeight;
	//static BasicStroke dashed;
    
	
    public MemoryCellsView(Gui ui,ModelParams cfg, int width, int height) {     	
    	this.ui = ui; 
    	this.cfg=cfg;
    	diplayWidth=width-32;
    	displayHeight=height-32;
    }

    
    public void setConfig( ModelParams cfg, int width, int height){
    	this.cfg=cfg;
    	diplayWidth=width-32;
    	displayHeight=height-32;
    	
    	//dashed =  new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,  10.0f, null, 0.0f);    	
    }
    
    
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	//super.draw(object, graphics, info);
        
		// Font font = new Font("SansSerif", Font.BOLD, 12);
		// graphics.setFont(font);
    	Sim sim = (Sim)(ui.state);

		double fWidth= cfg.environmentDefinition.xMax;
		double fHeight= cfg.environmentDefinition.yMax;
		
		double scale= fHeight / (double)displayHeight;
    	
		int ofsX=(int) info.draw.x;
		int ofsY=(int) info.draw.y;
		
		Fish fish=sim.getFishInFocus();
		
		if( fish !=null){		
			Color clr= Color.WHITE; //Color.getColor(def.color);
			
			graphics.setColor( clr);
			//graphics.setStroke(dashed);
			
			// set Portrayals to display the agents
			int numCells= (int)cfg.environmentDefinition.memCellsX;
			
			double dsize = fHeight / (double)numCells / scale;

	    	for( int cell=0; cell < numCells; cell++){
	    	   int c = (int)(dsize*(cell+1));	
	    	   graphics.setColor(clr);
			   graphics.drawLine( ofsX+c, 0, ofsX+c, (int) fHeight);
			   graphics.setColor( clr);
			   graphics.drawLine( ofsX, ofsY+c, ofsX + (int) fWidth, ofsY+c);
	    	} 
	    	
	    	for( int cellX=0; cellX < numCells; cellX++){
	      	   int cx = (int)((dsize*(cellX+1))-dsize/2.0);
	      	   for( int cellY=0; cellY < numCells; cellY++){  
	      		   
	      		   Integer num= fish.memField.get(cellX,cellY);
	      		   if( num> 0){
	      			    //System.out.println(num);
		      	   		int cy = (int)((dsize*(cellY+1))-dsize/2.0);
		 		   		 		   		 		  		 		   
		      	   		graphics.setColor( clr);
		      	   		graphics.drawString( String.valueOf(num) , ofsX + cx-5, ofsY +cy+5);
	      		   }
	      	   }
	     	}    
		}
    }
}
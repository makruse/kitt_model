package de.zmt.kitt.gui;

import sim.display.Display2D;
import sim.display.GUIState;
import sim.util.Bag;


public class DaDisplay2D extends Display2D {

	public DaDisplay2D(double width, double height, GUIState simulation) {
		super(width, height, simulation);
		
	}
	
	/**Returns LocationWrappers for all the objects which fall within the coordinate rectangle specified by rect.*/
	public Bag[] objectsHitBy(java.awt.geom.Rectangle2D.Double rect){
		
		rect.height= rect.height +100 ; 
		rect.width= rect.width +100 ; 
		rect.x= rect.x -100 ; 
		rect.y= rect.y -100 ; 
		return super.objectsHitBy(rect);
	}
      
}

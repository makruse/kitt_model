package de.zmt.kitt.gui;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import de.zmt.kitt.sim.params.*;


class SpeciesEdit extends AbstractTableModel {
 
	/** */
	private static final long serialVersionUID = 1L;
	JFrame pFrame = new JFrame();	
	private String[] columnNames= new String[]{ "Name","Value","Comment"};	
	int idx;
	SpeciesDefinition def;
	int numProperties;
	//Object[][] vals = new Object[numProperties][3];
    
    public SpeciesEdit(int idx,SpeciesDefinition def, int numProps){
    	this.idx=idx;
    	this.def=def;
    	this.numProperties= numProps;
    }
     
    public int getColumnCount() {
        return columnNames.length;
    }
   
    public int getRowCount() {   	
		return numProperties;
    }
    
    public String getColumnName(int col) {
        return columnNames[col];
    }

    
    public Object getValueAt(int row, int col) {

    	int curRow=0;
    	try {
       		if( row == curRow++) {
    			if( col==0 ) return "speciesName";
    			else if( col==1) return String.valueOf(def.speciesName);
    			else if( col==2 ) return "name of the defined species";    			
    		}
       		if( row == curRow++ ) {
    			if( col==0 ) return "movementtype";
    			else if( col==1) return String.valueOf(def.activityType);
    			else if( col==2 ) return "kind of movement behaviour";    			
    		}
       		if( row == curRow++ ) {
    			if( col==0 ) return "initialNr";
    			else if( col==1) return String.valueOf(def.initialNr);
    			else if( col==2 ) return "how many individuals should be put at the beginning of the simulation";    			
    		}
       		if( row == curRow++ ) {
    			if( col==0 ) return "maxNr";
    			else if( col==1) return String.valueOf(def.maxNr);
    			else if( col==2 ) return "the capacity of this species alltogether";    			
    		}
			if( row == curRow++ ) {
				if( col==0 ) return "maturitySizeFactor";
				else if( col==1)  return String.valueOf(def.maturitySizeFactor);
				else if( col==2 ) return "maturity in dependency of size ?";    			
			}
//			if( row == curRow++ ) {
//				if( col==0 ) return "perceptionRangeHabitat";
//				else if( col==1)  return String.valueOf(def.perceptionRangeHabitat);
//				else if( col==2 ) return "what is the farest place that can be perceived?";    			
//			}
			if( row == curRow++ ) {
				if( col==0 ) return "initialBiomass";
				else if( col==1)  return String.valueOf(def.initialBiomass);
				else if( col==2 ) return "when born how much of biomass?";    			
			}
			if( row == curRow++ ) {
				if( col==0 ) return "initialSize";
				else if( col==1)  return String.valueOf(def.initialSize);
				else if( col==2 ) return "size when born?";    			
			}
			if( row == curRow++ ) {
				if( col==0 ) return "step";
				else if( col==1)  return String.valueOf(def.stepForaging);
				else if( col==2 ) return "average step size factor for moving?";    			
			}
    	}
    	catch (Exception e) {
			//e.printStackTrace();
			JOptionPane.showMessageDialog(pFrame, "non valid value in data!","Warning",
					JOptionPane.WARNING_MESSAGE);
		}    	
    	return "";
    }


    /** if cell's data has been edited then try to set the cell's value
     * to the new if it is a parseable double */
    synchronized public void setValueAt(Object v, int row, int col) {
        
    	Double val;
    	try {
    		String strV= (String) v;    		
    		val = Double.parseDouble(strV);    		
	    } catch (Exception e) {
			JOptionPane.showMessageDialog(pFrame, "Please put in a valid Value" , "Warning",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		}  
    	int curRow=0;    	
		if( row ==curRow++) {
			if((val > 0)) 
				def.initialNr= val.intValue();
			else
				JOptionPane.showMessageDialog(pFrame, "Please put in a valid Value >0 & integer" , "Warning", JOptionPane.WARNING_MESSAGE);	return;
		}
 		

		fireTableCellUpdated(row, col);
    }  

	public void setConfig(SpeciesDefinition newDefinition) {
		def= newDefinition;	
	} 
	
	


}
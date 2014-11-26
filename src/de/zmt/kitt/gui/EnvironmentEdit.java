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

import de.zmt.kitt.sim.EnvironmentDefinition;
import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.SpeciesDefinition;


class EnvironmentEdit extends AbstractTableModel {
 
	/** */
	private static final long serialVersionUID = 1L;
	JFrame pFrame = new JFrame();	
	private String[] columnNames= new String[]{ "Name","Value","Comment"};	
	EnvironmentDefinition def;
	int numProperties=0;
	Object[][] vals = new Object[numProperties][3];
    
    public EnvironmentEdit(EnvironmentDefinition def, int numProps){
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

    	try {   		
    		if( row == 0 ) {
    			if( col==0 ) return "xMax";
    			else if( col==1) return String.valueOf(def.xMax);
    			else if( col==2 ) return "xMax";    			
    		}
			else if( row == 1 )  {
    			if( col==0 ) return "yMax";
    			else if( col==1) return String.valueOf(def.yMax);
    			else if( col==2 ) return "yMax";    			
			}
			else if( row == 2 ) {
				if( col==0 ) return "timeResolutionMinutes";
				else if( col==1)  return String.valueOf(def.timeResolutionMinutes);
				else if( col==2 ) return "timeResolutionMinutes";    			
			}
			else if( row == 3 ) {
				if( col==0 ) return "simtime";
				else if( col==1)  return String.valueOf(def.simtime);
				else if( col==2 ) return "simtime";    			
			}
			else if( row == 4 ) {
				if( col==0 ) return "drawinterval";
				else if( col==1)  return String.valueOf(def.drawinterval);
				else if( col==2 ) return "drawinterval";    			
			}
			else if( row == 5 ) {
				if( col==0 ) return "rst";
				else if( col==1)  return String.valueOf(def.rst);
				else if( col==2 ) return "rst";
			}
    		   		

    	}
    	catch (Exception e) {
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
    		    	
		if( row ==0) {
			if((val>0))
				def.xMax= val;
			else{
				JOptionPane.showMessageDialog(pFrame, "Please put in a valid Value >0 & integer" , "Warning", JOptionPane.WARNING_MESSAGE);	
				return;
			}
		}
		else if( row ==1)  {
			if((val>0))
				def.yMax = val;
			else{
				JOptionPane.showMessageDialog(pFrame, "Please put in a valid Value >0 & integer" , "Warning", JOptionPane.WARNING_MESSAGE);	
				return;
			}  
		}
    }  

	public void setEnvironment(EnvironmentDefinition cenvironment) {
		def= cenvironment;		
	} 
}
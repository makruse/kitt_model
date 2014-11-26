package de.zmt.kitt.sim;

import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Enumeration;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;

public class OutputStepper implements Steppable{

	private static final long serialVersionUID = 1L;
	/** */
	ModelParams cfg;
	/** */
	CsvWriter csv;
	DecimalFormat fmt= new DecimalFormat("#.##");
	/** */
	boolean fileIsOpen=false;
	
	
	public OutputStepper(ModelParams cfg){
		this.cfg=cfg;
	}

	public synchronized void prepareFile() throws Exception{		
		// set outputpath to configfilename without extension + csv
		String outPath = new String(cfg.currentPath.substring(0,cfg.currentPath.length()-4));
		String strCount="";
		int count=1;
		// if outputfile with current name exist, add count to the path
		while( new File(outPath + "_total" + strCount + ".csv").exists()){
			strCount= "." + String.format("%02d", count);
			count++;
		}
		
		// create output file
		csv = new CsvWriter(outPath + "_total" + strCount +".csv");
		// create header of outputfile
		csv.append("steps:");csv.append("total:");csv.append("SpeciesA:");csv.append("SpeciesB:");
		csv.newLine();		
	}
	
	
	public synchronized void closeFile() throws Exception{	
		csv.close();
	}
	
	
	@Override
	public void step(SimState state)  {

		Sim sim = (Sim)state;
		long steps = sim.schedule.getSteps();
				
		double t = state.schedule.time();
		try{
			if (t <= Schedule.EPOCH ){
				prepareFile();				
			}
			if (steps % cfg.environmentDefinition.drawinterval == 0 ){
				
				writeData(steps,sim);
			}
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
	}

	
	public void writeData(long steps, Sim sim) throws Exception{
		
		Environment env= sim.environment;
		
		csv.append(String.valueOf(steps));
//		Enumeration<Integer> popEnum = env.populations.elements();
//		while(popEnum.hasMoreElements()){			
//			csv.append(String.valueOf( env.getPopulation(popEnum.nextElement())));
//		}
		csv.newLine();
		
	}	
}

package de.zmt.kitt.sim;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.zmt.kitt.sim.ModelParams;
import de.zmt.kitt.sim.OutputStepper;
import de.zmt.kitt.sim.Sim;
import ec.util.MersenneTwisterFast;
import sim.engine.*;
import sim.util.Bag;


/**
 	main class for running the simulation without gui
 */
public class Sim extends SimState
{
	/* the environment of the simulation, contains also the fields */
	public Environment environment;
	public ModelParams params;
	public MersenneTwisterFast rand;
	
	protected long fishInFocus=5;
	
	synchronized public void setIdInFocus(long id){
		fishInFocus=id;
	}
	synchronized public long getIdInFocus() {
		return fishInFocus;
	}
	
	public Fish getFishInFocus(){
		if( environment != null && environment.field != null){
			Bag bag= environment.field.getAllObjects();
			for( Object o:bag){
				Fish f= (Fish)o;
				if( f.getId() == fishInFocus){
					return f;
				}
			}			
		}
		return null;
	}
	
	public double getTimeResolutionInMinutes() {
		return params.environmentDefinition.timeResolutionMinutes;
	}
		
	/**
	 * @param path the path to the configuration file that initializes the model
	 * constructs the simulation. superclass is first initialized with seed 0.
	 * afterwards when running simulation it gets the seed from the config file.
	 */
	public Sim(String path) {
		super(0);
		try {			
			params = new ModelParams(path);			
			rand= new MersenneTwisterFast(params.environmentDefinition.rst);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return;
		}
		environment= new Environment(this,rand,params);
	}

	/**
	 * starts the simulation.
	 * for each run the model input parameters are set by the last set configuration file.
	 * the field is initialized and then seeded by a given number of agents
	 * to a random position. Habitats are put into the field, if specified and activated in configuration.
	 * @see sim.engine.SimState#start()
	 */
	public void start() {
				
		super.start();	
				
		rand.setSeed(params.environmentDefinition.rst);	
		environment.initPlayground();
	}
		
	
	/** is called when the simulation has finished */
	@Override
    public void finish()
    {
    	super.finish(); // cleans up and resets the schedule    
    	
    	fishInFocus = fishInFocus; //+ Fish.currentIdAssignment - 1;
    }

	


	public void reload(String configPath){
		
//		String path=null;
//		if(configPath!=null)
//			path=configPath;
//		else 
//			path=cfg.currentPath;
//		
//		try {
//			cfg = new Config(path);
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			return;
//		}		
//		grid= new ObjectGrid2D( cfg.env.xCells, cfg.env.yCells);
//		
//		// reset populations of different Coraltypes for the next run
//		for( int i= 0; i<4 ; i++) populations[i]=0;
//		zonePopulations=new int[cfg.env.numZones][4];
//		for( int i= 0; i< cfg.env.numZones ; i++) for( int j=0; j<4 ; j++)
//			zonePopulations[i][j]=0;
//		
//		this.random.setSeed(cfg.env.rst);
//		rand= new Random(cfg.env.rst);
//				
//		if( cfg.env.neighborhood == 8){
//			ca = new CACoralEight(this,cfg);
//		}
//		if( cfg.env.neighborhood == 6){
//			ca = new CACoralSix(this,cfg);
//		}
//		else{
//			ca = new CACoral(this,cfg);
//		}
//		ca.initialize();		
	}

	
	/**
	 * @return the full qualified path to the current directory
	 * @throws IOException
	 */
	public static String getWrkDir() throws IOException{
				
		try {
			String wrkDir = new java.io.File( "" ).getCanonicalPath() +"/";
			return wrkDir;
		} catch (IOException e) {
			e.printStackTrace();
			throw(e);
		}			
	}
	
	/**
	 * @return the path to the Main class
	 * @throws ClassNotFoundException
	 * get the classdir in which resources might reside
	 */
	public static String getClassDir() throws ClassNotFoundException{
		String clssDir;
		try {
			clssDir = Class.forName(Sim.class.getCanonicalName()).getResource("Sim.class").getPath().replace("Sim.class", "");
			return clssDir;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw(e);
		}
	}
	
	
	/**
	 * @param args name of the chosen configfile- permitted "config01.xml" to "config08.xml"
	 * @return full qualified path to the configuration file
	 * @throws Exception
	 * if no arguments are supplied. it returns the path to current directory + the default config01.xml
	 */
	public static String getConfigPath(String[] args) throws Exception{	

		String path=null;
		// if no filename argument is given, take default config config01.xml
		String fileName="modelparams.xml";
		if (args.length > 0) {
			if (args[0].length()>0) {
				fileName=new String( args[0]);
			}
		}
		try {
			path=getWrkDir() + fileName;
			if( ! (new File(path).exists())) {
				//System.out.println("info: not found localfile " + path);
				throw new Exception();
			}
		} catch (Exception e) {
			try {
				path=getClassDir() + fileName;
				if( ! (new File(path).exists())) {
					//System.out.println("info: not found in output path " + path);
					throw new Exception();
				}
			} 
			catch (Exception e1) {
				//Itn.class.getResourceAsStream(fileName));
				if( ! (new File(path).exists())) throw new Exception( "could not find: " +path);
			}
		}	
		System.out.println("initial modelparams file: " + path);
		return path;
	}
	
		
	/**
	 * run one simulation with the given configuration-file path
	 * Sim searches first in the current local path for the configfile,
	 * if not found it searches in the class path of ItnClass
	 */
	public static void runSimulation(String configPath){
		
		long t1 = System.nanoTime();
		Sim sim = new Sim(configPath);
		
		// set outputpath to configfilename without extension + csv
		String outPath = new String(configPath.substring(0,configPath.length()-4));
		int count=1;
		// if outputfile with current name exist, add count to the path
		while( new File(outPath + "." + count + ".csv").exists()){
			count++;
		}		
		long steps=0;
		
		OutputStepper outputStepper;
		try {
			// create output file(s)
			System.out.println(sim.params);
			outputStepper= new OutputStepper(sim.params);		
			outputStepper.prepareFile();	
			
			// run the simulation
			sim.start();	        
	        do{
	            if (!sim.schedule.step(sim))
	                break;
	            
	            steps = sim.schedule.getSteps();
	            // write current populations to outputfile(s)
	            outputStepper.writeData(steps, sim);
	        // if the number of steps exceeds maximum then finish
	        }while(steps < sim.params.environmentDefinition.simtime);
	        outputStepper.closeFile();
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		sim.finish();
		long t2 = System.nanoTime();
		Date d = new Date((t2 - t1)/1000000);
		SimpleDateFormat df= new SimpleDateFormat("mm:ss");
        System.out.println("Simulation finished with "+ steps + " steps in " + df.format(d) + " (min:sec) \noutput written to " + outPath  + ".csv");
	}	
	
	
	/**
	 * @param args 
	 * 	
	 *	 filename of the local configuration file 
	 *	 ( if not found, default configuration file config01.xml 
	 */
	public static void main(String[] args) {
		
//		String path="";
//		try {
//			path = Sim.getConfigPath(args);
//		} catch (Exception e) {
//			System.out.println(e.getMessage());
//			System.exit(1);
//		}
		
		
		runSimulation("resource/ModelParams.xml");
        
		System.exit(0);
	}


}

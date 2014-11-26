package de.zmt.kitt.sim;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ec.util.MersenneTwisterFast;
import flanagan.analysis.Regression;
import flanagan.interpolation.CubicSpline;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.continuous.Continuous3D;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int2D;


abstract public class EnvironmentBase{
		
	protected Continuous2D field;
	protected IntGrid2D habitatField;
	protected DoubleGrid2D foodField;
			
	protected MersenneTwisterFast rand;
	//protected Sim sim;
	protected ModelParams params;
	
	
	public EnvironmentBase(MersenneTwisterFast rand,ModelParams params){
		this.rand=rand;	
		this.params= params;
	}
		
	public void initPlayground() {
		
		//readHabitatDataFromText("reef1.txt");
		
		double bucketSize=10; //sim.cfg.environmentDefinition.xMax / habitatField.getWidth();
		// initialize foodgrid according to habitattype in input map
		MapGenerator mapGen= new MapGenerator();
		//System.out.println("width:" + mapGen.getMapWidth());
		field=new Continuous2D(bucketSize, mapGen.getMapWidth(), mapGen.getMapHeight());
		
		//foodField=new DoubleGrid2D(mapGen.getMapWidth(), mapGen.getMapHeight());
		foodField=new DoubleGrid2D( (int)(getFieldSizeX()/getFoodCellSizeX()),
 				(int)(getFieldSizeY()/getFoodCellSizeY()));
		
		habitatField=new IntGrid2D(mapGen.getMapWidth(), mapGen.getMapHeight());
		mapGen.createFieldsByMap(foodField, habitatField, this);
		//field = new Continuous2D( bucketSize, sim.cfg.environmentDefinition.xMax, sim.cfg.environmentDefinition.yMax);
		//foodField = new DoubleGrid2D ( (int)sim.cfg.environmentDefinition.xMax, (int)sim.cfg.environmentDefinition.yMax);
		//habitatField = new IntGrid2D ( (int)sim.cfg.environmentDefinition.xMax, (int)sim.cfg.environmentDefinition.yMax );
		// reset any population count

		double x = 0.0, y = 0.0, z = 0.0;
						
		// Seeding the fishes		
		for( SpeciesDefinition speciesDefinition: params.speciesList){
			for (int i = 0; i < speciesDefinition.initialNr; i++) {
				x = rand.nextDouble() * (getFieldSizeX());
				y = rand.nextDouble() * (getFieldSizeY());		
				z = 0.0;
								
				Fish fish = new Fish(x, y, z, speciesDefinition.initialBiomass, speciesDefinition.initialSize, this, params, speciesDefinition);
									
				// schedule agents for the first possible time in a random order from 0 to 100 						
				schedule(fish);
			}
		}
			
		scheduleEnvironment();
		//System.out.println(getFieldSizeX());
		System.out.println("creating fields finished");
	}

	
	
	public long getTimeRes(){
		return params.environmentDefinition.timeResolutionMinutes;
	}
		
	public long getDielCycle(){
		long numberOfCurrentSteps = getCurrentTimestep();		
		long minutesAlltogether= numberOfCurrentSteps * getTimeRes();		
		long minutesPerDay= 24*60*60;
		long day= minutesAlltogether / minutesPerDay;
		return day+1;
	}

	
	public long getDay(){
		long numberOfCurrentSteps = getCurrentTimestep();		
		long minutesAlltogether= numberOfCurrentSteps * getTimeRes();		
		long minutesPerDay= 24*60*60;
		long day= minutesAlltogether / minutesPerDay;
		return day+1;
	}

	public long getHourOfDay(){
		
		long allHours = getCurrentTimestep() * getTimeRes()/60;
		return allHours % 24;
	}

	
	public long getDayTimeInMinutes(){
		long numberOfCurrentSteps = getCurrentTimestep();		
		long minutesAlltogether= numberOfCurrentSteps * getTimeRes();		
		long minutesPerDay= 24*60*60;
		long dayTimeInMinutes= minutesAlltogether % minutesPerDay;
		return dayTimeInMinutes;
	}

	
	public int getHabitatOnPosition( Double2D position ){
		
		
		int habitatX= (int)(position.x * habitatField.getWidth()/field.getWidth() );
		int habitatY= (int)(position.y * habitatField.getHeight()/field.getHeight());
		if( habitatX >= habitatField.getWidth()) habitatX=habitatField.getWidth()-1;
		if( habitatX < 0 ) habitatX=0;
		if( habitatY > habitatField.getHeight()) habitatY=habitatField.getHeight()-1;
		if( habitatY < 0) habitatY=0;
		int habitatType = habitatField.get(habitatX,habitatY);
		return habitatType;
	}
	
	public Int2D getMemFieldCell(Double3D pos){
		
		double cellX = pos.x/getMemCellSizeX();
		double cellY = pos.y/getMemCellSizeY();
		if( cellX >= getMemCellSizeX())
			cellX = getMemCellSizeX()-1;
		if( cellY >= getMemCellSizeY())
			cellY = getMemCellSizeY()-1;
		
		return new Int2D((int)( cellX),(int)( cellY));
	}
	
	
	public double getFoodOnPosition(Double3D pos){
		
		double cellX = pos.x/getFoodCellSizeX();
		double cellY = pos.y/getFoodCellSizeY();
		if( cellX >= getFoodCellSizeX())
			cellX = getFoodCellSizeX()-1;
		if( cellY >= getFoodCellSizeY())
			cellY = getFoodCellSizeY()-1;
		
		return foodField.get( (int) cellX, (int) cellY);
	}
	
	
	public double getFoodOnPosition( Double2D position ) {
		int foodX=(int)(position.x/foodField.getWidth());	
		int foodY=(int)(position.y/foodField.getHeight());
		return foodField.get(foodX,foodY);
	}
	
	
	public Double2D getRandomFieldPosition(){		
		double x = rand.nextDouble() * (getFieldSizeX());
		double y = rand.nextDouble() * (getFieldSizeY());	
		return new Double2D(x,y);
	}
	

	
		
	public List<Double2D> getCentersOfAttraction( int species ){
		List<Double2D> centers= new ArrayList<Double2D>();
		double scalingX= getFieldSizeX()/getHabitatFieldSizeX();
		double scalingY= getFieldSizeY()/getHabitatFieldSizeY();
		
		// define coral reef center hardcoded
		centers.add(new Double2D( 20*scalingX, 12*scalingY ));
		
		return centers;
	}
	
	
	public double getFieldSizeX() {
		return field.getWidth();
	}
	public double getFieldSizeY() {
		return field.getHeight();
	}
	public int getHabitatFieldSizeX() {
		return habitatField.getWidth();
	}
	public int getHabitatFieldSizeY() {
		return habitatField.getHeight();
	}
	public int getFoodFieldSizeX() {
		return foodField.getWidth();
	}
	public int getFoodFieldSizeY() {
		return foodField.getHeight();
	}

	public IntGrid2D getHabitatField() {
		return habitatField;
	}	
	public Continuous2D getField() {
		return field;
	}
	public DoubleGrid2D getFoodField() {
		return foodField;
	}
	public double getMemCellSizeX(){
		return params.environmentDefinition.memCellSizeX;
	}
	public double getMemCellSizeY(){
		return params.environmentDefinition.memCellSizeY;
	}
	public int getFoodCellSizeY() {
		return params.environmentDefinition.foodCellSizeX;
	}
	private int getFoodCellSizeX() {
		return params.environmentDefinition.foodCellSizeY;
	}

}

package de.zmt.kitt.sim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.vecmath.Vector3d;

import flanagan.interpolation.CubicSpline;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.IntGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int2D;

/**
 * superclass for all movable Agents in the simulation field.<br />
 * implements the step method of the Mason-Steppable.<br />
 * The step method is called in each time-step of a running simulation.<br />
 * for a different behavior of the agent it has to be overridden.<br />
 * The moving method is implemented for all species, for a different movement<br />
 * override this method.
 * 
 * @author oth
 */
public class Fish extends Agent{
	
	/** reference to agent specification - which species */
	public final SpeciesDefinition speciesDefinition;
	
	public class MemEntry{		
		public Double2D pos;
		public double food;
		public long timeStep;
		
		public MemEntry( Double2D pos, double food, long timeStep){
			this.pos= pos;
			this.food=food;
			this.timeStep= timeStep;
		}
	}
	
	IntGrid2D memField;
	
	/** holds the latest locations of feeding places with food */
	public Queue<MemEntry> memory= new LinkedList<MemEntry>();
	public final int memSize=200;
	
	/** references to the environment to have access e.g. to the field */
	
	// GROWTH AND AGE	
	/** current biomass of the fish (g wet weight) */
	public double biomass;
	/** store weekly biomass update (g wet weight) */
	protected double oldBiomassWeekly=0;	
	/** current size of fish as standard length (cm) */
	protected double size;
	/** calculated age (years) depending on actual energy intake based on vBGF (daily update in the evening) */
	protected double virtualAgeDifference=0;
	/** timestep of fish initialisation for age calculation */
	protected double birthTimeStep;

	// BODY COMPARTMENTS
	/** holds the latest incomes of food energy in gut (kJ) */
	protected Queue<Double> gutStorageQueue = new LinkedList<Double>() ;
	/** shortterm storage of energy (kJ) */
	protected double shorttermStorage=0.0;
	/** energy in gut (kJ)*/
	protected double currentGutContent=0;
	/** current amount of body bodyFat of fish (kJ) */ 
	protected double bodyFat=0;
	/** current amount of body tissue (kJ) */
	protected double bodyTissue=0;
	/** current amount of repro fraction (kJ) */
	protected double reproFraction=0;
	/** min capacity of repro fraction for spawning: 10% of total body weight */
	protected double minReproFraction=0.1; 
	/** max capacity of repro fraction: 30% of total body weight */
	protected double maxReproFraction=0.3; 
	
	// ENERGY METABOLISM
	/** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
	static public final double restingMetabolicRateA=0.0072;
	/** (RMR in mmol O2/h)=A*(g fish wet weight)^B */
	static public final double restingMetabolicRateB=0.79;
	/** Verlustfaktor bei flow shortterm storage <=> bodyFat */
	static public final double VerlustfaktorFatToEnergy=0.87; 
	/** Verlustfaktor bei flow shortterm storage <=> bodyTissue */
	static public final double VerlustfaktorTissueToEnergy=0.90;
	/** Verlustfaktor bei flow shortterm storage <=> reproFraction */
	static public final double VerlustfaktorReproToEnergy=0.87; 
	// energy-biomass conversions
	/** metabolizable energy (kJ) from 1 g bodyFat */
	static public final double energyPerGramFat=36.3; 
	/** metabolizable energy (kJ) from 1 g bodyTissue */
	static public final double energyPerGramTissue=6.5;  
	/** metabolizable energy (kJ) from 1 g reproFraction (ovaries) */
	static public final double energyPerGramRepro=23.5; 
	/** 1 kJ fat*conversionRate = fat in g */
	static public final double conversionRateFat=0.028; 
	/** 1 kJ bodyTissue*conversionRate = body tissue in g */
	static public final double conversionRateTissue=0.154;
	/** 1 kJ repro*conversionRate = repro in g */
	static public final double conversionRateRepro=0.043;
	
	// FEEDING AND ENERGY BUDGET
	/** g dry weight food/m2 */
	protected double availableFood=0;
	/** g dry weight food per fish */
	protected double foodIntake=0;
	/** energy intake (kJ) */ 
	protected double energyIntake=0;
	/** energy consumption at zero speed (kJ) */
	protected double restingMetabolicRatePerTimestep=0;
	/** energy demand of fish (difference between expectedE and currentE at age in kJ) */
	protected double hunger=0;
	/** hunger state of the fish */ 
	protected boolean isHungry=true;
	/** ueber den tag aufsummierter energy intake (kJ) */
	protected double intakeForCurrentDay=0;
	/** current energy value of fish (all body compartments) in kJ */
	protected double currentEnergyTotal=0;
	/** current energy value of fish (all except Repro) in kJ */
	protected double currentEnergyWithoutRepro=0;
	/** expected energy value of fish based on vBGF in kJ */
	protected double expectedEnergyWithoutRepro=0;
	
	// MOVING
	/** center of habitat-dependent foraging area */
	public Double2D centerOfAttrForaging=new Double2D( ) ;
	/** center of habitat-dependent resting area */
	public Double2D centerOfAttrResting= new Double2D( );
	/** steplength in cm per time step */
	double step=0; //check unit!
	/** holds the current factor for turning angle depending on the last action decision */
	protected double turningFactor; //wozu?
	/** holds the current factor for steplength depending on the last action decision */
	protected double stepLengthFactor;	//wozu?
	/** mode of moving (resting, foraging or migrating) */
	protected MoveMode currentMoveMode;
	/** current speed of agent in x- and y-direction (unit?) */
	protected double xSpeed,ySpeed,zSpeed; //unit?
	/** current speed in cm per timestep */
	protected double currentSpeed=0.0; //stimmt unit?
	/** net activity costs (kJ) for current timestep */
	protected double netActivityCosts=0.0;
	
	// REPRODUCTION
	/** each fish starts as a post-settlement juvenile */
	// nicht besonders clever, typical pop sturcture sollte start sein!!
	// dann auch initSize/biomass und lifestage anpassen!!
	/** life stage of fish (juvenile, female or male) */
	protected LifeStage lifeStage= LifeStage.JUVENILE; 
	
	/** references to the environment to have access e.g. to the field */
	protected final ModelParams params;
	protected final Environment environment;
	/**
	 * @param x initial x Position
	 * @param y initial y Position
	 * @param b initial biomass
	 * @param environment in which agent runs
	 */
	public Fish(final double x, final double y, final double z, double initialBiomass, double initialSize, EnvironmentBase environment,ModelParams params, SpeciesDefinition speciesDefinition)
	{	
		super(new Double3D(x,y,z));
		/** references to the environment to have access e.g. to the field */
		this.environment=(Environment)environment;
		this.params=params;

		this.id=currentIdAssignment++;
		this.pos=new Double3D(x, y, z);
		this.oldpos=pos;
		
		this.biomass=initialBiomass;
		this.oldBiomassWeekly=biomass;
		this.size= initialSize;
		
		// allocating initialBiomass to body compartments as E values (kJ)
		// wenn auch female fehlt noch reproFraction!!
		this.bodyFat=biomass*0.05*energyPerGramFat;
		this.bodyTissue=biomass*0.95*energyPerGramTissue;	
		this.size=speciesDefinition.initialSize;
		
		this.currentMoveMode=MoveMode.FORAGING;
		this.speciesDefinition=speciesDefinition;
		this.birthTimeStep=getSteps();
		
		
		initCentersOfAttraction();
		
 		memField=new IntGrid2D( (int)(environment.getFieldSizeX()/environment.getMemCellSizeX()),
 				(int)(environment.getFieldSizeY()/environment.getMemCellSizeY()));
		
 		
 		birth();
		System.out.print(bodyFat);
		System.out.println();
		System.out.print(bodyTissue);
		System.out.println();
		System.out.print(reproFraction);
		System.out.println();
	} 
	
	/** 
	 * is called by the first step() call 
	 * initializes the agent and schedules agent for lifeloop
	 */
	public void birth( ){	
		
		environment.changePopulation(speciesDefinition.speciesId,+1);
		this.born=true;
		this.alive=true;	
		// put agent into the environment field
		environment.field.setObjectLocation(this, new Double2D(pos.x, pos.y));
		
		// schedule agent for the next time slot when all agents are initialized
		// reiht event in warteschlange ein 
		// (bei scheduleRepeated=immer gleiche reihenfolgebei scheduleFirst=zufällig??
		environment.schedule(this);
	}
	
	/**
	 * is called to take agent from lifeloop
	 * agent is taken from the 'simulation world' and not scheduled again
	 * due to the architecture of the scheduler remaining schedules of this agent can't be deleted immediately 
	 * from the schedule. hence some empty step calls might occur.
	 */	
	public void die(){ 
		alive=false;
		environment.field.remove(this);
		environment.changePopulation(speciesDefinition.speciesId,-1);
	}
	
	/////////////////LIFE LOOP////////////////////////////////////////////////////////
	 /**
	 * called every time when its taken from queue in the scheduler
	 * lifeloop of the agent
	 */
	@Override
	//konvention von MASON bei step-methode=basisklasse als übergabe-parameter
	public void step(final SimState state)
	{	
		Sim sim=(Sim) state;
		
		if(alive==true){					   
			try{
				move();
			}
			catch(Exception e){
				e.printStackTrace();
				System.out.println("Tageszeit nicht vorhanden");
			}
			
			biomass=biomass+ 0.2 * biomass; //wo kommt das her?
			if(isHungry==true){ 
				// CONSUMPTION (C)
				feed();	
			}
		
			//ENERGY BUDGET (RESPIRATION, R)
			//if(updateEnergy()==false){
			//	System.out.println(this + "died");
			//}
			
			System.out.println(state.schedule.getSteps());
			// DAILY UPDATES:
			if(state.schedule.getSteps() % (60/getTimeResInMinutes()*24) == 0) {
				
				// PRODUCTION I (growth): update of BIOMASS (g wet weight) + weekly update of SIZE (cm);
				grow();	
				
				// Change of lifeStage if size appropriate
				double currentSize=giveSize();
//				// size frame = variation? wie macht man das mit 50% levels?
// 				double sizeFrame=0.3*environment.rand.nextGaussian(); //warum 0.3??
// 				if((currentSize > (speciesDefinition.reproSize-sizeFrame)) || (currentSize < (speciesDefinition.reproSize+sizeFrame))) { 
// 					// has a probability at this point of 1/(365days/7days) ???
//  					if(environment.rand.nextDouble() > (1.0/(365.0/7.0))) {					
//						lifeStage=LifeStage.FEMALE;
//					}
// 				}
				// aber nur zu 50% !!
				if(currentSize > (speciesDefinition.reproSize)) { 
						lifeStage=LifeStage.FEMALE;
				}
				
				
 				//PRODUCTION II (reproduction) 
 				// spawn if lifeStage=female && repro zw. 20-30% of biomass && currentEohneRepro >= 75% of expectedEohneRepro at age (nach Wootton 1985)
 				// C. sordidus spawns on a daily basis without clear seasonal pattern (McILwain 2009)
				// HINZUFÜGEN: Wahrscheinlichkeit von nur 5-10%, damit nicht alle bei genau 20% reproduzieren, aber innerhalb der nächsten Tagen wenn über 20%
 				if(lifeStage==LifeStage.FEMALE && (reproFraction >= (biomass*0.2*energyPerGramRepro))) {						
 					reproduce();				
 				}
					
 				//zuruecksetzen von intakeForDay 
 	 			intakeForCurrentDay=0;
			
			}
							
			// reschedule agent for the next step call
			environment.schedule(this);		
			
		}			
		else if(born==false) {
			birth();
			return;
		}		
	}

	
/////////////////MOVEMENT////////////////////////////////////////	
//	protected MoveMode determineMoveMode(){
//		MoveMode mm= MoveMode.FORAGING;
//		
//		//...
//		
//		return mm;
//	}

	
//	protected Double3D getPositionCandidate(){
//		
//    	double x=pos.x, y=pos.y, z=pos.z;
//    	
//    	double gx=environment.rand.nextGaussian();
//		double gy=environment.rand.nextGaussian();
//							
//		xSpeed = ( xSpeed * turningFactor) + speciesDefinition.step * stepLengthFactor * gx;  // ipp.rand.normal(0.0, cfg.step);
//		ySpeed = ( ySpeed * turningFactor) + speciesDefinition.step * stepLengthFactor * gy;
//		if (xSpeed > speciesDefinition.xSpeedMax) {
//			xSpeed = speciesDefinition.xSpeedMax;
//		}
//		else if( xSpeed <  -speciesDefinition.xSpeedMax ){
//			xSpeed = -speciesDefinition.xSpeedMax;
//		}
//		if (ySpeed > speciesDefinition.ySpeedMax) {
//			ySpeed = speciesDefinition.ySpeedMax;
//		}
//		else if( ySpeed <  -speciesDefinition.ySpeedMax ){
//			ySpeed = -speciesDefinition.ySpeedMax;
//		}
//
//		dx= xSpeed;
//		dy= ySpeed;
//		
//		// modify coordinates in torroidal way 
//		x = ( x + xSpeed + environment.getFieldSizeX()) % environment.getFieldSizeX();
//        y = ( y + ySpeed + environment.getFieldSizeY()) % environment.getFieldSizeY();
//
//        // update pos attribute to the new position	        
//        Double3D posCandidate= new Double3D(x, y,0);
//        
//        return posCandidate;
//    }
    
    
//    // throw a number of positions based on moveMode
//	// take the first alternative that fits with a probability greater than 0.7
//    protected Double3D findSuitablePosition( MoveMode moveMode, int numAlternatives){
//    	
//	    int count=0;
//
//	    Double3D chosenPos=null;
//		while( count < nu//    Source:
//	Almanac for Computers, 1990
//	published by Nautical Almanac Office
//	United States Naval Observatory
//	Washington, DC 20392
//
//Inputs:
//	day, month, year:      date of sunrise/sunset
//	latitude, longitude:   location for sunrise/sunset
//	zenith:                Sun's zenith for sunrise/sunset
//	  offical      = 90 degrees 50'
//	  civil        = 96 degrees
//	  nautical     = 102 degrees
//	  astronomical = 108 degrees
//	
//	NOTE: longitude is positive for East and negative for West
//        NOTE: the algorithm assumes the use of a calculator with the
//        trig functions in "degree" (rather than "radian") mode. Most
//        programming languages assume radian arguments, requiring back
//        and forth convertions. The factor is 180/pi. So, for instance,
//        the equation RA = atan(0.91764 * tan(L)) would be coded as RA
//        = (180/pi)*atan(0.91764 * tan((pi/180)*L)) to give a degree
//        answer with a degree input for L.
//
//
//1. first calculate the day of the year
//
//	N1 = floor(275 * month / 9)
//	N2 = floor((month + 9) / 12)
//	N3 = (1 + floor((year - 4 * floor(year / 4) + 2) / 3))
//	N = N1 - (N2 * N3) + day - 30
//
//2. convert the longitude to hour value and calculate an approximate time
//
//	lngHour = longitude / 15
//	
//	if rising time is desired:
//	  t = N + ((6 - lngHour) / 24)
//	if setting time is desired:
//	  t = N + ((18 - lngHour) / 24)
//
//3. calculate the Sun's mean anomaly
//	
//	M = (0.9856 * t) - 3.289
//
//4. calculate the Sun's true longitude
//	
//	L = M + (1.916 * sin(M)) + (0.020 * sin(2 * M)) + 282.634
//	NOTE: L potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
//
//5a. calculate the Sun's right ascension
//	
//	RA = atan(0.91764 * tan(L))
//	NOTE: RA potentially needs to be adjusted into the range [0,360) by adding/subtracting 360
//
//5b. right ascension value needs to be in the same quadrant as L
//
//	Lquadrant  = (floor( L/90)) * 90
//	RAquadrant = (floor(RA/90)) * 90
//	RA = RA + (Lquadrant - RAquadrant)
//
//5c. right ascension value needs to be converted into hours
//
//	RA = RA / 15
//
//6. calculate the Sun's declination
//
//	sinDec = 0.39782 * sin(L)
//	cosDec = cos(asin(sinDec))
//
//7a. calculate the Sun's local hour angle
//	
//	cosH = (cos(zenith) - (sinDec * sin(latitude))) / (cosDec * cos(latitude))
//	
//	if (cosH >  1) 
//	  the sun never rises on this location (on the specified date)
//	if (cosH < -1)
//	  the sun never sets on this location (on the specified date)
//
//7b. finish calculating H and convert into hours
//	
//	if if rising time is desired:
//	  H = 360 - acos(cosH)
//	if setting time is desired:
//	  H = acos(cosH)
//	
//	H = H / 15
//
//8. calculate local mean time of rising/setting
//	
//	T = H + RA - (0.06571 * t) - 6.622
//
//9. adjust back to UTC
//	
//	UT = T - lngHour
//	NOTE: UT potentially needs to be adjusted into the range [0,24) by adding/subtracting 24
//
//10. convert UT value to local time zone of latitude/longitude
//	
//	localT = UT + localOffsetmAlternatives){					
//			Double3D posCandidate= getPositionCandidate();
//			int iHabitat = environment.getHabitatOnPosition(posCandidate); 
//			long dayTimeInMinutes= environment.getDayTimeInMinutes();
//			double probability = evaluatePosition(moveMode, dayTimeInMinutes ,iHabitat);
//			if( probability > 0.7 || count==0)
//				chosenPos = posCandidate;
//			
//	        count++;
//		}
//		
//		return chosenPos;
//    }
    
    
//    protected double evaluatePosition(MoveMode mode, long dayTimeInMinutes, int iHabitat){
//    	
//    	double probability=0.0;
//    	   	
//		// sunrise				
//		if( (dayTimeInMinutes > (DielCycle.DAY.beginTime()*60))  && ( dayTimeInMinutes <= (DielCycle.NIGHT.beginTime()*60))){
//			// maybe its better to throw a random probability between 0.5 and 1.0
//			if( iHabitat == 1) 
//				probability=1.0;
//			else if( iHabitat==2) 
//				probability=0.5;
//			else 					
//				probability=0.0;
//		}
//		// sunset
//		else{
//			if( iHabitat == 1) probability=0.5;
// 			else if( iHabitat==2) probability=1.0;
//			else probability=0.0;
//		}
//		return probability;
//    }
    
    
	
	public void initCentersOfAttraction(){
		
		// DEFINE STARTING CENTER OF ATTRACTIONS (ONCE PER FISH LIFE) 
		// find suitable point for center of attraction for foraging 
		// (random, but only in preferred habitattype)
		Double2D pos;
		do{
			pos=environment.getRandomFieldPosition();
		// hier abfrage nach habitat preference von spp def?	
		}while( environment.getHabitatOnPosition(pos) != HabitatHerbivore.SEAGRASS.id);
		
		centerOfAttrForaging=new Double2D(pos.x,pos.y);
		
		
		// find suitable point for center of attraction for resting (depending on habitat preference)
		do{
			pos=environment.getRandomFieldPosition();
			
		}while(environment.getHabitatOnPosition(pos) != HabitatHerbivore.CORALREEF.id);
		
		centerOfAttrResting=new Double2D(pos.x,pos.y);
	}
	
	// calculate vector from current position to center of attraction
	protected Double2D getDirectionToAttraction(Double3D currentPos, Double2D pointOfAttraction){
		
		double dirX=pointOfAttraction.x-currentPos.x;
		double dirY=pointOfAttraction.y-currentPos.y;		
		double length=Math.sqrt((dirX*dirX)+(dirY*dirY)) ;
		
		double nx=dirX/length;
		double ny=dirY/length;
		return new Double2D(nx,ny);
	}
	
	protected double getDistance(Double3D currentPos,Double2D pointOfAttraction){
		
		double dirX=pointOfAttraction.x-currentPos.x;
		double dirY=pointOfAttraction.y-currentPos.y;		
		return Math.sqrt((dirX*dirX)+(dirY*dirY)) ;		
	}

	
	/**
	 * agent's movement in one step with previously determined moveMode
	 */
	// exception => if something is wrong with daytime
	protected void move() throws Exception{
		
		// remember last position
		oldpos=pos;
		double food=1.0;//was heisst das?
		memory.offer(new MemEntry(new Double2D(pos.x,pos.y),food, environment.getDayTimeInMinutes()));
		if(memory.size() >= memSize){
			MemEntry mem=memory.poll();
//			for( MemEntry m: memory){
//				sum
//			}
		}
		// memField.set(cell.x,cell.y, memField.get(cell.x,cell.y)+1);
		Int2D cell=environment.getMemFieldCell(pos);		
		
		DielCycle dielCycle=DielCycle.getDielCycle(environment.getHourOfDay());
				
		double scal=100.0; // get the distance scaled to a value between 0 and 10
		
		double dist=0.0;
				
		// System.out.println(environment.getDayTimeInMinutes() );
		Double2D attractionDir=new Double2D();
		
		// hier stimmt abfrage noch nicht, 
		// evtl über activity(gibts nocht nicht) lösen, da bei nocturnal sunrise/sunset behaviour genau umgekehrt!!
		if(DielCycle.SUNRISE == dielCycle){
			dist=getDistance(pos,centerOfAttrForaging);
			attractionDir=getDirectionToAttraction(pos,centerOfAttrForaging);
		}
		else if(DielCycle.SUNSET == dielCycle){{
			dist=getDistance(pos,centerOfAttrResting);
			attractionDir=getDirectionToAttraction(pos,centerOfAttrResting);
		}
		double probabilityToAttraction=Math.tanh(dist/scal);
			
		
		
		if( probabilityToAttraction> 0.001)
			//step = cm per time step (stimmt das mit cm??) => abh�ngig von size(bodylength), cell resolution und time resolution
			step= size/params.environmentDefinition.cellResolution * speciesDefinition.stepMigration * 60 * params.environmentDefinition.timeResolutionMinutes;		
			
			// p*vec1 + 1-p*vec2:
			xSpeed = probabilityToAttraction * attractionDir.x * step +  environment.rand.nextGaussian() * attractionDir.x *step * 0.01;
			
			ySpeed = probabilityToAttraction * attractionDir.y * step +  environment.rand.nextGaussian() * attractionDir.y *step * 0.01;
		}
		
		double max = 109000; // erstmal nur so
				
		
		if (xSpeed > max)
			xSpeed=max;		
		else if(xSpeed < -max)
			xSpeed=-max;
				
		// ca 8 cm size
		
		if (ySpeed > max)
			ySpeed=max;		
		else if(ySpeed < -max)
			ySpeed=-max;
				
		double x=(pos.x+xSpeed+environment.getFieldSizeX()) % environment.getFieldSizeX();
        double y=(pos.y+ySpeed+environment.getFieldSizeY()) % environment.getFieldSizeY();
		
		pos=new Double3D(x,y,0);
		
		environment.field.setObjectLocation(this,new Double2D(x,y));
		
		
		// 2. TERMINE ACTIVTIY PHASE/FAVOURED HABITAT AT EACH TIMESTEP: 
		// sollte man explizit sunrise/sunset mit movemode verbinden?
		// determine favored habitat depending on activity pattern in spp definition
		// check time of day ueber sonnenstandsfunktion (daraus ergibt sich welches habitat bevorzugt wird)
		// determine activity mode: active (eg start after sunrise to after sunset)/resting (eg. start after sunset bis after sunrise)
			// if active => check habitat: is habitat the favored one for feeding? evtl auch distance zu focal point?
				// if not + depending on distance to center=> migrating to center1 with mode=biased random walk
				// if yes + center close enough=> foraging, mode=unbiased random walk, evtl auch hier schon choose best patch?
					//wenn dann ausserhalb des favored habitats, aber close enough to center=evaluate surroundings, move back to pos with most favored habitat?
			// if resting => check habitat: correct for resting?
				// if not => migrating to center2 with mode=biased random walk
				// if yes => resting => steplength close to 0! = resting movement
		    
		// 3. CALCULATE NEW POS based on move mode ueber according turning angle/steplength
		  // => boder behaviour=reflect
		
		// 4. CALCULATE ACTIVITY COSTS depending on move mode ueber according speed
		// net activity costs per hour (kJ/h) = (1.193*U(cm/s)^1.66)*0.0142 (=>oxicaloric value!) 
		// speed = step (cm per time step) in cm per sec umrechnen STIMMT SO NOCH NICHT! STEP IST NICHT IN CM ODER?
		currentSpeed=step/(60* params.environmentDefinition.timeResolutionMinutes);
		// net costs per timestep = 1.193*speed pro sec^1.66*oxicaloric value/60*timeResolution				
		// gilt so nur für parrots, gibts was allgemein gültiges??
		netActivityCosts=(1.193*Math.pow(currentSpeed, 1.66))*0.0142/60* params.environmentDefinition.timeResolutionMinutes;
		
		// habitat => determines predation risk and food availability
		// predation risk?
		// food availability?
		// exploration behaviour?
		    
							
//		MoveMode moveMode=determineMoveMode();
//		int numAlternatives=1; // was bedeutet das?
//		
//		// when feeding turning angle is medium and stepLengthFactor is small 
//		// => fish is slowly grazing the current area
//		if( moveMode == MoveMode.FORAGING) {
//			stepLengthFactor= 0.1;
//			turningFactor= 0.7;
//			numAlternatives=1;			
//				//activity factor=
//		}
//		
//		// when searching turning angle is big and stepLengthFactor is medium
//		// => fish is checking most of its surroundings
//		else if( moveMode == MoveMode.SEARCHFOODPATCH) {
//				stepLengthFactor= 0.1;
//				turningFactor= 0.7;
//				numAlternatives=1;	
//				//activity factor = 
//					
//		}
//		
//		// when searching turning angle is big and stepLengthFactor is medium
//		// => fish is checking most of its surroundings
//		else if( moveMode == MoveMode.SEARCHFOODPATCH) {
//						stepLengthFactor= 0.1;
//						turningFactor= 0.7;
//						numAlternatives=1;	
//						//activity factor = 
//		}
//		
//		// when searching turning angle is big and stepLengthFactor is medium
//		// => fish is checking most of its surroundings
//		else{
//			stepLengthFactor= 3.0;
//			turningFactor= 0.1;
//		}
//		Double3D newPos= findSuitablePosition(moveMode, numAlternatives);
//		pos=newPos;
//        // update agent's position in the simulation world
//		environment.field.setObjectLocation(this, new Double2D( pos.x, pos.y));	
	}

	
	
	//////////////////CONSUMPTION///////////////////////////////////////
	// includes loss due to excretion/egestion/SDA)
	//food in g dry weight and fish in g wet weight!!
	public void feed() {	
		
		if(pos.x < 0)
			System.out.println("");
		
		// get the amount of food on current patch of foodField in g dry weight/m2
		availableFood=getFoodAt(pos.x,pos.y);	 
		
		
		// daily consumption rate = g food dry weight/g fish wet weight*day
		// multiplied with individual fish biomass and divided by time resolution 
		// only 12 of 24 h are considered relevant for food intake, daher divided by 12 not 24!
		double consumptionRatePerTimeStep=(speciesDefinition.consumptionRate*biomass/12/60)* params.environmentDefinition.timeResolutionMinutes;
		// food intake in g food dry weight
		foodIntake=consumptionRatePerTimeStep;
		// even if fish could consume more, just take the available food on grid
		foodIntake=(foodIntake > availableFood) ? availableFood : foodIntake;

		// energy intake (kJ) = amount of food ingested (g dry weight)*energy content of food (kJ/g food dry weight)
		energyIntake=foodIntake*speciesDefinition.energyContentFood;
		// in g algal dry weight
		intakeForCurrentDay+=foodIntake;
		if(intakeForCurrentDay >= speciesDefinition.maxDailyFoodRationA*biomass+speciesDefinition.maxDailyFoodRationB){
			isHungry=false;
		}
		
		if((energyIntake <= ((speciesDefinition.maxDailyFoodRationA*biomass + speciesDefinition.maxDailyFoodRationB)*speciesDefinition.energyContentFood) ) && (energyIntake>0.0) )
		 {	
			
			// after queueSize steps the energyIntake flows to the shortterm
			double delayForStorageInSteps= speciesDefinition.gutTransitTime/params.environmentDefinition.timeResolutionMinutes;
			
			gutStorageQueue.offer(energyIntake);
			// wenn transit time (entspricht queue size) reached => E geht in shortterm storage		
			if(gutStorageQueue.size() >= delayForStorageInSteps){
				// gutStorageQueue.poll entnimmt jeweils 1. element und löscht es damit aus queue
				shorttermStorage+=speciesDefinition.netEnergy*gutStorageQueue.poll();
			}			
		}
		// update the amount of food on current foodcell
		setFoodAt(pos.x,pos.y,availableFood-foodIntake);
	}
	
	
	
	/////////////////ENERGY BUDGET////////////////////////////////////////////////////////////////
	// returns false if fish dies due to maxAge, starvation or naturalMortality
	public boolean updateEnergy(){		
		
		// METABOLISM (RESPIRATION)
		restingMetabolicRatePerTimestep=(restingMetabolicRateA*Math.pow(biomass, restingMetabolicRateB))*0.434* params.environmentDefinition.timeResolutionMinutes/60;	
		// total energy consumption (RMR + activities)
		double energyConsumption=restingMetabolicRatePerTimestep+netActivityCosts;	
		
		// if not enough energy for consumption in shortterm storage
		// transfer energy to shortterm storage from bodyFat, then reproFraction, and last from bodyTissue 
		// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87, von bodyTissue=0.90 (Brett & Groves 1979)
		if(shorttermStorage < energyConsumption) {	
			// not enough in bodyFat AND reproFraction => metabolise energy from ALL 3 body compartments
			if( (bodyFat < (shorttermStorage-energyConsumption)/VerlustfaktorFatToEnergy) && (reproFraction < (energyConsumption-shorttermStorage - energyConsumption)/VerlustfaktorFatToEnergy)) {
				double energyFromProtein=(energyConsumption-shorttermStorage-(bodyFat/VerlustfaktorFatToEnergy)-(reproFraction/VerlustfaktorReproToEnergy)) / VerlustfaktorTissueToEnergy;
				shorttermStorage+=energyFromProtein*VerlustfaktorTissueToEnergy;
				bodyTissue-=energyFromProtein;				
				shorttermStorage+=bodyFat*VerlustfaktorFatToEnergy;
				bodyFat=0;
				reproFraction=0;
			}
			// transfer energy to shortterm storage from bodyFat and then from reproFraction
			// verlustfaktor beim metabolizieren von bodyFat/reproFraction=0.87 
			else if(bodyFat < (shorttermStorage-energyConsumption)/VerlustfaktorFatToEnergy) {
					double energyFromRepro=(energyConsumption-shorttermStorage-(bodyFat/VerlustfaktorFatToEnergy))/VerlustfaktorTissueToEnergy;
					shorttermStorage+=energyFromRepro*VerlustfaktorReproToEnergy;
					reproFraction-=energyFromRepro;				
					shorttermStorage+=bodyFat*VerlustfaktorFatToEnergy;
					bodyFat=0;
			}
			// if not enough energy for consumption in shortterm storage but enough in bodyFat, energy diff is metabolized from bodyFat only
			else{
				double diff=energyConsumption-shorttermStorage;
				shorttermStorage+=diff;
				// vom bodyFat muss mehr abgezogen werden due to verlust beim metabolizieren
				bodyFat-=diff/VerlustfaktorFatToEnergy;
			}
		}
		// enough energy for consumption in shortterm storage
		else {
			shorttermStorage-=energyConsumption;		
		}
		
		// PRODUCTION (Growth, reproduction is calculated extra)
		// if more energy in shortterm storgae then needed for metablism and it exceeds shortterm storage maxCapcity
		// maxCapacity shortterm storage = 450*restingMetabolicRatePerTimestep (nach Hauke) CHECK!!
		if(shorttermStorage > restingMetabolicRatePerTimestep*450){
			double energySpillover=shorttermStorage-restingMetabolicRatePerTimestep*450;
			shorttermStorage-=energySpillover;
				if ((lifeStage==LifeStage.FEMALE) && (reproFraction < biomass*maxReproFraction*energyPerGramRepro)) {
					// => energy is transfered into bodycompartments with same verlustfaktor wie beim metabolisieren zu energie
					// wenn female: zu 95% zu bodyTissue, zu 3.5% zu bodyFat, 1.5% zu repro (f�r repro: following Wootton 1985)
					bodyFat+=VerlustfaktorFatToEnergy*energySpillover*0.035;
					bodyTissue+=VerlustfaktorTissueToEnergy*energySpillover*0.95;
					reproFraction+=VerlustfaktorReproToEnergy*energySpillover*0.015;
				}
				// wenn juvenile oder max capacity reproFraction f�r females erreicht
				else {
					// => energy is transfered into bodycompartments with same verlustfaktor wie beim metabolisieren zu energie
					// zu 95% zu bodyTissue, zu 5% zu bodyFat according to body composition 
					bodyFat+=VerlustfaktorFatToEnergy*energySpillover*0.05;
					bodyTissue+=VerlustfaktorTissueToEnergy*energySpillover*0.95;
				}
						
		}
		
		// adjustment of virtual age
		// comparision of overall current energy at age vs. expected energy at age (vBGF) to slow down growth (more realistic)
		// energy in gut (aufsummieren der GutContentElemente)
		currentGutContent=0;
		for(Double d:gutStorageQueue){
			currentGutContent+=d;			
		}
		// sum of energy in all body compartments	
		currentEnergyWithoutRepro=bodyTissue+bodyFat+shorttermStorage+currentGutContent;
		expectedEnergyWithoutRepro=speciesDefinition.expectedEnergyWithoutRepro.interpolate(giveAge()-virtualAgeDifference);
		
		System.out.print(currentEnergyWithoutRepro);

		// daily: compare current growth with expected growth at age from vBGF + ggf adjust virtual age + die of starvation, maxAge, naturalMortality
		if(getSteps() % (60/getTimeResInMinutes()*24) == 0) { 
			double maxAge=speciesDefinition.maxAgeInYrs+environment.rand.nextGaussian();	
			
			if ((expectedEnergyWithoutRepro-currentEnergyWithoutRepro) > 10) {
			
			// das funktioniert so nicht! abfrage dreht sich im kreis!!	
				double virtualAge=giveAge(); //asymLenghtsL*(1- Math.pow(Math.E,-growthCoeffB*(age+ageAtTimeZero)));		
				double diff=giveAge()-virtualAge;
				virtualAgeDifference+=diff;	
			}
			
			if(( currentEnergyWithoutRepro < 0.6*expectedEnergyWithoutRepro) 
			|| maxAge <= giveAge() 
			|| (speciesDefinition.mortalityRatePerYears/365) > environment.rand.nextDouble()){
			
				//die();
				//return false;
			}
		}
	
		// adjust isHungry, REICHT DIE ABFRAGE AN DIESER STELLE UND ALLES ABGEDECKT?
		// 1.abfrage = to limit overall growth, 2.abfrage to limit daily intake
		if (currentEnergyWithoutRepro >= 0.95*expectedEnergyWithoutRepro || intakeForCurrentDay >= (speciesDefinition.maxDailyFoodRationA*biomass+speciesDefinition.maxDailyFoodRationB)){   
		
			isHungry=false;
		}
		else {
			
			isHungry=true;
		}
		
		return true;
	}

	
	public long getSteps(){
		
		return environment.getCurrentTimestep();
	}
		
	public double getFoodAt(double x,double y){
		
		return environment.foodField.get((int)x,(int)y);
	}
	
	public void setFoodAt(double x,double y,double val){
		
		environment.foodField.set((int)pos.x, (int)pos.y,availableFood-foodIntake);
	}

	public int getTimeResInMinutes(){
		
		return params.environmentDefinition.timeResolutionMinutes;
	}
	
	/////////////////GROWTH////////////////////////////////////////
	// called daily to update biomass, size only weekly
	public void grow() {
		
		// update fish biomass (g wet weight)
		// conversion factor for shortterm and gut same as for tissue
		biomass=(bodyFat*conversionRateFat)+(bodyTissue+shorttermStorage+currentGutContent)*conversionRateTissue+(reproFraction*conversionRateRepro);
		
		// update fish size (SL in cm)
		if( (environment.getCurrentTimestep() % ((60*24*7)/environment.getTimeRes())) == 0) {
			if( biomass > oldBiomassWeekly) {
				//W(g WW)=A*L(SL in cm)^B ->  L=(W/A)^1/B
				double exp=1/speciesDefinition.lengthMassCoeffB; 
				double base=biomass/speciesDefinition.lengthMassCoeffA;
				size=Math.pow(base,exp);
				//for testing
				System.out.println("biomass: " + biomass);
				System.out.println("size: " + size);			}		
			oldBiomassWeekly=biomass;
		}		
	}
	
    
	
	/////////////////REPRODUCTION////////////////////////////////////////
	protected void reproduce() {
			
		double reproFractionOld=0.0;
		double diffRepro=0.0;
		// b=Anzahl offspring, damit jeder offspring (wenn mehr als 2) initialisiert werden	
		// DELAY factor von der Größe post-settlement age (zb 0.33 yrs einfügen!!)
		for (int b=0; b < speciesDefinition.nrOffspring; b++) {
			// set biomass of offspring to initialSize/initialBiomass, VORSICHT wenn INITIAL VALUES GEändert werden!!
			Fish offSpring=new Fish(oldpos.x, oldpos.y, oldpos.z, (speciesDefinition.initialBiomass), speciesDefinition.initialSize, environment,params, speciesDefinition);
			// guide the new born in the same direction as the reflected parent 
			offSpring.dx=this.dx;
			offSpring.dy=this.dy;
			
			
			// schedule the new born to the next timeslot for initialization
			environment.schedule(offSpring);
		}
		// biomass loss of parent fish due to reproduction effort:
		// CHECK STIMMT DAS SO???
		reproFractionOld=reproFraction;
		// all E taken from reproFraction! set ReproFraction back to minRepro (following Wootton 1985)
		reproFraction=biomass*minReproFraction*energyPerGramRepro;
		// substract loss in reproFraction from biomass 
		// REIHENFOLGE zur berechnung von minRepro aus biomass nicht ganz korrekt, CHECK MIT HAUKE!!
		diffRepro=reproFractionOld-reproFraction;
		biomass=biomass-diffRepro*conversionRateRepro;
	}
	
	// age in years!!
	// currenttimestep -birthtimestep converted to years
	public double giveAge(){
		// substract birthtimestep from current timestep+add intialAgeInDays(=>vorher in timesteps umrechnen!)
		return (environment.getCurrentTimestep()+1-birthTimeStep+SpeciesDefinition.initialAgeInYrs*365*24*environment.getTimeRes()) 
				/ (double)(60/params.environmentDefinition.timeResolutionMinutes*24*365);
	}
	
	public double giveSize() {
		return size;
	}
	
	public boolean isAlive() {
		return alive;
	}	
	
	public String getAge() {
		return (giveAge() + " years");
	}
	
	public String getActivityCosts() {
		return (netActivityCosts + " kJ");
	}
	
	public boolean isHungry() {
		return isHungry;
	}
	
	public String getAvailableFood() {
		return (availableFood + " g DW");
	}
	
	public String getFoodIntake() {
		return (foodIntake + " g DW");
	}
	
	public String getIntakeForCurrentDay() {
		return (intakeForCurrentDay + " g DW");
	}
	
	public String getEnergyIntake() {
		return (energyIntake + " kJ");
	}
	
	public Queue<Double> getGutQueueInKJ() {
		return gutStorageQueue;
	}
	
	public String getGutContent() {
		return (currentGutContent + " kJ");
	}
	
	public String getShorttermStorage() {
		return (shorttermStorage + " kJ");
	}

	public String getBodyFat() {
		return (bodyFat + " kJ");
	}

	public String getBodyTissue() {
		return (bodyTissue + " kJ");
	}
	
	public String getReproFraction() {
		return (reproFraction + " kJ");
	}
	
	public String getBiomass() {
		return (biomass + " g WW");
	}
	
	public String getSize() {
		return (size + " SL in cm");
	}
	
	public LifeStage getLifeStage() {
		return lifeStage;
	}
	
	public String getCurrentEnergy() {
		return (currentEnergyTotal + " kJ");
	}
	
	public String getExpextedtEnergy() {
		return (expectedEnergyWithoutRepro + " kJ");
	}
	

	public double getVirtualAgeDifference() {
		return virtualAgeDifference;
	}
	
	public String getRMR() {
		return (restingMetabolicRatePerTimestep + " kJ per timestep");
	}
	
//	public MoveMode getCurrentMoveMode() {
//		return currentMoveMode;
//  }
//	
//	public String getActivityCosts() {
//	return activityFactor?;
//}
//
//	public double getBirthTimeStep() {
//		return birthTimeStep;
//	}
//
//	public double getTurningFactor() {
//		return turningFactor;
//	}
//
//	public double getStepLengthFactor() {
//		return stepLengthFactor;
//	}
//
//	public double getxSpeed() {
//		return xSpeed;
//	}
//
//	public double getySpeed() {
//		return ySpeed;
//	}
//
//	public double getzSpeed() {
//		return zSpeed;
//	}
//
//	public double getBiomass() {
//		return biomass;
//	}
//	public double getOldBiomassWeekly() {
//		return oldBiomassWeekly;
//	
//	
//  public Double3D getPos() {
//	return pos;
//  }
	
}

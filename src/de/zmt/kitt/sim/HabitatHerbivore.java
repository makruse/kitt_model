package de.zmt.kitt.sim;

import java.awt.Color;


public enum HabitatHerbivore {	

	//habitat seleciton rules due to activity pattern and risk and food??
	// habitat hat ID, name, farbe, minFood, maxFood, predRisk, determines survival and food availability
	// algal turf standing crop in g/m2, als richtlinine: nach Cliffton 1995: 5-14 g/m2 in reef 
	// neue Angaben in gramm/m2
	CORALREEF (1,"coral reef", new Color(0,255,255), 10.0, 14.0, 0.002 ), // tuerkis
	SEAGRASS( 2,"seagrass bed", new Color(0,255,1) , 5.0, 10.0, 0.001), // green
	MANGROVE ( 3,"mangrove", new Color(60,179,113) , 3.0, 5.0, 0.02),	// dark green
    ROCK (4, "rock", new Color(161,161,161) , 5.0, 6.0, 0.001), // grey
    SANDYBOTTOM (5, "sandy bottom", new Color(255,255,0), 0.0, 3.0, 0.008), // yellow
    MAINLAND (6, "mainland", new Color(255,255,255), 0.0, 0.0, 0.0);  // 2hite
	//UNSPECIFIED(0, "unspecified",new Color(220,220,220), 0.0, 0.0 );  // light grey
	
    // Unterscheidung der food initialwerte f�r habitate zw carnivoren und herbivor
    public final int id;
    private final String name;
    /** color in the input map that is related to this habitat */
    private final Color color;
    /** typical algal standing crop in g/m2 */
    public final double initialFoodMin;
    public final double initialFoodMax;
    public double mortalityPredation;

    HabitatHerbivore( int id,String name,Color color, double initialFoodMin, double initialFoodMax, double mortalityPredation) {
    	this.id=id;
        this.name = name;
        this.color = color;
        this.initialFoodMin= initialFoodMin;
        this.initialFoodMax= initialFoodMax;
        this.mortalityPredation=mortalityPredation;
    }
    public String getName() { return name; }
    public Color getColor() { return color; }

    static public HabitatHerbivore getHabitat(int id) throws Exception{ 
    	for (HabitatHerbivore h : HabitatHerbivore.values()){
    		if( h.id==id)
    			return h;
    	}
    	throw new Exception();
        //System.out.printf("HabitatHerbivore %s is %d%d%n", h, h.name(), h.color());
    }

    public void print(){        
        for (HabitatHerbivore h : HabitatHerbivore.values()){
           //System.out.printf("HabitatHerbivore %s is %d%d%n", h, h.name(), h.color());
        }
    } 
    
    // int red= HabitatHerbivore.SANDYBOTTOM.getColor().getRed()
};
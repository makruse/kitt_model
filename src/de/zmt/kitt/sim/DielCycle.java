package de.zmt.kitt.sim;

public enum DielCycle {
    
	DAY ( "daytime", 8, 17),
    NIGHT ( "night", 20, 24),
    LATE_NIGHT ( "night", 0, 5),
	SUNRISE ( "sunrise",5, 8),
    SUNSET ( "sunset", 17, 20);

    private final int beginTime;   // in hours
    private final int endTime; // in hours

    DielCycle(String name,int beginTime, int endTime) {
        this.beginTime = beginTime;
        this.endTime = endTime;
    }
    
    public int beginTime() { 
    	return beginTime;
    }
    
    public int endTime() { 
    	return endTime; 
    }

    public void print(){        
        for (DielCycle t : DielCycle.values())
           System.out.printf("Time of day %s is %d%d%n",
                             t, t.beginTime(), t.endTime());
    }  
    
    
    static public DielCycle getDielCycle(long dayHour){ 
    	for (DielCycle dc : DielCycle.values()){
     		if( (dayHour >= dc.beginTime  && dayHour <= dc.endTime) )  //|| dc.endTime <= 6 <=6 wegen tageswechsel 0 Uhr
    			return dc;
    	}
    	return DielCycle.NIGHT;
        //System.out.printf("HabitatHerbivore %s is %d%d%n", h, h.name(), h.color());
    }

    // int beginTime= DielCycle.MORNING.beginTime()
    //

    
//    DAY ( "daytime"),
//    NIGHT ( "night"),
//	  SUNRISE ( "sunrise"),
//    SUNSET ( "sunset");  
    
//  // Calculating sunset/sunrise depending on day of the year and latitude
//	// Tag vom Jahr + Latitude in degrees? (coral eye lat=breite=1.753883 N, länge=lon=125.131261 E)
//	// Die geographische Länge ist ein Winkel, der ausgehend vom Nullmeridian (0°) bis 180° in östlicher und 180° in westlicher Richtung gemessen wird
//	// Die Breite erreicht Werte von 0° (Äquator) bis ±90° (Pole). traditionell +Nord, −Süd;  +Ost, −West
//	//ein jahr entspricht 2*PI
//	//manado coordinates: 1°29′35″N 124°50′29″E = 01.4930556°, 124.8413889° = 51N 704866mE 165113mN(UTM)
//	
//	// sim soll starten um 0:00 uhr => wann ist dann sunrise? 
//
//	//Originally from : "A Model Comparison for Daylength as a function of latitude and day of the year", 
//	//Ecological Modelling , volume 80 (1995))
//	
//	/** latitude in decimal degrees */
//	double lat=1.753883;
//	/** day of the year (from 1-365) */
//	//01.Januar entspricht dayOfYear = 1
//	double DayOfYear=environment.getDay();
//	
//	double P = Math.asin(0.39795*Math.cos(0.2163108 + 2*Math.atan(0.9671396*Math.tan(0.00860*(DayOfYear-186)))));
//	double LengthOfDay=24-(24/Math.PI)*Math.acos((Math.sin(0.8333*Math.PI/180) + Math.sin(lat*Math.PI/180)*Math.sin(P))/(Math.cos(lat*Math.PI/180)*Math.cos(P)));
//	
//	/** length of night in hours */
//	double nightlength=24-LengthOfDay; 
//	/**uhrzeit of sunrise */
//	double sunrise=12-LengthOfDay/2;
//	/** uhrzeit of sunset */
//	double sunset=12+LengthOfDay/2;
//	
//	//umrechnung von Dämmerung in stunden (als dezimalzahl) in uhrzeit format hh:mm
//	int sunriseHour=(int)(sunrise);
//	int sunriseMin=(int)((sunrise-(int)(sunrise))*60);
//	int sunsetHour=(int)(sunset);
//	int sunsetMin=(int)((sunset-(int)(sunset))*60);
//	String uhrzeitSunrise;		
//	uhrzeitSunrise=String.format("%02d:%02d", sunriseHour,sunriseMin);
//	String uhrzeitSunset;		
//	uhrzeitSunset=String.format("%02d:%02d", sunsetHour,sunsetMin);
    
//    public static double
   
};
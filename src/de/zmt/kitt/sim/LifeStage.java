package de.zmt.kitt.sim;

public enum LifeStage {	
	
	// 50% maturity values (change of lifeStage only based size (SL in cm) not age): 
	// juv=14.8 TL, females=20.7 (McIlwain 2009) => juv=12.34, fem=17.38, 
	//Lasym aus vBGF=39.1 => 40 als max wert für male genommen
	// TL to SL (based on formula für Serranidae: SL=TL/1.1709-0.2975, aus: Gaygusuz et al. 2006) 
	// fish is juvenile von 0-12cm
	JUVENILE ("Juvenile", 0, 12),
	// fish is female von 12-17cm
    FEMALE("Female",  12, 17),
    // größer als 17 = male)
	MALE("Male", 17, 40);
	
	
    private final String name;
    private final int minSize;
    private final int maxSize;

    LifeStage(String name ,int minSize, int maxSize) {
        this.name = name;
        this.minSize= minSize;
        this.maxSize= maxSize;
    }
    public String getName() { return name; }

    public void print(){        
        for (LifeStage m : LifeStage.values()){
           //System.out.printf("Life stage %s is %d%d%n", m, m.name(), m.color());
        }
    } 
    
    // int minSize= LifeStage.JUVENILE.getMinAge()
};
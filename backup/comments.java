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
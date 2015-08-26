package de.zmt.ecs.system;

import java.util.*;

import de.zmt.ecs.*;
import de.zmt.ecs.system.agent.*;
import de.zmt.ecs.system.environment.*;

public abstract class AbstractKittSystem extends AbstractSystem {

    private static final int DEFAULT_ORDERING = 0;
    protected static final Map<Class<? extends EntitySystem>, Integer> ORDERINGS = new HashMap<>();

    {
	// environment systems
	ORDERINGS.put(SimulationTimeSystem.class, 0);
	ORDERINGS.put(GrowFoodSystem.class, 1);

	// agent systems
	ORDERINGS.put(ActivitySystem.class, 0);
	ORDERINGS.put(MoveSystem.class, 1);
	ORDERINGS.put(AgeSystem.class, 1);
	ORDERINGS.put(FeedSystem.class, 2);
	ORDERINGS.put(CompartmentsSystem.class, 3);
	ORDERINGS.put(ConsumeSystem.class, 4);
	ORDERINGS.put(GrowthSystem.class, 5);
	ORDERINGS.put(ReproductionSystem.class, 6);
	ORDERINGS.put(MortalitySystem.class, 7);
    }

    private static int getOrderingForSystem(Class<? extends EntitySystem> systemClass) {
        if (ORDERINGS.containsKey(systemClass)) {
            return ORDERINGS.get(systemClass);
        } else {
            return DEFAULT_ORDERING;
        }
    }

    @Override
    public final int getOrdering() {
        return getOrderingForSystem(this.getClass());
    }

}
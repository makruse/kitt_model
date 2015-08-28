package de.zmt.ecs.component.agent;

import de.zmt.ecs.Component;
import de.zmt.ecs.component.agent.Metabolizing.BehaviorMode;
import sim.util.Double2D;

public class AttractionCenters implements Component {
    private static final long serialVersionUID = 1L;

    /** Attraction center of habitat-dependent foraging area. */
    private final Double2D foragingCenter;
    /** Attraction center of habitat-dependent resting area. */
    private final Double2D restingCenter;

    public AttractionCenters(Double2D foragingCenter,
	    Double2D restingCenter) {
	this.foragingCenter = foragingCenter;
	this.restingCenter = restingCenter;
    }

    public Double2D getForagingCenter() {
        return foragingCenter;
    }

    public Double2D getRestingCenter() {
        return restingCenter;
    }

    public Double2D obtainCenter(BehaviorMode behaviorMode) {
	if (behaviorMode == BehaviorMode.FORAGING) {
	    return foragingCenter;
	} else if (behaviorMode == BehaviorMode.RESTING) {
	    return restingCenter;
	} else {
	    throw new IllegalArgumentException("No center for " + behaviorMode);
	}
    }

    @Override
    public String toString() {
	return "AttractionCenters [foragingCenter=" + foragingCenter
		+ ", restingCenter=" + restingCenter + "]";
    }
}

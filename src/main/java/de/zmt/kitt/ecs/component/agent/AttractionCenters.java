package de.zmt.kitt.ecs.component.agent;

import sim.util.Double2D;
import de.zmt.ecs.Component;
import de.zmt.kitt.ecs.component.agent.Metabolizing.ActivityType;

public class AttractionCenters implements Component {
    private static final long serialVersionUID = 1L;

    /** attraction center of habitat-dependent foraging area */
    private final Double2D foragingCenter;
    /** attraction center of habitat-dependent resting area */
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

    public Double2D obtainCenter(ActivityType activityType) {
	if (activityType == ActivityType.FORAGING) {
	    return foragingCenter;
	} else if (activityType == ActivityType.RESTING) {
	    return restingCenter;
	} else {
	    throw new IllegalArgumentException("No center for " + activityType);
	}
    }

    @Override
    public String toString() {
	return "AttractionCenters [foragingCenter=" + foragingCenter
		+ ", restingCenter=" + restingCenter + "]";
    }
}

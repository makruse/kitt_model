package de.zmt.kitt.ecs.component.agent;

import sim.util.Double2D;
import ecs.Component;

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

    @Override
    public String toString() {
	return "AttractionCenters [foragingCenter=" + foragingCenter
		+ ", restingCenter=" + restingCenter + "]";
    }
}

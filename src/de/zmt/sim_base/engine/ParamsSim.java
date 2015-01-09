package de.zmt.sim_base.engine;

import sim.engine.*;
import de.zmt.sim_base.engine.params.ParamsBase;
import ec.util.MersenneTwisterFast;

public class ParamsSim extends SimState {
    private static final long serialVersionUID = 1L;

    protected ParamsBase params;

    public ParamsSim(long seed) {
	super(seed);
    }

    public ParamsSim(MersenneTwisterFast random) {
	super(random);
    }

    public ParamsSim(MersenneTwisterFast random, Schedule schedule) {
	super(random, schedule);
    }

    public ParamsSim(long seed, Schedule schedule) {
	super(seed, schedule);
    }

    public ParamsBase getParams() {
	return params;
    }

    public void setParams(ParamsBase params) {
	this.params = params;
    }
}
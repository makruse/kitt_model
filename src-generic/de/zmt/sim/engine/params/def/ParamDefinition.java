package de.zmt.sim.engine.params.def;

import java.io.Serializable;

public interface ParamDefinition extends Serializable {
    /** Title appearing in {@link ParamsInspector}'s tab list */
    public String getTitle();
}
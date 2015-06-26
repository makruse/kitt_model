package de.zmt.sim.engine.params.def;

import java.io.Serializable;

public interface ParamDefinition extends Serializable {
    /**
     * Title appearing in {@link ParamsInspector}'s tab list, also used in
     * automation to discriminate between objects if there are several of one
     * class.
     */
    public String getTitle();
}
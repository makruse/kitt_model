package de.zmt.sim.engine.params;

import de.zmt.sim.engine.params.def.ParamDefinition;

@SuppressWarnings("serial")
public class NotAutomatableFieldDefinition extends TestDefinition {
    @ParamDefinition.NotAutomatable
    private String notAutomatableValue = "not automatable";
    // INVALID DEFINITION
    public static final String FIELD_NAME_NOT_AUTO = "notAutomatableValue";

    public String getNotAutomatableValue() {
	return notAutomatableValue;
    }

    public void setNotAutomatableValue(String notAutomatableValue) {
	this.notAutomatableValue = notAutomatableValue;
    }
}

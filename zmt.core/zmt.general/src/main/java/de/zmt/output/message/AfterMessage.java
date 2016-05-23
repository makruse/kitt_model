package de.zmt.output.message;

/** Tagging interface for messages sent after collection. */
public interface AfterMessage {
    /** @return step number passed in simulation */
    long getSteps();
}
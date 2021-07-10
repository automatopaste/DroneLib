package data.scripts.subsystems.ai;

public interface dl_BaseSubsystemAI {
    /**
     * Returns the desired subsystem AI type.
     * @return
     */
    dl_BaseSubsystemAI getAI();

    /**
     * Initialise the AI script.
     */
    void aiInit();

    /**
     * Used to update any system AI decisions. Called after subsystem logic is advanced.
     * @param amount frame delta
     */
    void aiUpdate(float amount);
}

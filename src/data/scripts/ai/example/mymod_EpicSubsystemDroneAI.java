package data.scripts.ai.example;

import com.fs.starfarer.api.combat.ShipAIConfig;
import data.scripts.ai.dl_BaseSubsystemDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.subsystems.dl_BaseDroneSubsystem;
import org.lwjgl.util.vector.Vector2f;

public class mymod_EpicSubsystemDroneAI extends dl_BaseSubsystemDroneAI {
    public mymod_EpicSubsystemDroneAI(dl_DroneAPI passedDrone, dl_BaseDroneSubsystem baseDroneSubsystem) {
        super(passedDrone, baseDroneSubsystem);
    }

    @Override
    protected Vector2f getMovementTargetLocation(float amount) {
        return null;
    }

    @Override
    protected void doRotationTargeting() {

    }

    @Override
    public void setDoNotFireDelay(float amount) {

    }

    @Override
    public void forceCircumstanceEvaluation() {

    }

    @Override
    public boolean needsRefit() {
        return false;
    }

    @Override
    public void cancelCurrentManeuver() {

    }

    @Override
    public ShipAIConfig getConfig() {
        return null;
    }
}

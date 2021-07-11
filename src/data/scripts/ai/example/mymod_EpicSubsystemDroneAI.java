package data.scripts.ai.example;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAIConfig;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.scripts.ai.dl_BaseSubsystemDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.subsystems.dl_BaseDroneSubsystem;
import data.scripts.subsystems.example.mymod_EpicDroneSubsystem;
import data.scripts.util.dl_DroneAIUtils;
import data.scripts.util.dl_SpecLoadingUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class mymod_EpicSubsystemDroneAI extends dl_BaseSubsystemDroneAI {
    private final float[] shieldOrbitRadiusArray;
    private final float[] shieldOrbitSpeedArray;
    private final float[] defenceOrbitAngleArray;
    private final float[] defenceFacingArray;
    private final float[] defenceOrbitRadiusArray;
    private float shieldOrbitAngle;
    private float shieldOrbitRadius;
    private float defenceOrbitAngle;
    private float defenceFacing;
    private float defenceOrbitRadius;
    private WeaponSlotAPI landingSlot;
    private mymod_EpicDroneSubsystem.EpicDroneOrders orders;

    public mymod_EpicSubsystemDroneAI(dl_DroneAPI passedDrone, dl_BaseDroneSubsystem baseDroneSubsystem) {
        super(passedDrone, baseDroneSubsystem);

        shieldOrbitRadiusArray = dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.getshieldOrbitRadiusArray();
        shieldOrbitSpeedArray = dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.getshieldOrbitSpeedArray();
        defenceOrbitAngleArray = dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.getDefenceOrbitAngleArray();
        defenceFacingArray = dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.getDefenceFacingArray();
        defenceOrbitRadiusArray = dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.getDefenceOrbitRadiusArray();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        CombatEngineAPI engine = Global.getCombatEngine();

        mymod_EpicDroneSubsystem droneSubsystem = (mymod_EpicDroneSubsystem) engine.getCustomData().get(baseDroneSubsystem.getSystemID() + ship.hashCode());
        if (droneSubsystem == null) return;
        this.baseDroneSubsystem = droneSubsystem;

        //assign specific values
        droneIndex = baseDroneSubsystem.getIndex(drone);

        shieldOrbitRadius = shieldOrbitRadiusArray[droneIndex];
        float shieldOrbitSpeed = shieldOrbitSpeedArray[droneIndex];
        defenceOrbitAngle = defenceOrbitAngleArray[droneIndex];
        defenceFacing = defenceFacingArray[droneIndex];
        defenceOrbitRadius = defenceOrbitRadiusArray[droneIndex];

        orders = droneSubsystem.getDroneOrders();

        switch (orders) {
            case DEFENCE:
                List<dl_DroneAPI> deployedDrones = baseDroneSubsystem.getDeployedDrones();
                float angleDivisor = 360f / deployedDrones.size();
                shieldOrbitAngle = droneIndex * angleDivisor;

                delayBeforeLandingTracker.setElapsed(0f);

                landingSlot = null;

                drone.getShield().toggleOff();
                break;
            case SHIELD:
                delayBeforeLandingTracker.setElapsed(0f);

                landingSlot = null;

                shieldOrbitAngle += shieldOrbitSpeed * amount;

                drone.getShield().toggleOn();
                break;
            case RECALL:
                dl_DroneAIUtils.attemptToLand(ship, drone, amount, delayBeforeLandingTracker, engine);

                if (landingSlot == null) {
                    landingSlot = baseDroneSubsystem.getLandingBayWeaponSlotAPI();
                }

                drone.getShield().toggleOff();
                break;
        }

        doRotationTargeting();

        Vector2f movementTargetLocation = getMovementTargetLocation(amount);
        if (movementTargetLocation != null) {
            dl_DroneAIUtils.move(drone, drone.getFacing(), movementTargetLocation);
        }
    }

    @Override
    protected Vector2f getMovementTargetLocation(float amount) {
        float angle;
        float radius;
        Vector2f movementTargetLocation;

        switch (orders) {
            case DEFENCE:
                angle = defenceOrbitAngle + ship.getFacing();
                radius = defenceOrbitRadius + ship.getShieldRadiusEvenIfNoShield();
                movementTargetLocation = MathUtils.getPointOnCircumference(ship.getLocation(), radius, angle);
                break;
            case SHIELD:
                angle = shieldOrbitAngle + ship.getFacing();
                radius = shieldOrbitRadius + ship.getShieldRadiusEvenIfNoShield();
                movementTargetLocation = MathUtils.getPointOnCircumference(ship.getLocation(), radius, angle);
                break;
            case RECALL:
                if (landingSlot == null) {
                    landingSlot = baseDroneSubsystem.getLandingBayWeaponSlotAPI();
                }

                movementTargetLocation = (landingSlot == null) ? new Vector2f(ship.getLocation()) : landingSlot.computePosition(ship);
                break;
            default:
                movementTargetLocation = ship.getMouseTarget();
        }

        return movementTargetLocation;
    }

    @Override
    protected void doRotationTargeting() {
        float targetFacing;

        switch (orders) {
            case DEFENCE:
                targetFacing = ship.getFacing() + defenceFacing;
                break;
            case SHIELD:
                targetFacing = ship.getFacing() + shieldOrbitAngle;
                break;
            case RECALL:
            default:
                targetFacing = ship.getFacing();
                break;
        }

        dl_DroneAIUtils.rotateToFacing(drone, targetFacing, Global.getCombatEngine());
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

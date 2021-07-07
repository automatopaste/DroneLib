package data.scripts.ai.example;

import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.scripts.ai.dl_BaseDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.shipsystems.dl_BaseDroneSystem;
import data.scripts.shipsystems.example.dl_DroneRift;
import data.scripts.util.dl_DroneAIUtils;
import data.scripts.util.dl_SpecLoadingUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class dl_DroneRiftDroneAI extends dl_BaseDroneAI {
    private final float[] fieldOrbitRadiusArray;
    private final float[] fieldOrbitSpeedArray;
    private final float[] defenceOrbitAngleArray;
    private final float[] defenceFacingArray;
    private final float[] defenceOrbitRadiusArray;
    private float fieldOrbitAngle;
    private float fieldOrbitRadius;
    private float defenceOrbitAngle;
    private float defenceFacing;
    private float defenceOrbitRadius;
    private WeaponSlotAPI landingSlot;
    private dl_DroneRift.RiftDroneOrders orders;

    public dl_DroneRiftDroneAI(dl_DroneAPI passedDrone, dl_BaseDroneSystem baseDroneSystem) {
        super(passedDrone, baseDroneSystem);

        fieldOrbitRadiusArray = dl_SpecLoadingUtils.dl_RiftSpecLoading.getFieldOrbitRadiusArray();
        fieldOrbitSpeedArray = dl_SpecLoadingUtils.dl_RiftSpecLoading.getFieldOrbitSpeedArray();
        defenceOrbitAngleArray = dl_SpecLoadingUtils.dl_RiftSpecLoading.getDefenceOrbitAngleArray();
        defenceFacingArray = dl_SpecLoadingUtils.dl_RiftSpecLoading.getDefenceFacingArray();
        defenceOrbitRadiusArray = dl_SpecLoadingUtils.dl_RiftSpecLoading.getDefenceOrbitRadiusArray();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        dl_DroneRift droneRiftSystem = (dl_DroneRift) engine.getCustomData().get(getUniqueSystemID());
        if (droneRiftSystem == null) {
            return;
        }
        baseDroneSystem = droneRiftSystem;

        //assign specific values
        droneIndex = baseDroneSystem.getIndex(drone);

        fieldOrbitRadius = fieldOrbitRadiusArray[droneIndex];
        float fieldOrbitSpeed = fieldOrbitSpeedArray[droneIndex];
        defenceOrbitAngle = defenceOrbitAngleArray[droneIndex];
        defenceFacing = defenceFacingArray[droneIndex];
        defenceOrbitRadius = defenceOrbitRadiusArray[droneIndex];

        orders = droneRiftSystem.getDroneOrders();

        switch (orders) {
            case DEFENCE:
                List<dl_DroneAPI> deployedDrones = baseDroneSystem.deployedDrones;
                float angleDivisor = 360f / deployedDrones.size();
                fieldOrbitAngle = droneIndex * angleDivisor;

                delayBeforeLandingTracker.setElapsed(0f);

                landingSlot = null;
                break;
            case ECCM_ARRAY:
                delayBeforeLandingTracker.setElapsed(0f);

                landingSlot = null;

                fieldOrbitAngle += fieldOrbitSpeed * amount;
                break;
            case RECALL:
                dl_DroneAIUtils.attemptToLand(ship, drone, amount, delayBeforeLandingTracker, engine);

                if (landingSlot == null) {
                    landingSlot = baseDroneSystem.getPlugin().getLandingBayWeaponSlotAPI();
                }
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
            case ECCM_ARRAY:
                angle = fieldOrbitAngle + ship.getFacing();
                radius = fieldOrbitRadius + ship.getShieldRadiusEvenIfNoShield();
                movementTargetLocation = MathUtils.getPointOnCircumference(ship.getLocation(), radius, angle);
                break;
            case RECALL:
                if (landingSlot == null) {
                    landingSlot = baseDroneSystem.getPlugin().getLandingBayWeaponSlotAPI();
                }

                movementTargetLocation = landingSlot.computePosition(ship);
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
            case ECCM_ARRAY:
                targetFacing = ship.getFacing() + fieldOrbitAngle;
                break;
            case RECALL:
            default:
                targetFacing = ship.getFacing();
                break;
        }

        dl_DroneAIUtils.rotateToFacing(drone, targetFacing, engine);
    }
}
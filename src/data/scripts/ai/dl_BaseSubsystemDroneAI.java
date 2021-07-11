package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.subsystems.dl_BaseDroneSubsystem;
import data.scripts.util.dl_DroneAIUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public abstract class dl_BaseSubsystemDroneAI implements ShipAIPlugin {
    protected dl_DroneAPI drone;
    protected ShipAPI ship;
    protected String uniqueSystemPrefix;
    protected dl_BaseDroneSubsystem baseDroneSubsystem;
    protected int droneIndex;

    protected final IntervalUtil delayBeforeLandingTracker = new IntervalUtil(2f, 2f);
    protected WeaponSlotAPI landingSlot;

    public dl_BaseSubsystemDroneAI(dl_DroneAPI passedDrone, dl_BaseDroneSubsystem baseDroneSubsystem) {
        this.drone = passedDrone;
        this.ship = drone.getDroneSource();

        this.baseDroneSubsystem = baseDroneSubsystem;
        this.uniqueSystemPrefix = baseDroneSubsystem.getSystemID();

        drone.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
    }

    @Override
    public void advance(float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused() || drone == null) return;

        //check for relocation
        if (ship == null || !engine.isEntityInPlay(ship) || !ship.isAlive()) {
            landingSlot = null;

            ship = dl_DroneAIUtils.getAlternateHost(drone, uniqueSystemPrefix, 4000f);

            if (ship == null || !engine.isEntityInPlay(ship) || !ship.isAlive()) {
                dl_DroneAIUtils.deleteDrone(drone, engine);
                return;
            }
        }

        if (!baseDroneSubsystem.getDeployedDrones().contains(drone)) {
            baseDroneSubsystem.getDeployedDrones().add(drone);
        }

        //check if currently superfluous
        droneIndex = baseDroneSubsystem.getIndex(drone);
        if (droneIndex == -1) {
            if (landingSlot == null) {
                landingSlot = baseDroneSubsystem.getLandingBayWeaponSlotAPI();
            }

            Vector2f movementTargetLocation = landingSlot.computePosition(ship);

            dl_DroneAIUtils.move(drone, drone.getFacing(), movementTargetLocation);

            Vector2f to = Vector2f.sub(movementTargetLocation, drone.getLocation(), new Vector2f());
            float angle = VectorUtils.getFacing(to);
            dl_DroneAIUtils.rotateToFacing(drone, angle, engine);

            dl_DroneAIUtils.attemptToLandAsExtra(ship, drone);
        }
    }

    protected abstract Vector2f getMovementTargetLocation(float amount);

    protected abstract void doRotationTargeting();

    @Override
    public ShipwideAIFlags getAIFlags() {
        ShipwideAIFlags flags = new ShipwideAIFlags();
        flags.setFlag(ShipwideAIFlags.AIFlags.DRONE_MOTHERSHIP);
        return flags;
    }
}

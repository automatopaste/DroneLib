package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.shipsystems.dl_BaseDroneSystem;
import data.scripts.util.dl_CombatUI;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author tomatopaste
 * one of these should be instantiated by every instance of a DroneLib shipsystem, used to manage launching, forging and ui
 */
public class dl_DroneLaunchManager extends BaseEveryFrameCombatPlugin {
    public static final String LAUNCH_DELAY_STAT_KEY = "dl_launchDelayStatKey";
    public static final String REGEN_DELAY_STAT_KEY = "dl_regenDelayStatKey";

    private final dl_BaseDroneSystem baseDroneSystem;

    private final IntervalUtil launchTracker;
    private final ShipAPI ship;
    private final String droneVariant;
    private final float launchSpeed;
    private final int maxDeployedDrones;
    private final int maxReserveDroneCount;
    private int reserveDroneCount;

    private final ArrayList<dl_DroneAPI> toRemove = new ArrayList<>();

    private float forgeCooldownRemaining;
    private float forgeCooldown;

    public dl_DroneLaunchManager(dl_BaseDroneSystem baseSystem) {
        if (baseSystem != null) {
            baseDroneSystem = baseSystem;
        } else {
            throw new NullPointerException("Unlucky: system object null");
        }

        this.launchSpeed = baseDroneSystem.launchSpeed;

        this.launchTracker = new IntervalUtil(baseDroneSystem.launchDelay, baseDroneSystem.launchDelay);
        launchTracker.forceIntervalElapsed();

        this.ship = baseDroneSystem.ship;

        forgeCooldown = baseDroneSystem.forgeCooldown;
        forgeCooldownRemaining = forgeCooldown;

        maxReserveDroneCount = baseSystem.maxReserveDrones;

        this.droneVariant = baseDroneSystem.droneVariant;
        this.maxDeployedDrones = baseDroneSystem.maxDeployedDrones;
        this.reserveDroneCount = maxDeployedDrones;
    }

    private boolean isActivationKeyDownPreviousFrame = false;

    public void advance(float amount, List<InputEventAPI> events) {
        if (ship == null || !ship.isAlive()) {
            return;
        }

        dl_CombatUI.drawSecondUnlimitedInterfaceStatusBar(
                ship,
                forgeCooldownRemaining/ forgeCooldown,
                null,
                null,
                (float) reserveDroneCount / maxReserveDroneCount,
                "FORGE",
                reserveDroneCount + "/" + maxReserveDroneCount
        );

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) {
            return;
        }

        //stat modifications
        float regenDelayStatMod = ship.getMutableStats().getDynamic().getMod(REGEN_DELAY_STAT_KEY).computeEffective(1f);
        float launchDelayStatMod = ship.getMutableStats().getDynamic().getMod(LAUNCH_DELAY_STAT_KEY).computeEffective(1f);
        forgeCooldown = baseDroneSystem.forgeCooldown * regenDelayStatMod;

        boolean isActivationKeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(Global.getSettings().getControlStringForEnumName("SHIP_USE_SYSTEM")));

        int numDronesActive;
        ArrayList<dl_DroneAPI> deployedDrones;

        if (engine.getPlayerShip().equals(ship)) {
            baseDroneSystem.maintainStatusMessage();
        }
        deployedDrones = baseDroneSystem.getDeployedDrones();
        updateDeployedDrones(deployedDrones);
        numDronesActive = deployedDrones.size();

        //trackSystemAmmo();
        if (forgeCooldownRemaining > 0f) {
            if (reserveDroneCount < maxReserveDroneCount) {
                forgeCooldownRemaining -= amount;
            }
        } else {
            if (reserveDroneCount < maxReserveDroneCount) {
                reserveDroneCount++;

                forgeCooldownRemaining = forgeCooldown;
            } else {
                forgeCooldownRemaining = 0f;
            }
        }

        //check if can spawn new drone
        if (numDronesActive < maxDeployedDrones && !baseDroneSystem.isRecallMode() && reserveDroneCount > 0) {
            if (launchTracker.getElapsed() >= launchTracker.getIntervalDuration()) {
                launchTracker.setElapsed(0);
                baseDroneSystem.getDeployedDrones().add(spawnDroneFromShip(droneVariant, engine));

                //subtract from reserve drone count on launch
                reserveDroneCount -= 1;
            }

            launchTracker.advance(amount / launchDelayStatMod);
        }

        if (isActivationKeyDown && !isActivationKeyDownPreviousFrame && ship.equals(engine.getPlayerShip())) {
            baseDroneSystem.nextDroneOrder();
        }

        if (!baseDroneSystem.isRecallMode()) {
            baseDroneSystem.applyActiveStatBehaviour();
        } else {
            baseDroneSystem.unapplyActiveStatBehaviour();
        }

        baseDroneSystem.setDeployedDrones(deployedDrones);

        //system.setAmmo(deployedDrones.size());

        engine.getCustomData().put("dl_DroneList_" + ship.hashCode(), deployedDrones);

        baseDroneSystem.executePerOrders(amount);

        if (ship.getFluxTracker().isOverloadedOrVenting()) {
            baseDroneSystem.setDefaultDeployMode();
        }

        //prevents triggering twice on first activation
        isActivationKeyDownPreviousFrame = isActivationKeyDown;
    }

    private void updateDeployedDrones(ArrayList<dl_DroneAPI> list) {
        //remove inactive drones from list
        for (dl_DroneAPI drone : list) {
            if (!drone.isAlive()) {
                toRemove.add(drone);
                continue;
            }
            //when drone has finished landing/shrinking animation
            if (drone.isFinishedLanding()) {
                //add to system ammo count / reserve
                reserveDroneCount += 1;

                drone.remove();

                toRemove.add(drone);
            }
        }
        if (!toRemove.isEmpty()) {
            list.removeAll(toRemove);
        }
    }

    private dl_DroneAPI spawnDroneFromShip(String specID, CombatEngineAPI engine) {
        engine.getFleetManager(ship.getOriginalOwner()).setSuppressDeploymentMessages(true);

        Vector2f location;
        float facing;
        if (getLandingBayWeaponSlotAPI() != null) {
            WeaponSlotAPI slot = getLandingBayWeaponSlotAPI();
            location = slot.computePosition(ship);
            facing = slot.getAngle();
        } else {
            location = ship.getLocation();
            facing = ship.getFacing();
        }

        dl_DroneAPI spawnedDrone = new dl_DroneAPI(
                engine.getFleetManager(ship.getOriginalOwner()).spawnShipOrWing(specID, location, facing),
                ship
        );
        spawnedDrone.setAnimatedLaunch();
        spawnedDrone.setLaunchingShip(ship);

        Vector2f launchVelocity = new Vector2f(ship.getVelocity());
        VectorUtils.clampLength(launchVelocity, launchSpeed);
        spawnedDrone.getVelocity().set(launchVelocity);

        spawnedDrone.setShipAI(baseDroneSystem.getNewDroneAIInstance(spawnedDrone, baseDroneSystem));

        spawnedDrone.setDroneSource(ship);
        spawnedDrone.setDrone();

        engine.getFleetManager(FleetSide.PLAYER).setSuppressDeploymentMessages(false);
        return spawnedDrone;
    }

    public WeaponSlotAPI getLandingBayWeaponSlotAPI() {
        List<WeaponSlotAPI> weapons = ship.getHullSpec().getAllWeaponSlotsCopy();
        if (!weapons.isEmpty()) {
            //these aren't actually bays, but since launch bays have no way of getting their location system mounts are used
            List<WeaponSlotAPI> bays = new ArrayList<>();
            for (WeaponSlotAPI weapon : weapons) {
                if (weapon.isSystemSlot()) {
                    bays.add(weapon);
                }
            }

            if (!bays.isEmpty()) {
                //pick random entry in bay list
                Random index = new Random();
                return bays.get(index.nextInt(bays.size()));
            }
        }
        return null;
    }

    public int getReserveDroneCount() {
        return reserveDroneCount;
    }
}
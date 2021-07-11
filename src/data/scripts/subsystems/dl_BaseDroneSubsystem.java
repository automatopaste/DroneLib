package data.scripts.subsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.ai.dl_BaseSubsystemDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.subsystems.ai.dl_BaseSubsystemAI;
import data.scripts.util.dl_CombatUI;
import data.scripts.util.dl_SpecLoadingUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class dl_BaseDroneSubsystem extends dl_BaseSubsystem {
    protected final dl_SpecLoadingUtils.DroneSystemSpec droneSubsystemData;

    protected IntervalUtil launchTracker;

    public dl_BaseDroneSubsystem(dl_SpecLoadingUtils.DroneSystemSpec droneSubsystemData, SubsystemData subsystemData) {
        super(subsystemData);
        this.droneSubsystemData = droneSubsystemData;
        launchTracker = new IntervalUtil((float) droneSubsystemData.launchDelay, (float) droneSubsystemData.launchDelay);

        reserveDroneCount = droneSubsystemData.maxDeployedDrones;
        forgeCooldownRemaining = (float) droneSubsystemData.forgeCooldown;
    }

    //////////
    //////////
    //SYSTEM LOGIC
    //////////
    //////////

    protected static final String STATUS_DISPLAY_KEY = "dl_DroneStatKey";
    protected static final String STATUS_DISPLAY_SPRITE = "graphics/icons/hullsys/drone_pd_high.png";
    protected static final String STATUS_DISPLAY_TITLE = "SYSTEM STATE";

    protected ArrayList<dl_DroneAPI> deployedDrones = new ArrayList<>();

    public abstract void nextDroneOrder();

    public abstract void maintainStatusMessage();

    public abstract boolean isRecallMode();

    public abstract void setDefaultDeployMode();

    public abstract void executePerOrders(float amount);

    public abstract dl_BaseSubsystemDroneAI getNewDroneAIInstance(dl_DroneAPI spawnedDrone);

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, SubsystemState state, float effectLevel) {

    }

    public int getIndex(dl_DroneAPI drone) {
        int index = 0;
        for (dl_DroneAPI deployedDrone : deployedDrones) {
            if (index >= droneSubsystemData.maxDeployedDrones) {
                break;
            }
            if (deployedDrone == drone) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public ArrayList<dl_DroneAPI> getDeployedDrones() {
        return deployedDrones;
    }

    public void setDeployedDrones(ArrayList<dl_DroneAPI> list) {
        this.deployedDrones = list;
    }

    protected void maintainSystemStateStatus(String state) {
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_DISPLAY_KEY, STATUS_DISPLAY_SPRITE, STATUS_DISPLAY_TITLE, state, false);
    }

    @Override
    public Vector2f guiRender(Vector2f inputLoc, Vector2f rootLoc) {
        if (ship == null || !Global.getCombatEngine().isUIShowingHUD() || Global.getCombatEngine().isUIShowingDialog()) return new Vector2f();

        Vector2f out = super.guiRender(inputLoc, rootLoc);
        Vector2f in = new Vector2f(out);
        in.y += 13f * Global.getSettings().getScreenScaleMult();
        dl_CombatUI.renderAuxiliaryStatusBar(
                ship,
                20f,
                150f,
                30f,
                forgeCooldownRemaining / forgeCooldown,
                "DRONE FORGE",
                reserveDroneCount + "/" + droneSubsystemData.maxReserveDroneCount,
                in
        );
        return out;
    }

    //////////
    //////////
    //LAUNCH MANAGEMENT
    //////////
    //////////

    public static final String LAUNCH_DELAY_STAT_KEY = "dl_launchDelayStatKey";
    public static final String REGEN_DELAY_STAT_KEY = "dl_regenDelayStatKey";

    private final ArrayList<dl_DroneAPI> toRemove = new ArrayList<>();

    private int reserveDroneCount;
    private float forgeCooldownRemaining;
    private float forgeCooldown;

    private boolean isActivationKeyDownPreviousFrame = false;

    @Override
    public void advance(float amount) {
        super.advance(amount);

        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine != null && ship != null) {
            engine.getCustomData().put(getSystemID() + ship.hashCode(), this);
        }

        launchManagerAdvance(amount);
    }

    public void launchManagerAdvance(float amount) {
        if (ship == null || !ship.isAlive()) return;

        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.isPaused()) return;

        //stat modifications
        float regenDelayStatMod = ship.getMutableStats().getDynamic().getMod(REGEN_DELAY_STAT_KEY).computeEffective(1f);
        float launchDelayStatMod = ship.getMutableStats().getDynamic().getMod(LAUNCH_DELAY_STAT_KEY).computeEffective(1f);
        forgeCooldown = (float) (droneSubsystemData.forgeCooldown * regenDelayStatMod);

        boolean isActivationKeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(getHotkeyString()));

        int numDronesActive;
        ArrayList<dl_DroneAPI> deployedDrones;

        if (engine.getPlayerShip().equals(ship)) {
            maintainStatusMessage();
        }
        deployedDrones = getDeployedDrones();
        updateDeployedDrones(deployedDrones);
        numDronesActive = deployedDrones.size();

        //trackSystemAmmo();
        if (forgeCooldownRemaining > 0f) {
            if (reserveDroneCount < droneSubsystemData.maxReserveDroneCount) {
                forgeCooldownRemaining -= amount;
            }
        } else {
            if (reserveDroneCount < droneSubsystemData.maxReserveDroneCount) {
                reserveDroneCount++;

                forgeCooldownRemaining = forgeCooldown;
            } else {
                forgeCooldownRemaining = 0f;
            }
        }

        //check if can spawn new drone
        if (numDronesActive < droneSubsystemData.maxDeployedDrones && !isRecallMode() && reserveDroneCount > 0) {
            if (launchTracker.getElapsed() >= launchTracker.getIntervalDuration()) {
                launchTracker.setElapsed(0f);
                getDeployedDrones().add(spawnDroneFromShip(droneSubsystemData.droneVariant, engine));

                //subtract from reserve drone count on launch
                reserveDroneCount -= 1;
            }

            launchTracker.advance(amount / launchDelayStatMod);
        }

        if (isActivationKeyDown && !isActivationKeyDownPreviousFrame && ship.equals(engine.getPlayerShip())) {
            nextDroneOrder();
        }

        setDeployedDrones(deployedDrones);

        engine.getCustomData().put("dl_DroneList_" + ship.hashCode(), deployedDrones);

        executePerOrders(amount);

        if (ship.getFluxTracker().isOverloadedOrVenting()) {
            setDefaultDeployMode();
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
        VectorUtils.clampLength(launchVelocity, (float) droneSubsystemData.launchSpeed);
        spawnedDrone.getVelocity().set(launchVelocity);

        spawnedDrone.setShipAI(getNewDroneAIInstance(spawnedDrone));

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

    //////////
    //////////
    //SUBSYSTEM AI
    //////////
    //////////

    @Override
    public dl_BaseSubsystemAI getAI() {
        return this;
    }

    @Override
    public String getInfoString() {
        int reserve = getReserveDroneCount();

        if (reserve < droneSubsystemData.maxReserveDroneCount) {
            return "FORGING";
        } else if (reserve > droneSubsystemData.maxReserveDroneCount) {
            return "OVER CAP";
        } else {
            return "AT CAPACITY";
        }
    }

    @Override
    public int getNumGuiBars() {
        return 2;
    }
}
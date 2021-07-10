package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.ai.dl_BaseDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.plugins.dl_DroneLaunchManager;
import data.scripts.util.dl_SpecLoadingUtils;

import java.util.ArrayList;
import java.util.Map;

public abstract class dl_BaseDroneSystem extends BaseShipSystemScript {
    protected static final String STATUS_DISPLAY_KEY = "PSE_DroneStatKey";
    protected static final String STATUS_DISPLAY_SPRITE = "graphics/icons/hullsys/drone_pd_high.png";
    protected static final String STATUS_DISPLAY_TITLE = "SYSTEM STATE";

    public ArrayList<dl_DroneAPI> deployedDrones = new ArrayList<>();

    public ShipAPI ship;

    public int maxDeployedDrones;
    public int maxReserveDrones;
    public float forgeCooldown;
    public float launchDelay;
    public float launchSpeed;
    public String droneVariant;

    protected dl_DroneLaunchManager plugin = null;

    protected void loadSpecData() {
        dl_SpecLoadingUtils.DroneSystemSpec spec = dl_SpecLoadingUtils.droneSystemSpecHashMap.get(getSystemID());

        maxDeployedDrones = spec.maxDeployedDrones;
        maxReserveDrones = spec.maxReserveDroneCount;
        forgeCooldown = (float) spec.forgeCooldown;
        launchDelay = (float) spec.launchDelay;
        launchSpeed = (float) spec.launchSpeed;
        droneVariant = spec.droneVariant;
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //initialisation and engine data stuff
        this.ship = (ShipAPI) stats.getEntity();
        CombatEngineAPI engine = Global.getCombatEngine();

        if (engine != null) {
            ensurePluginExistence();

            String UNIQUE_SYSTEM_ID = getSystemID() + ship.hashCode();
            engine.getCustomData().put(UNIQUE_SYSTEM_ID, this);
        }
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {

    }

    public int getIndex(dl_DroneAPI drone) {
        int index = 0;
        for (dl_DroneAPI deployedDrone : deployedDrones) {
            if (index >= maxDeployedDrones) {
                break;
            }
            if (deployedDrone == drone) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public abstract void nextDroneOrder();

    public abstract void maintainStatusMessage();

    public abstract boolean isRecallMode();

    public abstract void setDefaultDeployMode();

    public abstract void executePerOrders(float amount);

    public abstract dl_BaseDroneAI getNewDroneAIInstance(dl_DroneAPI spawnedDrone, dl_BaseDroneSystem baseDroneSystem);

    public abstract String getSystemID();

    public void applyActiveStatBehaviour() {
        ship.getMutableStats().getShieldUnfoldRateMult().modifyPercent(this.toString(),-25f);
        ship.getMutableStats().getShieldTurnRateMult().modifyPercent(this.toString(), -25f);
    }

    public void unapplyActiveStatBehaviour() {
        ship.getMutableStats().getShieldUnfoldRateMult().unmodify(this.toString());
        ship.getMutableStats().getShieldTurnRateMult().unmodify(this.toString());
    }

    public dl_DroneLaunchManager getPlugin() {
        return plugin;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (plugin == null) return "NULL";

        int reserve = plugin.getReserveDroneCount();

        if (reserve < maxReserveDrones) {
            return "FORGING";
        } else if (reserve > maxReserveDrones) {
            return "OVER CAP";
        } else {
            return "AT CAPACITY";
        }
    }

    public ArrayList<dl_DroneAPI> getDeployedDrones() {
        return deployedDrones;
    }

    public void setDeployedDrones(ArrayList<dl_DroneAPI> list) {
        this.deployedDrones = list;
    }

    public void ensurePluginExistence() {
        if (plugin == null) {
            plugin = new dl_DroneLaunchManager(this);
            Global.getCombatEngine().addPlugin(plugin);
        }
    }

    protected void maintainSystemStateStatus(String state) {
        Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_DISPLAY_KEY, STATUS_DISPLAY_SPRITE, STATUS_DISPLAY_TITLE, state, false);
    }
}
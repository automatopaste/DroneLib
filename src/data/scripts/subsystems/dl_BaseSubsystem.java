package data.scripts.subsystems;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.dl_SubsystemCombatManager;
import data.scripts.subsystems.ai.dl_BaseSubsystemAI;
import data.scripts.util.dl_CombatUI;
import data.scripts.util.dl_SpecLoadingUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public abstract class dl_BaseSubsystem implements dl_Subsystem, dl_BaseSubsystemAI {
    private String sysId;
    private final SubsystemData data;

    public enum SubsystemState {
        OFF,
        IN,
        ACTIVE,
        OUT,
        COOLDOWN
    }

    protected SubsystemState state;
    protected ShipAPI ship;
    protected int index;

    public dl_BaseSubsystem(String systemId) {
        this.data = dl_SpecLoadingUtils.getSubsystemData(systemId);

        state = SubsystemState.OFF;
    }
    public dl_BaseSubsystem(SubsystemData data) {
        this.data = data;

        state = SubsystemState.OFF;
    }

    @Override
    public void init(ShipAPI ship) {
        this.ship = ship;
        sysId = "dl_Subsystem_" + this.hashCode() + "_" + ship.hashCode();

        aiInit();
    }

    @Override
    public abstract void apply(MutableShipStatsAPI stats, String id, SubsystemState state, float effectLevel);

    @Override
    public abstract void unapply(MutableShipStatsAPI stats, String id);

    @Override
    public dl_BaseSubsystemAI getAI() {
        return this;
    }

    @Override
    public abstract void aiInit();

    @Override
    public abstract void aiUpdate(float amount);

    private boolean isHotkeyDownLastUpdate = false;
    private float active = 0f;
    private float effectLevel = 0f;
    private float guiLevel = 0f;

    /**
     * Checks if activation is legal then will start subsystem cycle
     */
    protected void activate() {
        if (isOff() && !isCooldown()) {
            state = SubsystemState.IN;
        }
        if (isToggle() && isActive()) {
            state = SubsystemState.OUT;
            active = 0f;
        }
    }

    @Override
    public void advance(float amount) {
        if (ship == null || !ship.isAlive()) return;

        boolean isHotkeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(getHotkeyString()));
        if (isHotkeyDown && !isHotkeyDownLastUpdate) {
            activate();
        }

        switch (state) {
            case OFF:
                if (isHotkeyDown && !isHotkeyDownLastUpdate) {
                    state = SubsystemState.IN;
                }

                guiLevel = 0f;
                effectLevel = 0f;
                active = 0f;
                break;
            case IN:
                active += amount;

                if (isToggle()) {
                    guiLevel = active / getInTime();
                } else {
                    guiLevel = active / (getInTime() + getActiveTime());
                }

                effectLevel = active / getInTime();

                if (active >= getInTime()) {
                    state = SubsystemState.ACTIVE;
                    active = 0f;
                }
                break;
            case ACTIVE:
                active  += amount;

                if (isToggle()) {
                    guiLevel = 1f;
                } else {
                    guiLevel = (getInTime() + active) / (getInTime() + getActiveTime());
                }

                effectLevel = 1f;

                if (active >= getActiveTime()) {
                    state = SubsystemState.OUT;
                    active = 0f;
                }
                break;
            case OUT:
                active += amount;

                guiLevel = 1f - (active / (getCooldownTime() + getOutTime()));

                effectLevel = 1f - (active / getOutTime());

                if (active >= getOutTime()) {
                    state = SubsystemState.COOLDOWN;
                    active = 0f;
                }
                break;
            case COOLDOWN:
                active += amount;

                guiLevel = 1f - ((active + getOutTime()) / (getCooldownTime() + getOutTime()));

                effectLevel = 0f;

                if (active >= getCooldownTime()) {
                    state = SubsystemState.OFF;
                    active = 0f;
                }
                break;
        }

        if (isOn()) {
            apply(ship.getMutableStats(), sysId, state, effectLevel);

            ship.getFluxTracker().increaseFlux(amount * getFluxPerSecondFlat(), false);
            ship.getFluxTracker().increaseFlux(amount * getFluxPerSecondMaxCapacity() * 0.01f * ship.getMaxFlux(), false);
        } else {
            unapply(ship.getMutableStats(), sysId);
        }

        //CombatEngineAPI engine = Global.getCombatEngine();
        //if (engine.getPlayerShip().equals(ship)) engine.maintainStatusForPlayerShip(sysId, null, getName(), state.name(), false);

        aiUpdate(amount);

        isHotkeyDownLastUpdate = isHotkeyDown;
    }

    public Vector2f guiRender(Vector2f inputLoc, Vector2f rootLoc) {
        String info = getInfoString();
        String flavour = getFlavourString();
        if (info == null || info.isEmpty()) info = "COOL INFO PLACEHOLDER";
        if (flavour == null || flavour.isEmpty()) flavour = "COOL FLAVOUR PLACEHOLDER";

        String stateText = "DEV_INIT";
        if (isToggle()) {
            switch (state) {
                case ACTIVE:
                case OFF:
                    stateText = "READY";
                    break;
                case COOLDOWN:
                case OUT:
                case IN:
                    stateText = "--";
            }
        } else {
            switch (state) {
                case OFF:
                    stateText = "READY";
                    break;
                case COOLDOWN:
                    stateText = "--";
                    break;
                case OUT:
                case ACTIVE:
                case IN:
                    stateText = "ACTIVE";
            }
        }

        String customStatus = getStatusString();
        if (customStatus != null) stateText = customStatus;

        return dl_CombatUI.drawSubsystemStatus(
                ship,
                guiLevel,
                getName(),
                info,
                stateText,
                getHotkeyString(),
                flavour,
                dl_SubsystemCombatManager.showInfoText,
                getNumGuiBars(),
                inputLoc,
                rootLoc
        );
    }

    @Override
    public boolean isOff() {
        return state == SubsystemState.OFF || state == SubsystemState.COOLDOWN;
    }

    @Override
    public boolean isOn() {
        return !isOff();
    }

    @Override
    public boolean isFadingIn() {
        return state == SubsystemState.IN;
    }

    @Override
    public boolean isActive() {
        return state == SubsystemState.ACTIVE;
    }

    @Override
    public boolean isFadingOut() {
        return state == SubsystemState.OUT;
    }

    @Override
    public boolean isCooldown() {
        return state == SubsystemState.COOLDOWN;
    }

    public static class SubsystemData {
        private final String id;
        private final String name;

        private String hotkey;

        private final float inTime;
        private final float activeTime;
        private final float outTime;
        private final float cooldownTime;

        private final boolean isToggle;

        private final float fluxPerSecondMaxCapacity;
        private final float fluxPerSecondFlat;

        public SubsystemData(
                String hotkey,
                String systemID,
                String name,
                float inTime,
                float activeTime,
                float outTime,
                float cooldownTime,
                boolean isToggle,
                float fluxPerSecondMaxCapacity,
                float fluxPerSecondFlat
        ) {
            this.hotkey = hotkey;
            this.id = systemID;
            this.name = name;
            this.inTime = inTime;
            this.activeTime = activeTime;
            this.outTime = outTime;
            this.cooldownTime = cooldownTime;
            this.isToggle = isToggle;
            this.fluxPerSecondMaxCapacity = fluxPerSecondMaxCapacity;
            this.fluxPerSecondFlat = fluxPerSecondFlat;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getHotkey() {
            return hotkey;
        }

        public void setHotkey(String hotkey) {
            this.hotkey = hotkey;
        }

        public float getInTime() {
            return inTime;
        }

        public float getActiveTime() {
            return activeTime;
        }

        public float getOutTime() {
            return outTime;
        }

        public float getCooldownTime() {
            return cooldownTime;
        }

        public boolean isToggle() {
            return isToggle;
        }

        public float getFluxPerSecondMaxCapacity() {
            return fluxPerSecondMaxCapacity;
        }

        public float getFluxPerSecondFlat() {
            return fluxPerSecondFlat;
        }
    }

    public void setHotkey(String hotkey) {
        data.setHotkey(hotkey);
    }

    public String getHotkeyString() {
        return data.getHotkey();
    }

    public String getSystemID() {
       return data.getId();
    }

    public String getName() {
        return data.getName();
    }

    public float getInTime() {
        return data.getInTime();
    }

    public float getActiveTime() {
        return data.getActiveTime();
    }

    public float getOutTime() {
        return data.getOutTime();
    }

    public float getCooldownTime() {
        return data.getCooldownTime();
    }

    public boolean isToggle() {
        return data.isToggle();
    }

    public float getFluxPerSecondMaxCapacity() {
        return data.getFluxPerSecondMaxCapacity();
    }

    public float getFluxPerSecondFlat() {
        return data.getFluxPerSecondFlat();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
package data.scripts.subsystems.example;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import data.scripts.subsystems.dl_BaseSubsystem;

public class mymod_EpicPhaseCloakSubsystem extends dl_BaseSubsystem {
    public static final float SHIP_ALPHA_MULT = 0.25f;

    public static final float MAX_TIME_MULT = 3f;

    public mymod_EpicPhaseCloakSubsystem() {
        super("pcloak");
    }

    public static float getMaxTimeMult(MutableShipStatsAPI stats) {
        return 1f + (MAX_TIME_MULT - 1f) * stats.getDynamic().getValue(Stats.PHASE_TIME_BONUS_MULT);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, SubsystemState state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
        } else {
            return;
        }

        if (Global.getCombatEngine().isPaused()) {
            return;
        }

        if (state == SubsystemState.COOLDOWN || state == SubsystemState.OFF) {
            unapply(stats, id);
            return;
        }

        float speedPercentMod = stats.getDynamic().getMod(Stats.PHASE_CLOAK_SPEED_MOD).computeEffective(0f);
        stats.getMaxSpeed().modifyPercent(id, speedPercentMod * effectLevel);

        float levelForAlpha = effectLevel;

        if (state == SubsystemState.IN || state == SubsystemState.ACTIVE) {
            ship.setPhased(true);
            levelForAlpha = effectLevel;
        } else if (state == SubsystemState.OUT) {
            ship.setPhased(effectLevel > 0.5f);
            levelForAlpha = effectLevel;
        }

        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
        ship.setApplyExtraAlphaToEngines(true);

        float shipTimeMult = 1f + (getMaxTimeMult(stats) - 1f) * levelForAlpha;
        stats.getTimeMult().modifyMult(id, shipTimeMult);
        if (player) {
            Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
        } else {
            Global.getCombatEngine().getTimeMult().unmodify(id);
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        Global.getCombatEngine().getTimeMult().unmodify(id);
        stats.getTimeMult().unmodify(id);

        stats.getMaxSpeed().unmodifyPercent(id);

        ship.setPhased(false);
        ship.setExtraAlphaMult(1f);
    }

    @Override
    public String getInfoString() {
        return "PHASE CLOAK BAYBEE";
    }

    @Override
    public String getFlavourString() {
        return "SHIFT SHIP TO P-SPACE";
    }

    @Override
    public String getStatusString() {
        return null;
    }

    @Override
    public void aiInit() {

    }

    @Override
    public void aiUpdate(float amount) {

    }

    @Override
    public int getNumGuiBars() {
        return 1;
    }
}

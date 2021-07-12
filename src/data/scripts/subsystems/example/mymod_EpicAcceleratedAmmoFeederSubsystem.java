package data.scripts.subsystems.example;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.subsystems.dl_BaseSubsystem;
import data.scripts.util.dl_SpecLoadingUtils;

import java.awt.*;
import java.util.EnumSet;

public class mymod_EpicAcceleratedAmmoFeederSubsystem extends dl_BaseSubsystem {
    public static final String SUBSYSTEM_ID = "accammo"; //this should match the id in the csv

    public static final float ROF_BONUS = 1f;
    public static final float FLUX_REDUCTION = 50f;

    public mymod_EpicAcceleratedAmmoFeederSubsystem() {
        super(SUBSYSTEM_ID);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, SubsystemState state, float effectLevel) {
        float mult = 1f + ROF_BONUS * effectLevel;
        stats.getBallisticRoFMult().modifyMult(id, mult);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, 1f - (FLUX_REDUCTION * 0.01f));

        ship.setWeaponGlow(effectLevel, new Color(255, 200, 0, 255), EnumSet.allOf(WeaponAPI.WeaponType.class));
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
    }

    @Override
    public String getInfoString() {
        if (isOn()) return "BRRRRRTING";
        else return "PREPARING DAKKA";
    }

    @Override
    public String getFlavourString() {
        return "BALLISTIC WEAPON BOOST";
    }

    @Override
    public int getNumGuiBars() {
        return 1;
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
        if (ship == null || !ship.isAlive()) return;

        if (ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.MANEUVER_TARGET) && ship.getFluxLevel() < 0.8f) activate();
    }
}
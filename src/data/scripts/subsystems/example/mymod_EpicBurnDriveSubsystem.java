package data.scripts.subsystems.example;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import data.scripts.subsystems.dl_BaseSubsystem;

public class mymod_EpicBurnDriveSubsystem extends dl_BaseSubsystem {
    public static final String SUBSYSTEM_ID = "bdrive"; //this should match the id in the csv

    public mymod_EpicBurnDriveSubsystem() {
        super(SUBSYSTEM_ID);
    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, SubsystemState state, float effectLevel) {
        if (state == SubsystemState.OUT) {
            stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
        } else {
            stats.getMaxSpeed().modifyFlat(id, 200f * effectLevel);
            stats.getAcceleration().modifyFlat(id, 200f * effectLevel);
        }

        ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
        stats.getTurnAcceleration().modifyMult(id, 0f);
        stats.getMaxTurnRate().modifyMult(id, 0.5f);

        ship.getEngineController().extendFlame(this, 2f * effectLevel, 2f * effectLevel, 3f * effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
    }

    @Override
    public String getInfoString() {
        return "HIT THE NOS";
    }

    @Override
    public String getFlavourString() {
        return "ENGINE BOOST TOGGLE";
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

    }
}

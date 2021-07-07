package data.scripts.shipsystems.example;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.ai.dl_BaseDroneAI;
import data.scripts.ai.example.PSE_DroneRiftDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.shipsystems.dl_BaseDroneSystem;
import org.lazywizard.lazylib.MathUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class PSE_DroneRift extends dl_BaseDroneSystem {
    public static final String UNIQUE_SYSTEM_PREFIX = "PSE_droneRift";

    private static final float FIELD_EFFECT_RADIUS = 1000f;
    private static final float FIELD_EFFECT_BONUS_PERCENT = 35f;
    private static final float MISSILE_FX_RADIUS = 40f;
    private static final float FIGHTER_FX_RADIUS = 80f;
    private static final Color FIELD_EFFECT_COLOUR = new Color(0, 255, 144, 255);
    private static final Color FIELD_ENEMY_EFFECT_COLOUR = new Color(255, 102, 0, 255);

    public enum RiftDroneOrders {
        DEFENCE,
        ECCM_ARRAY,
        RECALL
    }

    private RiftDroneOrders droneOrders = RiftDroneOrders.RECALL;

    private final IntervalUtil droneParticleInterval = new IntervalUtil(1.5f, 1.5f);
    private final IntervalUtil fieldParticleInterval = new IntervalUtil(1f, 1f);
    private final IntervalUtil missileParticleInterval = new IntervalUtil(0.6f, 0.6f);
    //private PSE_RingVisual visual;

    public PSE_DroneRift() {
        systemID = UNIQUE_SYSTEM_PREFIX;

        loadSpecData();
    }

    @Override
    public void nextDroneOrder() {
        droneOrders = getNextDroneOrder();
    }

    private RiftDroneOrders getNextDroneOrder() {
        if (droneOrders.ordinal() == PSE_DroneRift.RiftDroneOrders.values().length - 1) {
            return RiftDroneOrders.values()[0];
        }
        return RiftDroneOrders.values()[droneOrders.ordinal() + 1];
    }

    @Override
    public void maintainStatusMessage() {
        switch (droneOrders) {
            case ECCM_ARRAY:
                maintainSystemStateStatus("ECCM ARRAY FORMATION");
                break;
            case DEFENCE:
                maintainSystemStateStatus("DEFENCE FORMATION");
                break;
            case RECALL:
                if (deployedDrones.isEmpty()) {
                    maintainSystemStateStatus("DRONES RECALLED");
                } else {
                    maintainSystemStateStatus("RECALLING DRONES");
                }
                break;
        }
    }

    @Override
    public boolean isRecallMode() {
        return droneOrders == RiftDroneOrders.RECALL;
    }

    @Override
    public void setDefaultDeployMode() {
        droneOrders = RiftDroneOrders.DEFENCE;
    }

    @Override
    public void executePerOrders(float amount) {
        droneParticleInterval.advance(amount);
        if (droneParticleInterval.intervalElapsed()) {
            for (dl_DroneAPI drone : deployedDrones) {
                /*PSE_CombatEffectsPlugin.spawnEntityTrackingPrimitiveParticle(
                        drone,
                        2f,
                        FIELD_ENEMY_EFFECT_COLOUR,
                        CombatEngineLayers.UNDER_SHIPS_LAYER,
                        300f,
                        40f,
                        6,
                        0.2f,
                        0f,
                        0.2f,
                        20f
                );*/
            }
        }

        switch (droneOrders) {
            case DEFENCE:
                /*if (visual != null) {
                    visual.expire();
                    visual = null;
                }*/

                fieldParticleInterval.setElapsed(0f);
                missileParticleInterval.setElapsed(0f);
                break;
            case ECCM_ARRAY:
                fieldParticleInterval.advance(amount);
                missileParticleInterval.advance(amount);

                if (fieldParticleInterval.intervalElapsed()) {
                    /*PSE_CombatEffectsPlugin.spawnPrimitiveParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            new Vector2f(),
                            2f,
                            new Color(0, 255, 144, 255),
                            CombatEngineLayers.BELOW_SHIPS_LAYER,
                            FIELD_EFFECT_RADIUS,
                            FIELD_EFFECT_RADIUS / 2f,
                            12,
                            ship.getFacing(),
                            20f + ship.getAngularVelocity(),
                            0f,
                            0.5f,
                            0f,
                            0.1f,
                            3f
                    );
                    PSE_CombatEffectsPlugin.spawnPrimitiveParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            new Vector2f(),
                            2f,
                            new Color(0, 255, 144, 255),
                            CombatEngineLayers.BELOW_SHIPS_LAYER,
                            FIELD_EFFECT_RADIUS,
                            FIELD_EFFECT_RADIUS / 2f,
                            12,
                            ship.getFacing(),
                            -20f +ship.getAngularVelocity(),
                            0f,
                            0.5f,
                            0f,
                            0.1f,
                            3f
                    );*/
                }

                /*if (visual == null) {
                    visual = new PSE_RingVisual(1000f, ship, 36,0.05f, 0.1f,  new Color(0, 255, 144, 255), 5f);
                    visual.init(ship);

                    Global.getCombatEngine().addLayeredRenderingPlugin(visual);
                }*/

                List<ShipAPI> shipsWithBuffedMissiles = new ArrayList<>();
                for (MissileAPI missile : Global.getCombatEngine().getMissiles()) {
                    if (MathUtils.getDistanceSquared(ship.getLocation(), missile.getLocation()) < FIELD_EFFECT_RADIUS * FIELD_EFFECT_RADIUS) {
                        if (!shipsWithBuffedMissiles.contains(missile.getSourceAPI())) shipsWithBuffedMissiles.add(missile.getSourceAPI());
                    }
                }

                ListIterator<ShipAPI> iterator = shipsWithBuffedMissiles.listIterator();
                while (iterator.hasNext()) {
                    ShipAPI next = iterator.next();

                    if (next == null || !Global.getCombatEngine().isEntityInPlay(next) || !next.isAlive() || next.isHulk()) iterator.remove();
                }

                for (ShipAPI ship : shipsWithBuffedMissiles) {
                    for (MissileAPI missile : Global.getCombatEngine().getMissiles()) {
                        if (missile.getSource().equals(ship)) {
                            missile.setEccmChanceBonus(100f);
                            missile.setEccmChanceOverride(100f);
                            missile.getSource().getMutableStats().getMissileAccelerationBonus().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                            missile.getSource().getMutableStats().getMissileTurnAccelerationBonus().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                            missile.getSource().getMutableStats().getMissileMaxSpeedBonus().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                            missile.getSource().getMutableStats().getMissileMaxTurnRateBonus().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);

                            Color c = (ship.getOwner() == this.ship.getOwner()) ? FIELD_EFFECT_COLOUR : FIELD_ENEMY_EFFECT_COLOUR;

                            if (missileParticleInterval.intervalElapsed()) {
                                /*PSE_CombatEffectsPlugin.spawnEntityTrackingPrimitiveParticle(
                                        missile,
                                        0.8f,
                                        c,
                                        CombatEngineLayers.BELOW_SHIPS_LAYER,
                                        MISSILE_FX_RADIUS,
                                        MISSILE_FX_RADIUS / 2f,
                                        6,
                                        0.5f,
                                        0f,
                                        0.4f,
                                        3f
                                );*/
                            }
                        }
                    }
                }

                for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                    if (!ship.isFighter() || ship.isHulk() || !ship.isAlive() || ship.getHullSpec().getHullId().contains("_drone")) continue;

                    if (MathUtils.getDistanceSquared(this.ship.getLocation(), ship.getLocation()) < FIELD_EFFECT_RADIUS * FIELD_EFFECT_RADIUS) {
                        ship.getMutableStats().getTurnAcceleration().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                        ship.getMutableStats().getMaxSpeed().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                        ship.getMutableStats().getAcceleration().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);
                        ship.getMutableStats().getMaxTurnRate().modifyPercent(PSE_DroneRift.class.toString(), FIELD_EFFECT_BONUS_PERCENT);

                        Color c = (ship.getOwner() == this.ship.getOwner()) ? FIELD_EFFECT_COLOUR : FIELD_ENEMY_EFFECT_COLOUR;

                        if (fieldParticleInterval.intervalElapsed()) {
                            /*PSE_CombatEffectsPlugin.spawnEntityTrackingPrimitiveParticle(
                                    ship,
                                    2f,
                                    c,
                                    CombatEngineLayers.BELOW_SHIPS_LAYER,
                                    FIGHTER_FX_RADIUS,
                                    FIGHTER_FX_RADIUS / 2f,
                                    6,
                                    0.5f,
                                    0f,
                                    0.2f,
                                    3f
                            );*/
                        }
                    }
                }
                break;
            case RECALL:
                /*if (visual != null) {
                    visual.expire();
                    visual = null;
                }*/

                fieldParticleInterval.setElapsed(0f);
                missileParticleInterval.setElapsed(0f);
                break;
        }
    }

    @Override
    public dl_BaseDroneAI getNewAIInstance(dl_DroneAPI spawnedDrone, dl_BaseDroneSystem baseDroneSystem) {
        return new PSE_DroneRiftDroneAI(spawnedDrone, baseDroneSystem);
    }

    public RiftDroneOrders getDroneOrders() {
        return droneOrders;
    }
}

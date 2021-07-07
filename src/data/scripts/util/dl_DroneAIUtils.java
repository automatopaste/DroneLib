package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.impl.dl_DroneAPI;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public final class dl_DroneAIUtils {
    //private static final Color DRONE_EXPLOSION_COLOUR = new Color(183, 255, 153, 255);

    /**
     * @param drone drone instance, acceleration stats are used to move precisely
     * @param droneFacing absolute facing angle
     * @param movementTargetLocation location to go to
     */
    public static void move(dl_DroneAPI drone, float droneFacing, Vector2f movementTargetLocation) {
        //The bones of the movement AI are below, all it needs is a target vector location to move to

        //account for ship velocity
        Vector2f.add(movementTargetLocation, (Vector2f) new Vector2f(drone.getLaunchingShip().getVelocity()).scale(Global.getCombatEngine().getElapsedInLastFrame()), movementTargetLocation);

        //GET USEFUL VALUES
        float angleFromDroneToTargetLocation = VectorUtils.getAngle(drone.getLocation(), movementTargetLocation); //ABSOLUTE 360 ANGLE

        float droneVelocityAngle = VectorUtils.getFacing(drone.getVelocity()); //ABSOLUTE 360 ANGLE

        float rotationFromFacingToLocationAngle = MathUtils.getShortestRotation(droneFacing, angleFromDroneToTargetLocation); //ROTATION ANGLE
        float rotationFromVelocityToLocationAngle = MathUtils.getShortestRotation(droneVelocityAngle, angleFromDroneToTargetLocation); //ROTATION ANGLE

        float distanceToTargetLocation = MathUtils.getDistance(drone.getLocation(), movementTargetLocation); //DISTANCE

        //damping scaling based on ship speed (function y = -x + 2 where x is 0->1)
        //float damping = (-drone.getLaunchingShip().getVelocity().length() / drone.getLaunchingShip().getMaxSpeedWithoutBoost()) + 2f;

        //FIND DISTANCE THAT CAN BE DECELERATED FROM CURRENT SPEED TO ZERO s = v^2 / 2a
        float speedSquared = drone.getVelocity().lengthSquared();
        float decelerationDistance = speedSquared / (2 * drone.getDeceleration());

        //DO LARGE MOVEMENT IF OVER DISTANCE THRESHOLD
        if (distanceToTargetLocation >= decelerationDistance) {
            rotationFromFacingToLocationAngle = Math.round(rotationFromFacingToLocationAngle);

            //COURSE CORRECTION
            drone.getVelocity().set(VectorUtils.rotate(drone.getVelocity(), rotationFromVelocityToLocationAngle * 0.5f));

            //accelerate forwards or backwards
            if (90f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > -90f
            ) { //between 90 and -90 is an acute angle therefore in front
                drone.giveCommand(ShipCommand.ACCELERATE, null, 0);
            } else if ((180f >= rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > 90f) || (-90f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle >= -180f)
            ) { //falls between 90 to 180 or -90 to -180, which should be obtuse and thus relatively behind
                drone.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
            }

            //strafe left or right
            if (180f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > 0f) { //between 0 and 180 (i.e. left)
                drone.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
            } else if (0f > rotationFromFacingToLocationAngle && rotationFromFacingToLocationAngle > -180f) { //between 0 and -180 (i.e. right)
                drone.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
            }
        } else {
            //COURSE CORRECTION
            drone.getVelocity().set(VectorUtils.rotate(drone.getVelocity(), rotationFromVelocityToLocationAngle));
        }

        //DECELERATE IF IN THRESHOLD DISTANCE OF TARGET
        if (distanceToTargetLocation <= decelerationDistance) {
            drone.giveCommand(ShipCommand.DECELERATE, null, 0);

            float frac = distanceToTargetLocation / decelerationDistance;
            frac = (float) Math.sqrt(frac);
            //drone.getVelocity().set((Vector2f) drone.getVelocity().scale(frac));

            if (frac <= 0.25f) {
                drone.getVelocity().set(drone.getLaunchingShip().getVelocity());
            } else {
                drone.getVelocity().set((Vector2f) drone.getVelocity().scale(frac));
            }
        }
    }

    /**
     * @param drone drone to move
     * @param target location to go to
     */
    public static void snapToLocation(dl_DroneAPI drone, Vector2f target) {
        drone.getLocation().set(target);
    }

    public static void rotateToTarget(ShipAPI ship, dl_DroneAPI drone, Vector2f targetedLocation) {
        CombatEngineAPI engine = Global.getCombatEngine();
        //float droneFacing = drone.getFacing();

        //FIND ANGLE THAT CAN BE DECELERATED FROM CURRENT ANGULAR VELOCITY TO ZERO theta = v^2 / 2 (not actually used atm)
        //float decelerationAngle = (float) (Math.pow(drone.getAngularVelocity(), 2) / (2 * drone.getTurnDeceleration()));


        //point at target, if that doesn't exist then point in direction of mothership facing
        //float rotationAngleDelta;
        if (targetedLocation != null) {
            //GET ABSOLUTE ANGLE FROM DRONE TO TARGETED LOCATION
            Vector2f droneToTargetedLocDir = VectorUtils.getDirectionalVector(drone.getLocation(), targetedLocation);
            float droneAngleToTargetedLoc = VectorUtils.getFacing(droneToTargetedLocDir); //ABSOLUTE 360 ANGLE

            rotateToFacing(drone, droneAngleToTargetedLoc, engine);
        } else {
            rotateToFacing(drone, ship.getFacing(), engine);
        }
    }

    /**
     * @param drone drone to rotate, accelerations stats are used to rotate
     * @param absoluteFacingTargetAngle target angle to point to
     * @param engine combat engine
     */
    public static void rotateToFacing(dl_DroneAPI drone, float absoluteFacingTargetAngle, CombatEngineAPI engine) {
        float droneFacing = drone.getFacing();
        float angvel = drone.getAngularVelocity();
        float rotationAngleDelta = MathUtils.getShortestRotation(droneFacing, absoluteFacingTargetAngle);

        //FIND ANGLE THAT CAN BE DECELERATED FROM CURRENT ANGULAR VELOCITY TO ZERO theta = v^2 / 2
        float decelerationAngleAbs = (angvel * angvel) / (2 * drone.getTurnDeceleration());

        float accel = 0f;
        if (rotationAngleDelta < 0f) {
            if (-decelerationAngleAbs < rotationAngleDelta) {
                accel += drone.getTurnDeceleration() * engine.getElapsedInLastFrame();
            } else {
                accel -= drone.getTurnAcceleration() * engine.getElapsedInLastFrame();
            }
        } else if (rotationAngleDelta > 0f) {
            if (decelerationAngleAbs > rotationAngleDelta) {
                accel -= drone.getTurnDeceleration() * engine.getElapsedInLastFrame();
            } else {
                accel += drone.getTurnAcceleration() * engine.getElapsedInLastFrame();
            }
        }

        angvel += accel;

        MathUtils.clamp(angvel, -drone.getMaxTurnRate(), drone.getMaxTurnRate());

        drone.setAngularVelocity(angvel);
    }

    /**
     * @param drone drone to rotate
     * @param targetAngle absolute angle to point to
     * @param mag multiplier used to decrease angle every time method is called
     */
    public static void rotateToFacingJerky(dl_DroneAPI drone, float targetAngle, float mag) {
        float delta = MathUtils.getShortestRotation(drone.getFacing(), targetAngle);
        drone.setFacing(drone.getFacing() + delta * mag);
    }

    /**
     * @param ship ship to land on
     * @param drone drone attempting to land
     * @param amount frame delta time, provided in update(float amount) methods
     * @param delayBeforeLandingTracker used to define length of time before beginning landing animation
     * @param engine combat engine
     */
    public static void attemptToLand(ShipAPI ship, dl_DroneAPI drone, float amount, IntervalUtil delayBeforeLandingTracker, CombatEngineAPI engine) {
        delayBeforeLandingTracker.advance(amount);
        boolean isPlayerShip = ship.equals(engine.getPlayerShip());

        if (drone.isLanding()) {
            delayBeforeLandingTracker.setElapsed(0f);
            if (isPlayerShip) {
                engine.maintainStatusForPlayerShip("DL_STATUS_KEY_DRONE_LANDING_STATE", "graphics/icons/hullsys/drone_pd_high.png", "LANDING STATUS", "LANDING... ", false);
            }
        } else {
            float round = Math.round((delayBeforeLandingTracker.getIntervalDuration() - delayBeforeLandingTracker.getElapsed()) * 100) / 100f;
            if (isPlayerShip) {
                engine.maintainStatusForPlayerShip("DL_STATUS_KEY_DRONE_LANDING_STATE", "graphics/icons/hullsys/drone_pd_high.png", "LANDING STATUS", "LANDING IN " + round, false);
            }
        }

        if (delayBeforeLandingTracker.intervalElapsed()) {
            drone.beginLandingAnimation(ship);
        }
    }

    /**
     * used when the ship landing on is over deployed drone capacity
     * @param ship ship to land on
     * @param drone drone to land
     */
    public static void attemptToLandAsExtra(ShipAPI ship, dl_DroneAPI drone) {
        if (!drone.isLanding() && MathUtils.getDistance(drone, ship) < ship.getCollisionRadius()) {
            drone.beginLandingAnimation(ship);
        }
    }

    /**
     * used to pick targets, include option for fighter prioritisation
     * @param ship mothership
     * @param drone drone attempting to target
     * @param weaponRange range of effective weapon
     * @param ignoreMissiles duh
     * @param ignoreFighters duh
     * @param ignoreShips excludes fighters
     * @param targetingArcDeviation half of width of targeting arc, will ignore potential targets outside of this arc
     * @return recommended entity to target
     */
    public static CombatEntityAPI getEnemyTarget(ShipAPI ship, dl_DroneAPI drone, float weaponRange, boolean ignoreMissiles, boolean ignoreFighters, boolean ignoreShips, float targetingArcDeviation) {
        //GET NEARBY OBJECTS TO SHOOT AT priority missiles > fighters > ships
        float facing = VectorUtils.getFacing(dl_MiscUtils.getVectorFromAToB(ship, drone.getShipAPI()));

        MissileAPI droneTargetMissile = null;
        if (!ignoreMissiles) {
            //get missile close to mothership
            List<MissileAPI> enemyMissiles = AIUtils.getNearbyEnemyMissiles(ship, weaponRange);
            float tracker = Float.MAX_VALUE;
            for (MissileAPI missile : enemyMissiles) {
                if (!dl_MiscUtils.isEntityInArc(missile, drone.getLocation(), facing, targetingArcDeviation)) {
                    continue;
                }

                float distance = MathUtils.getDistance(missile, drone);
                if (distance < tracker) {
                    tracker = distance;
                    droneTargetMissile = missile;
                }
            }
        }

        ShipAPI droneTargetShip = null;
        if (!ignoreShips) {
            if (ship.getShipTarget() != null) {
                droneTargetShip = ship.getShipTarget();
            } else {
                //get non-fighter ship close to mothership
                List<ShipAPI> enemyShips = AIUtils.getNearbyEnemies(drone, weaponRange);
                float tracker = Float.MAX_VALUE;
                for (ShipAPI enemyShip : enemyShips) {
                    if (enemyShip.isFighter()) {
                        continue;
                    }

                    //check if there is a friendly ship in the way
                    boolean areFriendliesInFiringArc = false;
                    float relAngle = VectorUtils.getFacing(dl_MiscUtils.getVectorFromAToB(drone, enemyShip));
                    for (ShipAPI ally : AIUtils.getNearbyAllies(drone, weaponRange)) {
                        if (dl_MiscUtils.isEntityInArc(ally, drone.getLocation(), relAngle, 20f)) {
                            if (MathUtils.getDistance(enemyShip, drone) > MathUtils.getDistance(ally, drone)) {
                                areFriendliesInFiringArc = true;
                                break;
                            }
                        }
                    }
                    if (areFriendliesInFiringArc) {
                        continue;
                    }

                    //can only match similar facing to host ship for balancing
                    if (!dl_MiscUtils.isEntityInArc(enemyShip, drone.getLocation(), facing, targetingArcDeviation)) {
                        continue;
                    }

                    float distance = MathUtils.getDistance(enemyShip, drone);
                    if (distance < tracker) {
                        tracker = distance;
                        droneTargetShip = enemyShip;
                    }
                }
            }
        }

        ShipAPI droneTargetFighter = null;
        if (!ignoreFighters) {
            //get fighter close to drone
            List<ShipAPI> enemyShips = AIUtils.getNearbyEnemies(drone, weaponRange);
            float tracker = Float.MAX_VALUE;
            for (ShipAPI enemyShip : enemyShips) {
                if (!enemyShip.isFighter()) {
                    continue;
                }

                if (!dl_MiscUtils.isEntityInArc(enemyShip, drone.getLocation(), facing, targetingArcDeviation)) {
                    continue;
                }

                float distance = MathUtils.getDistance(enemyShip, drone);
                if (distance < tracker) {
                    tracker = distance;
                    droneTargetFighter = enemyShip;
                }
            }
        }

        //PRIORITISE TARGET, SET LOCATION
        CombatEntityAPI target;
        if (droneTargetMissile != null) {
            target = droneTargetMissile;
        } else if (droneTargetFighter != null) {
            target = droneTargetFighter;
        } else target = droneTargetShip;
        return target;
    }

    /**
     * used to prevent drones firing over friendly ships preventing cheese
     * @param drone drone to check from
     * @param target entity the drone is tracking as an enemy
     * @param weaponRange range of effective weapon
     * @return duh
     */
    public static boolean areFriendliesBlockingArc(CombatEntityAPI drone, CombatEntityAPI target, float weaponRange) {
        for (ShipAPI ally : AIUtils.getNearbyAllies(drone, weaponRange)) {
            if (ally.getCollisionClass() == CollisionClass.FIGHTER || ally.getCollisionClass() == CollisionClass.NONE) {
                continue;
            }

            float distance = MathUtils.getDistance(ally, drone);
            if (MathUtils.getDistance(target, drone) < distance) {
                continue;
            }

            if (CollisionUtils.getCollisionPoint(drone.getLocation(), target.getLocation(), ally) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * used to find an alternate ShipAPI that uses same shipsystem type
     * @param drone drone looking for a home :(
     * @param prefix specific string id that corresponds to the system type (check examples for usage)
     * @param range maximum distance to look for a host
     * @return nearest host
     */
    public static ShipAPI getAlternateHost(dl_DroneAPI drone, String prefix, float range) {
        CombatEngineAPI engine = Global.getCombatEngine();
        List<ShipAPI> allies = AIUtils.getNearbyAllies(drone, range);
        if (allies.isEmpty()) {
            return null;
        }
        float dist = Float.MAX_VALUE;
        ShipAPI host = null;
        for (ShipAPI ship : allies) {
            for (String key : engine.getCustomData().keySet()) {
                if (key.contentEquals(prefix + ship.hashCode())) {
                    float temp = MathUtils.getDistance(drone, ship);
                    if (temp < dist) {
                        dist = temp;
                        host = ship;
                    }
                }
            }
        }
        return host;
    }

    /**
     * bang
     * @param drone drone to explode
     * @param engine combat engine
     */
    public static void deleteDrone (dl_DroneAPI drone, CombatEngineAPI engine) {
        //engine.removeEntity(drone);
        //engine.spawnExplosion(drone.getLocation(), drone.getVelocity(), DRONE_EXPLOSION_COLOUR, drone.getMass(), 1.5f);
        engine.applyDamage(
                drone,
                drone.getLocation(),
                10000f,
                DamageType.HIGH_EXPLOSIVE,
                0f,
                true,
                false,
                null,
                false
        );
    }
}
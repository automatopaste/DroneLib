package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import data.scripts.plugins.dl_SubsystemCombatManager;
import data.scripts.subsystems.dl_BaseSubsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class dl_SubsystemUtils {
    private static final Map<ShipAPI, List<Class<? extends dl_BaseSubsystem>>> subsystemQueue = new HashMap<>();
    /**
     * Gets the subsystem manager instance from the combat engine.
     * @return The subsystem manager instance
     */
    public static dl_SubsystemCombatManager getSubsystemManager() {
        return (dl_SubsystemCombatManager) Global.getCombatEngine().getCustomData().get(dl_SubsystemCombatManager.DATA_KEY);
    }

    /**
     * Queues a subsystem for a ShipAPI instance so it will be applied upon entering combat. Allows subsystems to be
     * cleanly added via hullmod.
     * @param ship Ship to add subsystem to
     * @param subsystemClass Class of subsystem to add
     */
    public static void queueSubsystemForShip(ShipAPI ship, Class<? extends dl_BaseSubsystem> subsystemClass) {
        if (ship == null || subsystemClass == null) throw new NullPointerException("ShipAPI or dl_BaseSubsystem was null");

        List<Class<? extends dl_BaseSubsystem>> subsystems = subsystemQueue.get(ship);
        if (subsystems == null) subsystems = new ArrayList<>();

        if (!subsystems.contains(subsystemClass)) {
            subsystems.add(subsystemClass);
            subsystemQueue.put(ship, subsystems);
        }
    }

    /**
     * Adds a subsystem to a specific ShipAPI instance. Checks if the subsystem instance (not type!) is already applied
     * first. Subsystem must have .init(ShipAPI ship) .aiInit() called afterwards to prevent exceptions.
     * @param ship Ship to apply subsystem to
     * @param subsystem Subsystem instance to apply
     */
    public static void addSubsystemToShip(ShipAPI ship, dl_BaseSubsystem subsystem) {
        dl_SubsystemCombatManager manager = getSubsystemManager();
        if (manager == null) throw new NullPointerException("Combat subsys manager was null");

        List<dl_BaseSubsystem> subsystems = manager.getSubsystems().get(ship);
        if (subsystems == null) subsystems = new ArrayList<>();

        if (!subsystems.contains(subsystem)) subsystems.add(subsystem);

        manager.getSubsystems().put(ship, subsystems);
    }

    /**
     * Removes a subsystem class from a specific ship.
     * @param ship Ship to remove subsystem from
     * @param subsystem Class of subsystem to remove
     */
    public static void removeSubsystemFromShip(ShipAPI ship, Class<? extends dl_BaseSubsystem> subsystem) {
        dl_SubsystemCombatManager manager = getSubsystemManager();
        if (manager == null) throw new NullPointerException("Combat subsys manager was null");

        List<dl_BaseSubsystem> subsystems = manager.getSubsystems().get(ship);
        if (subsystems == null) subsystems = new ArrayList<>();

        dl_BaseSubsystem rem = null;
        for (dl_BaseSubsystem s : subsystems) if (s.getClass().equals(subsystem)) rem = s;
        if (rem != null) subsystems.remove(rem);

        manager.getSubsystems().put(ship, subsystems);
    }

    /**
     * Associates a subsystem class with a particular hull id. Can be called outside combat. Checks if the subsystem
     * type is already associated first.
     * @param hullId Hull ID to associate with
     * @param subsystem Class of subsystem to associate
     */
    public static void addSubsystemToShipHull(String hullId, Class<? extends dl_BaseSubsystem> subsystem) {
        List<Class<? extends dl_BaseSubsystem>> subsystems = dl_SubsystemCombatManager.getSubsystemsByHullId().get(hullId);
        if (subsystems == null) {
            subsystems = new ArrayList<>();
        }
        if (!subsystems.contains(subsystem)) {
            subsystems.add(subsystem);
        }

        dl_SubsystemCombatManager.getSubsystemsByHullId().put(hullId, subsystems);
    }

    /**
     * Removes association with ship hull id. Does not remove subsystems from ships already instantiated in combat.
     * @param hullId Hull id of ship
     * @param subsystem Class of subsystem to remove
     */
    public static void removeSubsystemFromShipHull(String hullId, Class<? extends dl_BaseSubsystem> subsystem) {
        List<Class<? extends dl_BaseSubsystem>> subsystems = dl_SubsystemCombatManager.getSubsystemsByHullId().get(hullId);
        if (subsystems == null) {
            subsystems = new ArrayList<>();
        }
        subsystems.remove(subsystem);

        dl_SubsystemCombatManager.getSubsystemsByHullId().put(hullId, subsystems);
    }

    public static Map<ShipAPI, List<Class<? extends dl_BaseSubsystem>>> getSubsystemQueue() {
        return subsystemQueue;
    }
}
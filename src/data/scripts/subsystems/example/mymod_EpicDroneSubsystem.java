package data.scripts.subsystems.example;

import data.scripts.ai.dl_BaseDroneAI;
import data.scripts.ai.dl_BaseSubsystemDroneAI;
import data.scripts.ai.example.mymod_EpicSubsystemDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.shipsystems.example.dl_FiverDroneSystem;
import data.scripts.subsystems.dl_BaseDroneSubsystem;
import data.scripts.util.dl_SpecLoadingUtils;

public class mymod_EpicDroneSubsystem extends dl_BaseDroneSubsystem {
    public static final String SUBSYSTEM_ID = "fivedrones"; //this should match the id in the csv

    public enum EpicDroneOrders {
        DEFENCE,
        SHIELD,
        RECALL
    }
    private EpicDroneOrders droneOrders = EpicDroneOrders.RECALL;

    public mymod_EpicDroneSubsystem() {
        super(dl_SpecLoadingUtils.droneSystemSpecHashMap.get("dl_fivedrones"), dl_SpecLoadingUtils.getSubsystemData(SUBSYSTEM_ID));
    }

    @Override
    public void nextDroneOrder() {
        droneOrders = getNextDroneOrder();
    }

    private EpicDroneOrders getNextDroneOrder() {
        if (droneOrders.ordinal() == dl_FiverDroneSystem.FiverDroneOrders.values().length - 1) {
            return EpicDroneOrders.values()[0];
        }
        return EpicDroneOrders.values()[droneOrders.ordinal() + 1];
    }

    @Override
    public void maintainStatusMessage() {
        switch (droneOrders) {
            case SHIELD:
                maintainSystemStateStatus("SHIELD ARRAY FORMATION");
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
        return droneOrders == EpicDroneOrders.RECALL;
    }

    @Override
    public void setDefaultDeployMode() {
        droneOrders = EpicDroneOrders.DEFENCE;
    }

    @Override
    public void executePerOrders(float amount) {
        switch (droneOrders) {
            case DEFENCE:

                break;
            case SHIELD:

                break;
            case RECALL:

                break;
        }
    }

    public EpicDroneOrders getDroneOrders() {
        return droneOrders;
    }

    @Override
    public dl_BaseSubsystemDroneAI getNewDroneAIInstance(dl_DroneAPI spawnedDrone) {
        return new mymod_EpicSubsystemDroneAI(spawnedDrone, this);
    }

    @Override
    public void aiInit() {

    }

    @Override
    public void aiUpdate(float amount) {

    }

    @Override
    public String getFlavourString() {
        return "LAUNCHES 5 DEFENCE DRONES";
    }

    @Override
    public String getStatusString() {
        return null;
    }
}

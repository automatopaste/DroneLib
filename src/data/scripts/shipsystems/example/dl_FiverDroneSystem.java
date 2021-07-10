package data.scripts.shipsystems.example;

import data.scripts.ai.dl_BaseDroneAI;
import data.scripts.ai.example.dl_FiverDroneAI;
import data.scripts.impl.dl_DroneAPI;
import data.scripts.shipsystems.dl_BaseDroneSystem;

public class dl_FiverDroneSystem extends dl_BaseDroneSystem {
    public static final String SYSTEM_ID = "dl_fivedrones";

    public enum FiverDroneOrders {
        DEFENCE,
        SHIELD,
        RECALL
    }

    private FiverDroneOrders droneOrders = FiverDroneOrders.RECALL;

    public dl_FiverDroneSystem() {
        loadSpecData();
    }

    @Override
    public void nextDroneOrder() {
        droneOrders = getNextDroneOrder();
    }

    private FiverDroneOrders getNextDroneOrder() {
        if (droneOrders.ordinal() == FiverDroneOrders.values().length - 1) {
            return FiverDroneOrders.values()[0];
        }
        return FiverDroneOrders.values()[droneOrders.ordinal() + 1];
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
        return droneOrders == FiverDroneOrders.RECALL;
    }

    @Override
    public void setDefaultDeployMode() {
        droneOrders = FiverDroneOrders.DEFENCE;
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

    @Override
    public dl_BaseDroneAI getNewDroneAIInstance(dl_DroneAPI spawnedDrone, dl_BaseDroneSystem baseDroneSystem) {
        return new dl_FiverDroneAI(spawnedDrone, baseDroneSystem);
    }

    @Override
    public String getSystemID() {
        return SYSTEM_ID;
    }

    public FiverDroneOrders getDroneOrders() {
        return droneOrders;
    }
}

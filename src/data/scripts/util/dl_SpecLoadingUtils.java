package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import data.scripts.shipsystems.example.dl_FiverDroneSystem;
import data.scripts.subsystems.dl_BaseSubsystem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * @author tomatopaste
 * used to load various spec from .json and .csv
 */
public final class dl_SpecLoadingUtils {
    public static Map<String, DroneSystemSpec> droneSystemSpecHashMap = new HashMap<>();

    private static HashMap<String, dl_BaseSubsystem.SubsystemData> subsystemData;

    private static final List<String> subsystemHotkeyPriority = new ArrayList<>();

    /**
     * @throws JSONException
     * called on application load by mod plugin, loads data always used by each DroneLib-pattern ship system
     */
    public static void loadDroneSystemSpecs() throws IOException, JSONException {
        SettingsAPI settings = Global.getSettings();

        JSONArray droneSystems = settings.getMergedSpreadsheetDataForMod("id","data/shipsystems/drone_systems.csv", "dronelib");

        int z = droneSystems.length();
        for (int i = 0; i < z; i++) {
            JSONObject row = droneSystems.getJSONObject(i);
            String id = row.getString("id");

            String filename = row.getString("filename");

            JSONObject droneSystemSpecJson = settings.loadJSON(filename);

            DroneSystemSpec spec = new DroneSystemSpec();

            spec.launchDelay = droneSystemSpecJson.getDouble("dl_launchDelay");
            spec.launchSpeed = droneSystemSpecJson.getDouble("dl_launchSpeed");
            spec.droneVariant = droneSystemSpecJson.getString("dl_droneVariant");
            spec.maxDeployedDrones = droneSystemSpecJson.getInt("dl_maxDrones");
            spec.maxReserveDroneCount = droneSystemSpecJson.getInt("dl_maxReserve");
            spec.forgeCooldown = droneSystemSpecJson.getDouble("dl_forgeCooldown");
            spec.filename = filename;

            droneSystemSpecHashMap.put(id, spec);
        }
    }

    public static void loadSubsystemData() throws JSONException, IOException {
        SettingsAPI settings = Global.getSettings();
        JSONArray subsystems = settings.getMergedSpreadsheetDataForMod("id","data/subsystems/subsystems.csv", "dronelib");

        subsystemData = new HashMap<>();

        for (int i = 0; i < subsystems.length(); i++) {
            JSONObject row = subsystems.getJSONObject(i);
            String id = row.getString("id");

            float inTime = catchJsonFloatDefaultZero(row, "inTime");
            float activeTime = catchJsonFloatDefaultZero(row, "activeTime");
            float outTime = catchJsonFloatDefaultZero(row, "outTime");
            float cooldownTime = catchJsonFloatDefaultZero(row, "cooldownTime");

            boolean isToggle = catchJsonBooleanDefaultFalse(row, "isToggle");
            if (isToggle) activeTime = Float.MAX_VALUE;

            float fluxPerSecondPercentMaxCapacity = catchJsonFloatDefaultZero(row, "fluxPerSecondPercentMaxCapacity");
            float fluxPerSecondFlat = catchJsonFloatDefaultZero(row, "fluxPerSecondFlat");

            String hotkey = row.getString("hotkey");
            if (hotkey.isEmpty()) hotkey = null;

            dl_BaseSubsystem.SubsystemData data = new dl_BaseSubsystem.SubsystemData(
                    hotkey,
                    id,
                    row.getString("name"),
                    inTime,
                    activeTime,
                    outTime,
                    cooldownTime,
                    isToggle,
                    fluxPerSecondPercentMaxCapacity,
                    fluxPerSecondFlat
            );

            subsystemData.put(id, data);
        }

        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind1"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind2"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind3"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind4"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind5"));
        subsystemHotkeyPriority.add(Global.getSettings().getString("dl_DefaultKeybind6"));
    }

    public static List<String> getSubsystemHotkeyPriority() {
        return subsystemHotkeyPriority;
    }

    public static dl_BaseSubsystem.SubsystemData getSubsystemData(String id) {
        return subsystemData.get(id);
    }

    public static float catchJsonFloatDefaultZero(JSONObject row, String id) {
        float value;
        try {
            value = (float) row.getDouble(id);
        } catch (JSONException e) {
            value = 0f;
        }
        return value;
    }

    public static boolean catchJsonBooleanDefaultFalse(JSONObject row, String id) {
        boolean value;
        try {
            value = row.getBoolean(id);
        } catch (JSONException e) {
            value = false;
        }
        return value;
    }

    public static String getFilenameForSystemID(String id) {
        return droneSystemSpecHashMap.get(id).filename;
    }

    public static class DroneSystemSpec {
        public int maxDeployedDrones;
        public int maxReserveDroneCount;
        public double forgeCooldown;
        public double launchDelay;
        public double launchSpeed;
        public String droneVariant;
        public String filename;
    }

    public static JSONArray getDroneBehaviour(String filename) throws JSONException, IOException {
        JSONObject droneSystemSpecJson = Global.getSettings().loadJSON(filename);
        return droneSystemSpecJson.getJSONArray("dl_droneBehavior");
    }

    /**
     * example for loading data for formations, should be called once on application load in mod plugin
     */
    public static class dl_FiveDronesSpecLoading {
        private static float[] shieldOrbitRadiusArray;
        private static float[] shieldOrbitSpeedArray;
        private static float[] defenceOrbitAngleArray;
        private static float[] defenceFacingArray;
        private static float[] defenceOrbitRadiusArray;

        public static void loadJSON() throws JSONException, IOException {
            JSONArray droneBehaviorSpecJson = getDroneBehaviour(getFilenameForSystemID(dl_FiverDroneSystem.SYSTEM_ID));
            int maxDeployedDrones = droneSystemSpecHashMap.get(dl_FiverDroneSystem.SYSTEM_ID).maxDeployedDrones;

            shieldOrbitRadiusArray = new float[maxDeployedDrones];
            shieldOrbitSpeedArray = new float[maxDeployedDrones];
            defenceOrbitAngleArray = new float[maxDeployedDrones];
            defenceFacingArray = new float[maxDeployedDrones];
            defenceOrbitRadiusArray = new float[maxDeployedDrones];

            for (int i = 0; i < maxDeployedDrones; i++) {
                JSONObject droneConfigPerIndexJsonObject = droneBehaviorSpecJson.getJSONObject(i);

                shieldOrbitRadiusArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("shieldOrbitRadius");
                shieldOrbitSpeedArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("shieldOrbitSpeed");
                defenceOrbitAngleArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceOrbitAngle");
                defenceFacingArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceFacing");
                defenceOrbitRadiusArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceOrbitRadius");
            }
        }

        public static float[] getshieldOrbitRadiusArray() {
            return shieldOrbitRadiusArray;
        }
        public static float[] getshieldOrbitSpeedArray() {
            return shieldOrbitSpeedArray;
        }
        public static float[] getDefenceOrbitAngleArray() {
            return defenceOrbitAngleArray;
        }
        public static float[] getDefenceFacingArray() {
            return defenceFacingArray;
        }
        public static float[] getDefenceOrbitRadiusArray() {
            return defenceOrbitRadiusArray;
        }
    }
}
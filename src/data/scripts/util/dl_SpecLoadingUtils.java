package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SettingsAPI;
import data.scripts.shipsystems.example.PSE_DroneRift;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author tomapaste
 * used to load drone system spec from .json
 */
public final class dl_SpecLoadingUtils {
    public static Map<String, DroneSystemSpec> droneSystemSpecHashMap = new HashMap<>();

    /**
     * @throws IOException
     * @throws JSONException
     * called on application load by mod plugin, loads data always used by each DroneLib-pattern ship system
     */
    public static void loadBaseSystemSpecs() throws IOException, JSONException {
        SettingsAPI settings = Global.getSettings();

        JSONArray droneSystems = settings.loadCSV("data/shipsystems/drone_systems.csv");

        for (int i = 0; i < droneSystems.length(); i++) {
            JSONObject row = droneSystems.getJSONObject(i);
            String id = row.getString("id");
            String filename = row.getString("filename");

            JSONObject droneSystemSpecJson = settings.loadJSON(filename);

            DroneSystemSpec spec = new DroneSystemSpec();

            spec.launchDelay = droneSystemSpecJson.getDouble("dl_launchDelay");
            spec.launchSpeed = droneSystemSpecJson.getDouble("dl_launchSpeed");
            spec.droneVariant = droneSystemSpecJson.getString("dl_droneVariant");
            spec.maxDeployedDrones = droneSystemSpecJson.getInt("dl_maxDrones");
            spec.forgeCooldown = droneSystemSpecJson.getDouble("dl_forgeCooldown");
            spec.filename = filename;

            droneSystemSpecHashMap.put(id, spec);
        }
    }

    public static String getFilenameForSystemID(String id) {
        return droneSystemSpecHashMap.get(id).filename;
    }

    public static class DroneSystemSpec {
        public int maxDeployedDrones;
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
    public static class PSE_RiftSpecLoading {
        private static float[] fieldOrbitRadiusArray;
        private static float[] fieldOrbitSpeedArray;
        private static float[] defenceOrbitAngleArray;
        private static float[] defenceFacingArray;
        private static float[] defenceOrbitRadiusArray;

        public static void loadJSON() throws JSONException, IOException {
            JSONArray droneBehaviorSpecJson = getDroneBehaviour(getFilenameForSystemID(PSE_DroneRift.UNIQUE_SYSTEM_PREFIX));
            int maxDeployedDrones = droneSystemSpecHashMap.get(PSE_DroneRift.UNIQUE_SYSTEM_PREFIX).maxDeployedDrones;

            fieldOrbitRadiusArray = new float[maxDeployedDrones];
            fieldOrbitSpeedArray = new float[maxDeployedDrones];
            defenceOrbitAngleArray = new float[maxDeployedDrones];
            defenceFacingArray = new float[maxDeployedDrones];
            defenceOrbitRadiusArray = new float[maxDeployedDrones];

            for (int i = 0; i < maxDeployedDrones; i++) {
                JSONObject droneConfigPerIndexJsonObject = droneBehaviorSpecJson.getJSONObject(i);

                fieldOrbitRadiusArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("fieldOrbitRadius");
                fieldOrbitSpeedArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("fieldOrbitSpeed");
                defenceOrbitAngleArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceOrbitAngle");
                defenceFacingArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceFacing");
                defenceOrbitRadiusArray[i] = Objects.requireNonNull(droneConfigPerIndexJsonObject).getInt("defenceOrbitRadius");
            }
        }

        public static float[] getFieldOrbitRadiusArray() {
            return fieldOrbitRadiusArray;
        }
        public static float[] getFieldOrbitSpeedArray() {
            return fieldOrbitSpeedArray;
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
package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import data.scripts.subsystems.example.*;
import data.scripts.util.dl_SpecLoadingUtils;
import data.scripts.util.dl_SubsystemUtils;
import org.json.JSONException;

import java.io.IOException;

public class dl_ModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        dl_SpecLoadingUtils.loadSubsystemData();

        try {
            dl_SpecLoadingUtils.loadDroneSystemSpecs();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        //vvv!! EXAMPLE !!vvv
        applySubsystems();
        loadDroneSystemSpecJson();
        //^^^!! EXAMPLE !!^^^
    }

    /**
     * Example method of baking subsystems into ship hulls.
     */
    private void applySubsystems() {
        if (Global.getSettings().getBoolean("dl_UseHammerheadForShowcase")) {
            dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicAcceleratedAmmoFeederSubsystem.class);
            dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicDroneSubsystem.class);
        }
    }

    /**
     * Example method of loading
     */
    private void loadDroneSystemSpecJson() {
        //try/catch block for each system with a loadJSON method
        try {
            dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.loadJSON();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        //and so on..
    }
}
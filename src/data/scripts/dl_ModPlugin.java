package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
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

        //dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicAcceleratedAmmoFeederSubsystem.class);
        dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicBurnDriveSubsystem.class);
        //dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicPhaseCloakSubsystem.class);
        dl_SubsystemUtils.addSubsystemToShipHull("hammerhead", mymod_EpicDroneSubsystem.class);

        dl_SubsystemUtils.addSubsystemToShipHull("onslaught", mymod_EpicBurnDriveSubsystem.class);
        dl_SubsystemUtils.addSubsystemToShipHull("onslaught", mymod_EpicPhaseCloakSubsystem.class);

        dl_SubsystemUtils.addSubsystemToShipHull("hound", mymod_EpicPhaseCloakSubsystem.class);

        //try/catch block for each system with a loadJSON method
        try {
            dl_SpecLoadingUtils.dl_FiveDronesSpecLoading.loadJSON();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        //and so on..
    }
}

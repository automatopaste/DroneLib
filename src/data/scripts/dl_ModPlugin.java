package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import data.scripts.util.dl_SpecLoadingUtils;
import org.json.JSONException;

import java.io.IOException;

public class dl_ModPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        try {
            dl_SpecLoadingUtils.loadBaseSystemSpecs();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        //try/catch block for each system with a loadJSON method
        try {
            dl_SpecLoadingUtils.dl_RiftSpecLoading.loadJSON();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        //and so on..
    }
}

package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.subsystems.ai.dl_BaseSubsystemAI;
import data.scripts.subsystems.dl_BaseSubsystem;
import data.scripts.util.dl_CombatUI;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class dl_SubsystemCombatManager extends BaseEveryFrameCombatPlugin {
    public static final String DATA_KEY = "dl_SubsystemCombatManager";
    public static final String INFO_TOGGLE_KEY = Global.getSettings().getString("dl_SubsystemToggleKey");

    public static boolean showInfoText = true;

    private final Map<ShipAPI, List<dl_BaseSubsystem>> subsystems;
    private static final Map<String, List<Class<? extends dl_BaseSubsystem>>> subsystemsByHullId = new HashMap<>();

    public dl_SubsystemCombatManager() {
        subsystems = new HashMap<>();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        engine.getCustomData().put(DATA_KEY, this);

        showInfoText = true;

        subsystems.clear();
    }

    private boolean isHotkeyDownLast = false;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.isPaused()) {
            List<ShipAPI> ships = engine.getShips();
            for (ShipAPI ship : ships) {
                if (!ship.isAlive()) continue;

                List<Class<? extends dl_BaseSubsystem>> subsystemByHullId = subsystemsByHullId.get(ship.getHullSpec().getBaseHullId());
                if (subsystemByHullId == null) continue;

                List<dl_BaseSubsystem> subsystemsOnShip = subsystems.get(ship);
                if (subsystemsOnShip == null) subsystemsOnShip = new ArrayList<>();

                int index = 0;
                outer:
                for (Class<? extends dl_BaseSubsystem> c : subsystemByHullId) {
                    for (dl_BaseSubsystem s : subsystemsOnShip) if (s.getClass().equals(c)) continue outer;

                    try {
                        dl_BaseSubsystem subsystem = c.newInstance();

                        subsystemsOnShip.add(subsystem);
                        subsystem.init(ship);
                        subsystem.setIndex(index);
                        index++;

                        dl_BaseSubsystemAI ai = subsystem.getAI();
                        ai.aiInit();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

                subsystems.put(ship, subsystemsOnShip);
            }

            List<ShipAPI> rem = new LinkedList<>();
            for (ShipAPI ship : subsystems.keySet()) {
                if (!engine.isEntityInPlay(ship)) {
                    rem.add(ship);
                    continue;
                }

                int index = 0;
                for (dl_BaseSubsystem subsystem : subsystems.get(ship)) {
                    subsystem.setIndex(index);
                    index++;
                    subsystem.advance(amount);
                }
            }

            for (ShipAPI ship : rem) subsystems.remove(ship);
        }

        boolean isHotkeyDown = Keyboard.isKeyDown(Keyboard.getKeyIndex(INFO_TOGGLE_KEY));
        if (isHotkeyDown && !isHotkeyDownLast) showInfoText = !showInfoText;
        isHotkeyDownLast = isHotkeyDown;

        ShipAPI player = engine.getPlayerShip();
        List<dl_BaseSubsystem> s;
        if (player != null) s = subsystems.get(player);
        else return;
        if (s == null) return;

        int numBars = 0;
        for (dl_BaseSubsystem sub : s) {
            numBars += sub.getNumGuiBars();
            if (showInfoText) numBars++;
        }
        Vector2f rootLoc = dl_CombatUI.getSubsystemsRootLocation(player, numBars, 13f * Global.getSettings().getScreenScaleMult());

        Vector2f inputLoc = new Vector2f(rootLoc);
        for (dl_BaseSubsystem sub : s) {
            inputLoc = sub.guiRender(inputLoc, rootLoc);
        }

        dl_CombatUI.drawSubsystemsTitle(engine.getPlayerShip(), showInfoText, rootLoc);
    }

    public Map<ShipAPI, List<dl_BaseSubsystem>> getSubsystems() {
        return subsystems;
    }

    public static Map<String, List<Class<? extends dl_BaseSubsystem>>> getSubsystemsByHullId() {
        return subsystemsByHullId;
    }
}

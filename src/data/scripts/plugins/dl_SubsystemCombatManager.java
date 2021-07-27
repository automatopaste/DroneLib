package data.scripts.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import data.scripts.subsystems.dl_BaseSubsystem;
import data.scripts.util.dl_CombatUI;
import data.scripts.util.dl_SpecLoadingUtils;
import data.scripts.util.dl_SubsystemUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class dl_SubsystemCombatManager extends BaseEveryFrameCombatPlugin {
    public static final String DATA_KEY = "dl_SubsystemCombatManager";
    public static final String INFO_TOGGLE_KEY = Global.getSettings().getString("dl_SubsystemToggleKey");

    private Map<ShipAPI, List<Class<? extends dl_BaseSubsystem>>> subsystemHullmodQueue = new HashMap<>();

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

        subsystemHullmodQueue = dl_SubsystemUtils.getSubsystemQueue();
        dl_SubsystemUtils.getSubsystemQueue().clear();
    }

    private boolean isHotkeyDownLast = false;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (!engine.isPaused()) {
            List<ShipAPI> ships = engine.getShips();
            for (ShipAPI ship : ships) {
                if (!ship.isAlive()) continue;

                List<dl_BaseSubsystem> subsystemsOnShip = subsystems.get(ship);
                if (subsystemsOnShip == null) subsystemsOnShip = new ArrayList<>();

                List<Class<? extends dl_BaseSubsystem>> toAdd = new ArrayList<>();

                List<Class<? extends dl_BaseSubsystem>> subsystemByHullId = subsystemsByHullId.get(ship.getHullSpec().getBaseHullId());
                if (subsystemByHullId != null) {
                    outer:
                    for (Class<? extends dl_BaseSubsystem> c : subsystemByHullId) {
                        for (dl_BaseSubsystem s : subsystemsOnShip) if (s.getClass().equals(c)) continue outer;

                        toAdd.add(c);
                    }
                }

                List<Class<? extends dl_BaseSubsystem>> hullmodQueue = subsystemHullmodQueue.get(ship);
                if (hullmodQueue != null) {
                    toAdd.addAll(hullmodQueue);
                    subsystemHullmodQueue.put(ship, null);
                }

                int index = 0;
                for (Class<? extends dl_BaseSubsystem> t : toAdd) {
                    try {
                        dl_BaseSubsystem subsystem = t.newInstance();

                        subsystemsOnShip.add(subsystem);
                        subsystem.init(ship);
                        subsystem.setIndex(index);
                        index++;
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
        if (player != null) {
            List<String> defaultHotkeys = new ArrayList<>(dl_SpecLoadingUtils.getSubsystemHotkeyPriority());
            Collections.reverse(defaultHotkeys);

            s = subsystems.get(player);
            if (s == null || s.isEmpty()) return;

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
    }

    public Map<ShipAPI, List<dl_BaseSubsystem>> getSubsystems() {
        return subsystems;
    }

    public static Map<String, List<Class<? extends dl_BaseSubsystem>>> getSubsystemsByHullId() {
        return subsystemsByHullId;
    }
}

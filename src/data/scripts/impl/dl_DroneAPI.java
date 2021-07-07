package data.scripts.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.CombatListenerManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.combat.entities.Ship;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class dl_DroneAPI implements ShipAPI {
    private ShipAPI mothership;
    private final ShipAPI drone;

    public dl_DroneAPI(ShipAPI drone, ShipAPI mothership) {
        this.drone = drone;
        this.mothership = mothership;
    }

    //this implementation is probably unnecessary but its been months since i touched it and i cant be fucken bothered

    public void setAnimatedLaunch() {
        Ship ship = (Ship) drone;
        ship.setAnimatedLaunch();
    }

    public void setDrone() {
        Ship ship = (Ship) drone;
        ship.setDrone(true);
    }

    public void setLaunchingShip(ShipAPI launchingShip) {
        Ship ship = (Ship) drone;
        ship.setLaunchingShip(launchingShip);
    }

    @Override
    public boolean isNonCombat(boolean considerOrders) {
        return false;
    }

    @Override
    public float findBestArmorInArc(float facing, float arc) {
        return 0;
    }

    @Override
    public float getAverageArmorInSlice(float direction, float arc) {
        return 0;
    }

    @Override
    public void setHoldFire(boolean holdFire) {

    }

    @Override
    public void cloneVariant() {

    }

    @Override
    public void setTimeDeployed(float timeDeployed) {

    }

    public ShipAPI getLaunchingShip() {
        return this.mothership;
    }

    public void remove() {
        Global.getCombatEngine().removeEntity(drone);
    }

    public ShipAPI getShipAPI() {
        return drone;
    }

    @Override
    public String getFleetMemberId() {
        return drone.getFleetMemberId();
    }

    @Override
    public Vector2f getMouseTarget() {
        return drone.getMouseTarget();
    }

    @Override
    public boolean isShuttlePod() {
        return false;
    }

    @Override
    public boolean isDrone() {
        return true;
    }

    @Override
    public boolean isFighter() {
        return false;
    }

    @Override
    public boolean isFrigate() {
        return false;
    }

    @Override
    public boolean isDestroyer() {
        return false;
    }

    @Override
    public boolean isCruiser() {
        return false;
    }

    @Override
    public boolean isCapital() {
        return false;
    }

    @Override
    public HullSize getHullSize() {
        return HullSize.FIGHTER;
    }

    @Override
    public ShipAPI getShipTarget() {
        return drone.getShipTarget();
    }

    @Override
    public void setShipTarget(ShipAPI ship) {
        this.drone.setShipTarget(ship);
    }

    @Override
    public int getOriginalOwner() {
        return drone.getOriginalOwner();
    }

    @Override
    public void setOriginalOwner(int originalOwner) {
        drone.setOriginalOwner(originalOwner);
    }

    @Override
    public void resetOriginalOwner() {
        drone.resetOriginalOwner();
    }

    @Override
    public MutableShipStatsAPI getMutableStats() {
        return drone.getMutableStats();
    }

    @Override
    public boolean isHulk() {
        return drone.isHulk();
    }

    @Override
    public List<WeaponAPI> getAllWeapons() {
        return drone.getAllWeapons();
    }

    @Override
    public ShipSystemAPI getPhaseCloak() {
        return drone.getPhaseCloak();
    }

    @Override
    public ShipSystemAPI getSystem() {
        return drone.getSystem();
    }

    @Override
    public ShipSystemAPI getTravelDrive() {
        return drone.getTravelDrive();
    }

    @Override
    public void toggleTravelDrive() {
        drone.toggleTravelDrive();
    }

    @Override
    public void setShield(ShieldAPI.ShieldType type, float shieldUpkeep, float shieldEfficiency, float arc) {
        drone.setShield(type, shieldUpkeep, shieldEfficiency, arc);
    }

    @Override
    public ShipHullSpecAPI getHullSpec() {
        return drone.getHullSpec();
    }

    @Override
    public ShipVariantAPI getVariant() {
        return drone.getVariant();
    }

    @Override
    public void useSystem() {
        drone.useSystem();
    }

    @Override
    public FluxTrackerAPI getFluxTracker() {
        return drone.getFluxTracker();
    }

    @Override
    public List<ShipAPI> getWingMembers() {
        return null;
    }

    @Override
    public ShipAPI getWingLeader() {
        return drone.getWingLeader();
    }

    @Override
    public boolean isWingLeader() {
        return drone.isWingLeader();
    }

    @Override
    public FighterWingAPI getWing() {
        return drone.getWing();
    }

    @Override
    public List<ShipAPI> getDeployedDrones() {
        return drone.getDeployedDrones();
    }

    @Override
    public ShipAPI getDroneSource() { //IMPORTANT!
        return mothership;
    }

    public void setDroneSource(ShipAPI sourceShip) { //IMPORTANT!
        this.mothership = sourceShip;
    }

    @Override
    public Object getWingToken() {
        return drone.getWingToken();
    }

    @Override
    public ArmorGridAPI getArmorGrid() {
        return drone.getArmorGrid();
    }

    @Override
    public void setRenderBounds(boolean renderBounds) {
        drone.setRenderBounds(renderBounds);
    }

    @Override
    public void setCRAtDeployment(float cr) {
        drone.setCRAtDeployment(cr);
    }

    @Override
    public float getCRAtDeployment() {
        return drone.getCRAtDeployment();
    }

    @Override
    public float getCurrentCR() {
        return drone.getCurrentCR();
    }

    @Override
    public void setCurrentCR(float cr) {
        drone.setCurrentCR(cr);
    }

    @Override
    public float getWingCRAtDeployment() {
        return drone.getWingCRAtDeployment();
    }

    @Override
    public void setHitpoints(float value) {
        drone.setHitpoints(value);
    }

    @Override
    public float getTimeDeployedForCRReduction() {
        return drone.getTimeDeployedForCRReduction();
    }

    @Override
    public float getFullTimeDeployed() {
        return drone.getFullTimeDeployed();
    }

    @Override
    public boolean losesCRDuringCombat() {
        return drone.losesCRDuringCombat();
    }

    @Override
    public boolean controlsLocked() {
        return drone.controlsLocked();
    }

    @Override
    public void setControlsLocked(boolean controlsLocked) {
        drone.setControlsLocked(controlsLocked);
    }

    @Override
    public void setShipSystemDisabled(boolean systemDisabled) {
        drone.setShipSystemDisabled(systemDisabled);
    }

    @Override
    public Set<WeaponAPI> getDisabledWeapons() {
        return drone.getDisabledWeapons();
    }

    @Override
    public int getNumFlameouts() {
        return drone.getNumFlameouts();
    }

    @Override
    public float getHullLevelAtDeployment() {
        return drone.getHullLevelAtDeployment();
    }

    @Override
    public void setSprite(String category, String key) {
        drone.setSprite(category, key);
    }

    @Override
    public SpriteAPI getSpriteAPI() {
        return drone.getSpriteAPI();
    }

    @Override
    public ShipEngineControllerAPI getEngineController() {
        return drone.getEngineController();
    }

    @Override
    public void giveCommand(ShipCommand command, Object param, int groupNumber) {
        drone.giveCommand(command, param, groupNumber);
    }

    @Override
    public void setShipAI(ShipAIPlugin ai) {
        drone.setShipAI(ai);
    }

    @Override
    public ShipAIPlugin getShipAI() {
        return drone.getShipAI();
    }

    @Override
    public void resetDefaultAI() {
        drone.resetDefaultAI();
    }

    @Override
    public void turnOnTravelDrive() {
        drone.turnOnTravelDrive();
    }

    @Override
    public void turnOnTravelDrive(float dur) {
        drone.turnOnTravelDrive(dur);
    }

    @Override
    public void turnOffTravelDrive() {
        drone.turnOffTravelDrive();
    }

    @Override
    public boolean isRetreating() {
        return drone.isRetreating();
    }

    @Override
    public void abortLanding() {
        drone.abortLanding();
    }

    @Override
    public void beginLandingAnimation(ShipAPI target) {
        drone.beginLandingAnimation(target);
    }

    @Override
    public boolean isLanding() {
        return drone.isLanding();
    }

    @Override
    public boolean isFinishedLanding() {
        return drone.isFinishedLanding();
    }

    @Override
    public boolean isAlive() {
        return drone.isAlive();
    }

    @Override
    public boolean isInsideNebula() {
        return drone.isInsideNebula();
    }

    @Override
    public void setInsideNebula(boolean isInsideNebula) {
        drone.setInsideNebula(isInsideNebula);
    }

    @Override
    public boolean isAffectedByNebula() {
        return drone.isAffectedByNebula();
    }

    @Override
    public void setAffectedByNebula(boolean affectedByNebula) {
        drone.setAffectedByNebula(affectedByNebula);
    }

    @Override
    public float getDeployCost() {
        return drone.getDeployCost();
    }

    @Override
    public void removeWeaponFromGroups(WeaponAPI weapon) {
        drone.removeWeaponFromGroups(weapon);
    }

    @Override
    public void applyCriticalMalfunction(Object module) {
        drone.applyCriticalMalfunction(module);
    }

    @Override
    public float getBaseCriticalMalfunctionDamage() {
        return drone.getBaseCriticalMalfunctionDamage();
    }

    @Override
    public float getEngineFractionPermanentlyDisabled() {
        return drone.getEngineFractionPermanentlyDisabled();
    }

    @Override
    public float getCombinedAlphaMult() {
        return drone.getCombinedAlphaMult();
    }

    @Override
    public float getLowestHullLevelReached() {
        return drone.getLowestHullLevelReached();
    }

    @Override
    public ShipwideAIFlags getAIFlags() {
        return drone.getAIFlags();
    }

    @Override
    public List<WeaponGroupAPI> getWeaponGroupsCopy() {
        return drone.getWeaponGroupsCopy();
    }

    @Override
    public boolean isHoldFire() {
        return drone.isHoldFire();
    }

    @Override
    public boolean isHoldFireOneFrame() {
        return drone.isHoldFireOneFrame();
    }

    @Override
    public void setHoldFireOneFrame(boolean holdFireOneFrame) {
        drone.setHoldFireOneFrame(holdFireOneFrame);
    }

    @Override
    public boolean isPhased() {
        return drone.isPhased();
    }

    @Override
    public boolean isAlly() {
        return drone.isAlly();
    }

    @Override
    public void setWeaponGlow(float glow, Color color, EnumSet<WeaponAPI.WeaponType> types) {
        drone.setWeaponGlow(glow, color, types);
    }

    @Override
    public void setVentCoreColor(Color color) {
        drone.setVentCoreColor(color);
    }

    @Override
    public void setVentFringeColor(Color color) {
        drone.setVentFringeColor(color);
    }

    @Override
    public Color getVentCoreColor() {
        return drone.getVentCoreColor();
    }

    @Override
    public Color getVentFringeColor() {
        return drone.getVentFringeColor();
    }

    @Override
    public void setFluxVentTextureSheet(String textureId) {
        drone.setFluxVentTextureSheet(textureId);
    }

    @Override
    public String getFluxVentTextureSheet() {
        return null;
    }

    @Override
    public String getHullStyleId() {
        return drone.getHullStyleId();
    }

    @Override
    public WeaponGroupAPI getWeaponGroupFor(WeaponAPI weapon) {
        return drone.getWeaponGroupFor(weapon);
    }

    @Override
    public void setCopyLocation(Vector2f loc, float copyAlpha, float copyFacing) {
        drone.setCopyLocation(loc, copyAlpha, copyFacing);
    }

    @Override
    public Vector2f getCopyLocation() {
        return drone.getCopyLocation();
    }

    @Override
    public void setAlly(boolean ally) {
        drone.setAlly(ally);
    }

    @Override
    public void applyCriticalMalfunction(Object module, boolean permanent) {
        drone.applyCriticalMalfunction(module, permanent);
    }

    @Override
    public String getId() {
        return drone.getId();
    }

    @Override
    public String getName() {
        return drone.getName();
    }

    @Override
    public void setJitter(Object source, Color color, float intensity, int copies, float range) {
        drone.setJitter(source, color, intensity, copies, range);
    }

    @Override
    public void setJitterUnder(Object source, Color color, float intensity, int copies, float range) {
        drone.setJitterUnder(source, color, intensity, copies, range);
    }

    @Override
    public void setJitter(Object source, Color color, float intensity, int copies, float minRange, float range) {
        drone.setJitter(source, color, intensity, copies, minRange, range);
    }

    @Override
    public void setJitterUnder(Object source, Color color, float intensity, int copies, float minRange, float range) {
        drone.setJitterUnder(source, color, intensity, copies, minRange, range);
    }

    @Override
    public float getTimeDeployedUnderPlayerControl() {
        return drone.getTimeDeployedUnderPlayerControl();
    }

    @Override
    public SpriteAPI getSmallTurretCover() {
        return drone.getSmallTurretCover();
    }

    @Override
    public SpriteAPI getSmallHardpointCover() {
        return drone.getSmallHardpointCover();
    }

    @Override
    public SpriteAPI getMediumTurretCover() {
        return drone.getMediumTurretCover();
    }

    @Override
    public SpriteAPI getMediumHardpointCover() {
        return drone.getMediumHardpointCover();
    }

    @Override
    public SpriteAPI getLargeTurretCover() {
        return drone.getLargeTurretCover();
    }

    @Override
    public SpriteAPI getLargeHardpointCover() {
        return drone.getLargeHardpointCover();
    }

    @Override
    public boolean isDefenseDisabled() {
        return drone.isDefenseDisabled();
    }

    @Override
    public void setDefenseDisabled(boolean defenseDisabled) {
        drone.setDefenseDisabled(defenseDisabled);
    }

    @Override
    public void setPhased(boolean phased) {
        drone.setPhased(phased);
    }

    @Override
    public void setExtraAlphaMult(float transparency) {
        drone.setExtraAlphaMult(transparency);
    }

    @Override
    public void setApplyExtraAlphaToEngines(boolean applyExtraAlphaToEngines) {
        drone.setApplyExtraAlphaToEngines(applyExtraAlphaToEngines);
    }

    @Override
    public void setOverloadColor(Color color) {
        drone.setOverloadColor(color);
    }

    @Override
    public void resetOverloadColor() {
        drone.resetOverloadColor();
    }

    @Override
    public Color getOverloadColor() {
        return drone.getOverloadColor();
    }

    @Override
    public boolean isRecentlyShotByPlayer() {
        return drone.isRecentlyShotByPlayer();
    }

    @Override
    public float getMaxSpeedWithoutBoost() {
        return drone.getMaxSpeedWithoutBoost();
    }

    @Override
    public float getHardFluxLevel() {
        return drone.getHardFluxLevel();
    }

    @Override
    public void fadeToColor(Object source, Color color, float durIn, float durOut, float maxShift) {
        drone.fadeToColor(source, color, durIn, durOut, maxShift);
    }

    @Override
    public boolean isShowModuleJitterUnder() {
        return drone.isShowModuleJitterUnder();
    }

    @Override
    public void setShowModuleJitterUnder(boolean showModuleJitterUnder) {
        drone.setShowModuleJitterUnder(showModuleJitterUnder);
    }

    @Override
    public void addAfterimage(Color color, float locX, float locY, float velX, float velY, float maxJitter, float in, float dur, float out, boolean additive, boolean combineWithSpriteColor, boolean aboveShip) {
        drone.addAfterimage(color, locX, locY, velX, velY, maxJitter, in, dur, out,  additive, combineWithSpriteColor, aboveShip);
    }

    @Override
    public PersonAPI getCaptain() {
        return drone.getCaptain();
    }

    @Override
    public WeaponSlotAPI getStationSlot() {
        return drone.getStationSlot();
    }

    @Override
    public void setStationSlot(WeaponSlotAPI stationSlot) {
        drone.setStationSlot(stationSlot);
    }

    @Override
    public ShipAPI getParentStation() {
        return drone.getParentStation();
    }

    @Override
    public void setParentStation(ShipAPI station) {
        drone.setParentStation(station);
    }

    @Override
    public Vector2f getFixedLocation() {
        return drone.getFixedLocation();
    }

    @Override
    public void setFixedLocation(Vector2f fixedLocation) {
        drone.setFixedLocation(fixedLocation);
    }

    @Override
    public boolean hasRadarRibbonIcon() {
        return drone.hasRadarRibbonIcon();
    }

    @Override
    public boolean isTargetable() {
        return drone.isTargetable();
    }

    @Override
    public void setStation(boolean isStation) {
        drone.setStation(isStation);
    }

    @Override
    public boolean isSelectableInWarroom() {
        return drone.isSelectableInWarroom();
    }

    @Override
    public boolean isShipWithModules() {
        return drone.isShipWithModules();
    }

    @Override
    public void setShipWithModules(boolean isShipWithModules) {
        drone.setShipWithModules(isShipWithModules);
    }

    @Override
    public List<ShipAPI> getChildModulesCopy() {
        return drone.getChildModulesCopy();
    }

    @Override
    public boolean isPiece() {
        return drone.isPiece();
    }

    @Override
    public BoundsAPI getVisualBounds() {
        return drone.getVisualBounds();
    }

    @Override
    public Vector2f getRenderOffset() {
        return drone.getRenderOffset();
    }

    @Override
    public ShipAPI splitShip() {
        return drone.splitShip();
    }

    @Override
    public int getNumFighterBays() {
        return drone.getNumFighterBays();
    }

    @Override
    public boolean isPullBackFighters() {
        return drone.isPullBackFighters();
    }

    @Override
    public void setPullBackFighters(boolean pullBackFighters) {
        drone.setPullBackFighters(pullBackFighters);
    }

    @Override
    public boolean hasLaunchBays() {
        return drone.hasLaunchBays();
    }

    @Override
    public List<FighterLaunchBayAPI> getLaunchBaysCopy() {
        return drone.getLaunchBaysCopy();
    }

    @Override
    public float getFighterTimeBeforeRefit() {
        return drone.getFighterTimeBeforeRefit();
    }

    @Override
    public void setFighterTimeBeforeRefit(float fighterTimeBeforeRefit) {
        drone.setFighterTimeBeforeRefit(fighterTimeBeforeRefit);
    }

    @Override
    public List<FighterWingAPI> getAllWings() {
        return drone.getAllWings();
    }

    @Override
    public float getSharedFighterReplacementRate() {
        return drone.getSharedFighterReplacementRate();
    }

    @Override
    public boolean areSignificantEnemiesInRange() {
        return drone.areSignificantEnemiesInRange();
    }

    @Override
    public List<WeaponAPI> getUsableWeapons() {
        return drone.getUsableWeapons();
    }

    @Override
    public Vector2f getModuleOffset() {
        return drone.getModuleOffset();
    }

    @Override
    public float getMassWithModules() {
        return drone.getMassWithModules();
    }

    @Override
    public PersonAPI getOriginalCaptain() {
        return drone.getOriginalCaptain();
    }

    @Override
    public boolean isRenderEngines() {
        return drone.isRenderEngines();
    }

    @Override
    public void setRenderEngines(boolean renderEngines) {
        drone.setRenderEngines(renderEngines);
    }

    @Override
    public WeaponGroupAPI getSelectedGroupAPI() {
        return drone.getSelectedGroupAPI();
    }

    @Override
    public void setHullSize(HullSize hullSize) {
        drone.setHullSize(hullSize);
    }

    @Override
    public void ensureClonedStationSlotSpec() {
        drone.ensureClonedStationSlotSpec();
    }

    @Override
    public void setMaxHitpoints(float maxArmor) {
        drone.setMaxHitpoints(maxArmor);
    }

    @Override
    public void setDHullOverlay(String spriteName) {
        drone.setDHullOverlay(spriteName);
    }

    @Override
    public boolean isStation() {
        return drone.isStation();
    }

    @Override
    public boolean isStationModule() {
        return drone.isStationModule();
    }

    @Override
    public boolean areAnyEnemiesInRange() {
        return drone.areAnyEnemiesInRange();
    }

    @Override
    public void blockCommandForOneFrame(ShipCommand command) {
        drone.blockCommandForOneFrame(command);
    }

    @Override
    public float getMaxTurnRate() {
        return drone.getMaxTurnRate();
    }

    @Override
    public float getTurnAcceleration() {
        return drone.getTurnAcceleration();
    }

    @Override
    public float getTurnDeceleration() {
        return drone.getTurnDeceleration();
    }

    @Override
    public float getDeceleration() {
        return drone.getDeceleration();
    }

    @Override
    public float getAcceleration() {
        return drone.getAcceleration();
    }

    @Override
    public float getMaxSpeed() {
        return drone.getMaxSpeed();
    }

    @Override
    public float getFluxLevel() {
        return drone.getFluxLevel();
    }

    @Override
    public float getCurrFlux() {
        return drone.getCurrFlux();
    }

    @Override
    public float getMaxFlux() {
        return drone.getMaxFlux();
    }

    @Override
    public float getMinFluxLevel() {
        return drone.getMinFluxLevel();
    }

    @Override
    public float getMinFlux() {
        return drone.getMinFlux();
    }

    @Override
    public void setLightDHullOverlay() {
        drone.setLightDHullOverlay();
    }

    @Override
    public void setMediumDHullOverlay() {
        drone.setMediumDHullOverlay();
    }

    @Override
    public void setHeavyDHullOverlay() {
        drone.setHeavyDHullOverlay();
    }

    @Override
    public boolean isJitterShields() {
        return drone.isJitterShields();
    }

    @Override
    public void setJitterShields(boolean jitterShields) {
        drone.setJitterShields(jitterShields);
    }

    @Override
    public boolean isInvalidTransferCommandTarget() {
        return drone.isInvalidTransferCommandTarget();
    }

    @Override
    public void setInvalidTransferCommandTarget(boolean invalidTransferCommandTarget) {
        drone.setInvalidTransferCommandTarget(invalidTransferCommandTarget);
    }

    @Override
    public void clearDamageDecals() {
        drone.clearDamageDecals();
    }

    @Override
    public void syncWithArmorGridState() {
        drone.syncWithArmorGridState();
    }

    @Override
    public void syncWeaponDecalsWithArmorDamage() {
        drone.syncWeaponDecalsWithArmorDamage();
    }

    @Override
    public boolean isDirectRetreat() {
        return drone.isDirectRetreat();
    }

    @Override
    public void setRetreating(boolean retreating, boolean direct) {
        drone.setRetreating(retreating, direct);
    }

    @Override
    public boolean isLiftingOff() {
        return drone.isLiftingOff();
    }

    @Override
    public void setVariantForHullmodCheckOnly(ShipVariantAPI variant) {
        drone.setVariantForHullmodCheckOnly(variant);
    }

    @Override
    public Vector2f getShieldCenterEvenIfNoShield() {
        return drone.getShieldCenterEvenIfNoShield();
    }

    @Override
    public float getShieldRadiusEvenIfNoShield() {
        return drone.getShieldRadiusEvenIfNoShield();
    }

    @Override
    public FleetMemberAPI getFleetMember() {
        return drone.getFleetMember();
    }

    @Override
    public Vector2f getShieldTarget() {
        return drone.getShieldTarget();
    }

    @Override
    public void setShieldTargetOverride(float x, float y) {
        drone.setShieldTargetOverride(x, y);
    }

    @Override
    public CombatListenerManagerAPI getListenerManager() {
        return drone.getListenerManager();
    }

    @Override
    public void addListener(Object listener) {
        drone.addListener(listener);
    }

    @Override
    public void removeListener(Object listener) {
        drone.removeListener(listener);
    }

    @Override
    public void removeListenerOfClass(Class<?> c) {
        drone.removeListenerOfClass(c);
    }

    @Override
    public boolean hasListener(Object listener) {
        return drone.hasListener(listener);
    }

    @Override
    public boolean hasListenerOfClass(Class<?> c) {
        return drone.hasListenerOfClass(c);
    }

    @Override
    public <T> List<T> getListeners(Class<T> c) {
        return drone.getListeners(c);
    }

    @Override
    public Object getParamAboutToApplyDamage() {
        return drone.getParamAboutToApplyDamage();
    }

    @Override
    public void setParamAboutToApplyDamage(Object param) {
        drone.setParamAboutToApplyDamage(param);
    }

    @Override
    public float getFluxBasedEnergyWeaponDamageMultiplier() {
        return drone.getFluxBasedEnergyWeaponDamageMultiplier();
    }

    @Override
    public void setName(String name) {
        drone.setName(name);
    }

    @Override
    public void setHulk(boolean isHulk) {
        drone.setHulk(isHulk);
    }

    @Override
    public void setCaptain(PersonAPI captain) {
        drone.setCaptain(captain);
    }

    @Override
    public float getShipExplosionRadius() {
        return drone.getShipExplosionRadius();
    }

    @Override
    public void setCircularJitter(boolean circular) {
        drone.setCircularJitter(circular);
    }

    @Override
    public float getExtraAlphaMult() {
        return drone.getExtraAlphaMult();
    }

    @Override
    public void setAlphaMult(float alphaMult) {
        drone.setAlphaMult(alphaMult);
    }

    @Override
    public float getAlphaMult() {
        return drone.getAlphaMult();
    }

    @Override
    public Vector2f getLocation() {
        return drone.getLocation();
    }

    @Override
    public Vector2f getVelocity() {
        return drone.getVelocity();
    }

    @Override
    public float getFacing() {
        return drone.getFacing();
    }

    @Override
    public void setFacing(float facing) {
        drone.setFacing(facing);
    }

    @Override
    public float getAngularVelocity() {
        return drone.getAngularVelocity();
    }

    @Override
    public void setAngularVelocity(float angVel) {
        drone.setAngularVelocity(angVel);
    }

    @Override
    public int getOwner() {
        return drone.getOwner();
    }

    @Override
    public void setOwner(int owner) {
        drone.setOwner(owner);
    }

    @Override
    public float getCollisionRadius() {
        return drone.getCollisionRadius();
    }

    @Override
    public CollisionClass getCollisionClass() {
        return drone.getCollisionClass();
    }

    @Override
    public void setCollisionClass(CollisionClass collisionClass) {
        drone.setCollisionClass(collisionClass);
    }

    @Override
    public float getMass() {
        return drone.getMass();
    }

    @Override
    public void setMass(float mass) {
        drone.setMass(mass);
    }

    @Override
    public BoundsAPI getExactBounds() {
        return drone.getExactBounds();
    }

    @Override
    public ShieldAPI getShield() {
        return drone.getShield();
    }

    @Override
    public float getHullLevel() {
        return drone.getHullLevel();
    }

    @Override
    public float getHitpoints() {
        return drone.getHitpoints();
    }

    @Override
    public float getMaxHitpoints() {
        return drone.getMaxHitpoints();
    }

    @Override
    public void setCollisionRadius(float radius) {
        drone.setCollisionRadius(radius);
    }

    @Override
    public Object getAI() {
        return drone.getAI();
    }

    @Override
    public boolean isExpired() {
        return drone.isExpired();
    }

    @Override
    public void setCustomData(String key, Object data) {
        drone.setCustomData(key, data);
    }

    @Override
    public void removeCustomData(String key) {
        drone.removeCustomData(key);
    }

    @Override
    public Map<String, Object> getCustomData() {
        return drone.getCustomData();
    }
}

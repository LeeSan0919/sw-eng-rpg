package swg_eng_rpg;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class GameState {
    private final Hacker player;
    private final Inventory inventory;
    private Region currentRegion;
    private final EnumSet<Region> clearedRegions;

    public GameState(String playerName) {
        player = new Hacker(playerName);
        inventory = new Inventory();
        currentRegion = Region.HIDEOUT;
        clearedRegions = EnumSet.noneOf(Region.class);
    }

    Hacker player() { return player; }
    Inventory inventory() { return inventory; }

    void setCurrentRegion(Region region) {
        if (region == null) throw new IllegalArgumentException("지역이 필요합니다.");
        currentRegion = region;
    }

    void markCleared(Region region) {
        if (region == null || !region.isBattleRegion()) {
            throw new IllegalArgumentException("전투 지역이 필요합니다.");
        }
        clearedRegions.add(region);
    }

    public Region getCurrentRegion() { return currentRegion; }
    public boolean isCleared(Region region) { return clearedRegions.contains(region); }

    public boolean isUnlocked(Region region) {
        if (region == null) return false;
        switch (region) {
            case HIDEOUT:
            case WEB: return true;
            case FILE:
            case AUTH: return isCleared(Region.WEB);
            case CORE: return isCleared(Region.FILE) && isCleared(Region.AUTH);
            default: return false;
        }
    }

    public boolean isGameCleared() { return isCleared(Region.CORE); }

    public Set<Region> getClearedRegions() {
        return Collections.unmodifiableSet(EnumSet.copyOf(clearedRegions));
    }
}

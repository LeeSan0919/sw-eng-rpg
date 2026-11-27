package swg_eng_rpg;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class GameManager {
    private final GameState state;
    private BattleManager battle;
    private Region battleRegion;

    public GameManager(String playerName) { state = new GameState(playerName); }
    public boolean isInBattle() { return battle != null; }

    // 전투 밖, 연출 대기 중, 종료된 전투에서는 false를 반환한다.
    public boolean canAct() {
        return battle != null && battle.canAct();
    }

    public boolean canCallAiAgent() {
        return battle != null && battle.canCallAiAgent();
    }

    // 전투 중 복구 버튼용 조회. 전투 밖 사용은 useRecoveryItem()으로 처리한다.
    public boolean canUseRecovery() {
        return battle != null && battle.canUseRecovery();
    }

    public OperationResult moveTo(Region target) {
        if (isInBattle()) return result(false, "전투 중에는 이동할 수 없습니다.");
        if (target == null) return result(false, "지역을 선택하세요.");
        if (!state.isUnlocked(target)) return result(false, "접근 불가: " + target.getUnlockCondition());
        state.setCurrentRegion(target);
        if (!target.isBattleRegion()) return result(true, "은신처에 도착했습니다.");
        if (state.isCleared(target)) return result(true, "이미 공략한 지역입니다. 전투는 발생하지 않습니다.");
        battleRegion = target;
        battle = new BattleManager(state.player(), target.createEnemy(), state.inventory());
        return result(true, target.getDisplayName() + " 접속! " + target.getEnemyName() + " 등장!");
    }

    public ActionResult performBattleAction(BattleAction action) {
        if (battle == null) throw new IllegalStateException("진행 중인 전투가 없습니다.");
        return battle.performAction(action);
    }

    public ActionResult.Snapshot getBattleSnapshot() {
        if (battle == null) throw new IllegalStateException("진행 중인 전투가 없습니다.");
        return battle.getSnapshot();
    }

    public String getEnemyName() {
        return battleRegion == null ? "" : battleRegion.getEnemyName();
    }

    // 이미지 선택용 ID. 전투 밖에서는 빈 문자열을 반환한다.
    public String getEnemyTypeId() {
        return battleRegion == null ? "" : battleRegion.getEnemyTypeId();
    }

    // 해당 전투의 모든 연출을 표시한 뒤 호출한다.
    public OperationResult completeBattlePresentation(int turn) {
        if (battle == null) return result(false, "진행 중인 전투가 없습니다.");
        if (!battle.completePresentation(turn)) return result(false, "유효하지 않은 연출 완료 요청입니다.");
        ActionResult.Outcome outcome = battle.getSnapshot().outcome;
        if (outcome == ActionResult.Outcome.ONGOING) return result(true, "다음 행동을 선택하세요.");
        Region finishedRegion = battleRegion;
        String message;
        if (outcome == ActionResult.Outcome.VICTORY) {
            // 경험치는 BattleManager에서 이미 지급했으므로 다시 지급하지 않는다.
            state.markCleared(finishedRegion);
            message = finishedRegion.getDisplayName() + " 공략 완료!";
            if (finishedRegion == Region.WEB) {
                message += " 파일 서버와 인증 서버가 열렸습니다.";
            } else if (finishedRegion == Region.FILE || finishedRegion == Region.AUTH) {
                if (state.isUnlocked(Region.CORE)) message += " 중앙 서버가 열렸습니다.";
            } else if (finishedRegion == Region.CORE) {
                message += " 모든 시스템을 공략했습니다!";
            }
        } else {
            state.setCurrentRegion(Region.HIDEOUT);
            state.player().heal(state.player().getMaxHealthPoint());
            message = "패배하여 은신처로 복귀했습니다. 체력을 모두 회복했습니다.";
        }
        battle = null;
        battleRegion = null;
        return result(true, message);
    }

    public OperationResult restAtHideout() {
        if (isInBattle()) return result(false, "전투 중에는 정비할 수 없습니다.");
        if (state.getCurrentRegion() != Region.HIDEOUT) return result(false, "은신처에서만 정비할 수 있습니다.");
        int recovered = state.player().heal(state.player().getMaxHealthPoint());
        if (recovered == 0) return result(false, "이미 최대 체력입니다.");
        return result(true, "정비 완료: 체력 +" + recovered);
    }

    public OperationResult useRecoveryItem() {
        if (isInBattle()) return result(false, "전투 메뉴에서 복구를 선택하세요.");
        if (state.inventory().getRecoveryCount() == 0) return result(false, "복구 프로그램이 없습니다.");
        if (state.player().isDead()) return result(false, "쓰러진 상태에서는 사용할 수 없습니다.");
        if (!state.inventory().canUseRecovery(state.player())) return result(false, "이미 최대 체력입니다.");
        int recovered = state.inventory().useRecovery(state.player());
        return result(true, "복구 프로그램 사용: 체력 +" + recovered);
    }

    public GameView getView() { return new GameView(state, isInBattle()); }

    private OperationResult result(boolean success, String message) {
        return new OperationResult(success, message, getView());
    }

    // GUI는 원본 캐릭터 대신 이 읽기 전용 복사본을 사용한다.
    public static final class GameView {
        public final String playerName;
        public final int hp, maxHp, strength, level, recoveryCount;
        public final long experience, requiredExperience;
        public final Region currentRegion;
        public final boolean inBattle, gameCleared;
        public final Set<Region> unlockedRegions, clearedRegions;

        private GameView(GameState state, boolean inBattle) {
            Hacker player = state.player();
            playerName = player.getName();
            hp = player.getHealthPoint();
            maxHp = player.getMaxHealthPoint();
            strength = player.getStrength();
            level = player.getLevel();
            experience = player.getExperience();
            requiredExperience = player.getRequiredExperience();
            recoveryCount = state.inventory().getRecoveryCount();
            currentRegion = state.getCurrentRegion();
            this.inBattle = inBattle;
            gameCleared = state.isGameCleared();
            EnumSet<Region> unlocked = EnumSet.noneOf(Region.class);
            for (Region region : Region.values()) {
                if (state.isUnlocked(region)) unlocked.add(region);
            }
            unlockedRegions = Collections.unmodifiableSet(unlocked);
            clearedRegions = state.getClearedRegions();
        }
    }

    public static final class OperationResult {
        public final boolean success;
        public final String message;
        public final GameView view;

        private OperationResult(boolean success, String message, GameView view) {
            this.success = success;
            this.message = message;
            this.view = view;
        }
    }
}

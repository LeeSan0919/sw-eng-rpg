package swg_eng_rpg_001;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 한 전투를 담당한다. 캐릭터 변경은 이 관리자를 통해 수행한다. */
public class BattleManager {
    public static final int REQUIRED_CHARGE = 3;
    public static final int AI_DAMAGE_MULTIPLIER = 2;

    private final Hacker player;
    private final SecurityProgram enemy;
    private final Inventory inventory;
    private int charge;
    private int turn;
    private boolean awaitingPresentation;
    private ActionResult.Outcome outcome = ActionResult.Outcome.ONGOING;

    public BattleManager(Hacker player, SecurityProgram enemy) {
        this(player, enemy, new Inventory(0));
    }

    /** 게임 전체에서 사용하는 인벤토리를 전달한다. */
    public BattleManager(Hacker player, SecurityProgram enemy, Inventory inventory) {
        if (player == null || enemy == null || player.isDead() || enemy.isDead()) {
            throw new IllegalArgumentException("살아 있는 플레이어와 적이 필요합니다.");
        }
        this.player = player;
        this.enemy = enemy;
        if (inventory == null) throw new IllegalArgumentException("인벤토리가 필요합니다.");
        this.inventory = inventory;
    }

    /** 한 번의 플레이어 행동과 필요한 적 반격을 처리한다. */
    public ActionResult performAction(BattleAction action) {
        if (outcome != ActionResult.Outcome.ONGOING) return reject("이미 종료된 전투입니다.");
        if (awaitingPresentation) return reject("이전 행동의 연출이 진행 중입니다.");
        if (action == null) return reject("행동을 선택하세요.");
        if (player.isDead() || enemy.isDead()) return reject("전투 밖에서 캐릭터 상태가 변경되었습니다.");
        if (action == BattleAction.USE_RECOVERY) {
            if (inventory.getRecoveryCount() == 0) return reject("복구 프로그램이 없습니다.");
            if (player.getHealthPoint() == player.getMaxHealthPoint()) return reject("이미 최대 체력입니다.");
        }
        if (action == BattleAction.CALL_AI_AGENT && charge < REQUIRED_CHARGE) {
            return reject("AI 분석이 완료되지 않았습니다.");
        }

        turn++;
        awaitingPresentation = true;
        List<ActionResult.BattleEvent> events = new ArrayList<>();
        boolean defending = action == BattleAction.DEFEND;
        if (action == BattleAction.CALL_AI_AGENT) {
            charge = 0;
            // long으로 먼저 계산하여 큰 공격력에서도 정수 넘침을 방지한다.
            int requested = (int) Math.min(Integer.MAX_VALUE,
                    (long) player.getStrength() * AI_DAMAGE_MULTIPLIER);
            int damage = enemy.takeDamage(requested);
            add(events, ActionResult.EventType.AI_AGENT, "ChatGPT 에이전트 호출! " + damage + " 피해", damage);
        } else {
            charge = Math.min(REQUIRED_CHARGE, charge + 1);
            if (action == BattleAction.USE_RECOVERY) {
                int recovered = inventory.useRecovery(player);
                events.add(new ActionResult.BattleEvent(ActionResult.EventType.RECOVERY,
                        "복구 프로그램 사용: 체력 +" + recovered, 0, recovered, getSnapshot()));
            } else if (defending) {
                add(events, ActionResult.EventType.DEFEND, "방어막 전개: 이번 반격 피해를 절반으로 줄입니다.", 0);
            } else {
                int before = enemy.getHealthPoint();
                player.attack(enemy);
                int damage = before - enemy.getHealthPoint();
                add(events, ActionResult.EventType.PLAYER_ATTACK, player.getName() + " 공격: " + damage + " 피해", damage);
            }
        }

        if (enemy.isDead()) {
            outcome = ActionResult.Outcome.VICTORY;
            add(events, ActionResult.EventType.VICTORY, enemy.getName() + " 무력화!", 0);
            int reward = enemy.getRewardExperience();
            int levels = player.gainExperience(reward);
            add(events, ActionResult.EventType.REWARD,
                    "경험치 +" + reward + (levels > 0 ? ", 레벨 +" + levels : ""), 0);
        } else {
            int before = player.getHealthPoint();
            if (defending) {
                // 홀수 피해의 절반은 올림한다. 공격력 1이면 방어해도 피해 1.
                int reduced = enemy.getStrength() / 2 + enemy.getStrength() % 2;
                player.takeDamage(reduced);
            } else {
                enemy.attack(player);
            }
            int damage = before - player.getHealthPoint();
            add(events, ActionResult.EventType.ENEMY_ATTACK, enemy.getName() + " 반격: " + damage + " 피해", damage);
            if (player.isDead()) {
                outcome = ActionResult.Outcome.DEFEAT;
                add(events, ActionResult.EventType.DEFEAT, "해커 아바타가 쓰러졌습니다.", 0);
            }
        }
        return new ActionResult(true, "행동을 처리했습니다.", getSnapshot(), events);
    }

    /** 모든 이벤트 연출을 마친 뒤 result.state.turn을 전달한다. 오래된 완료 알림은 무시한다. */
    public boolean completePresentation(int completedTurn) {
        if (!awaitingPresentation || completedTurn != turn) return false;
        awaitingPresentation = false;
        return true;
    }

    public boolean canAct() {
        return outcome == ActionResult.Outcome.ONGOING && !awaitingPresentation
                && !player.isDead() && !enemy.isDead();
    }

    public boolean canCallAiAgent() {
        return canAct() && charge >= REQUIRED_CHARGE;
    }

    public ActionResult.Snapshot getSnapshot() {
        return new ActionResult.Snapshot(player, enemy, charge, turn, outcome, inventory.getRecoveryCount());
    }

    public boolean canUseRecovery() {
        return canAct() && inventory.canUseRecovery(player);
    }

    private ActionResult reject(String message) {
        return new ActionResult(false, message, getSnapshot(), Collections.emptyList());
    }

    private void add(List<ActionResult.BattleEvent> events, ActionResult.EventType type,
                     String message, int damage) {
        events.add(new ActionResult.BattleEvent(type, message, damage, getSnapshot()));
    }
}

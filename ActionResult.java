package swg_eng_rpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 화면에 전달하는 읽기 전용 결과. 캐릭터 객체 대신 시점별 수치를 보관한다. */
public final class ActionResult {
    public enum Outcome { ONGOING, VICTORY, DEFEAT }
    public enum EventType { PLAYER_ATTACK, DEFEND, AI_AGENT, RECOVERY, ENEMY_ATTACK, VICTORY, DEFEAT, REWARD }

    public static final class Snapshot {
        public final int playerHp, playerMaxHp, playerStrength, playerLevel;
        public final long experience, requiredExperience;
        public final int enemyHp, enemyMaxHp, charge, turn;
        public final int recoveryCount;
        public final Outcome outcome;

        Snapshot(Hacker player, SecurityProgram enemy, int charge, int turn, Outcome outcome, int recoveryCount) {
            playerHp = player.getHealthPoint();
            playerMaxHp = player.getMaxHealthPoint();
            playerStrength = player.getStrength();
            playerLevel = player.getLevel();
            experience = player.getExperience();
            requiredExperience = player.getRequiredExperience();
            enemyHp = enemy.getHealthPoint();
            enemyMaxHp = enemy.getMaxHealthPoint();
            this.charge = charge;
            this.turn = turn;
            this.outcome = outcome;
            this.recoveryCount = recoveryCount;
        }
    }

    public static final class BattleEvent {
        public final EventType type;
        public final String message;
        public final int damage;
        public final int recovered;
        public final Snapshot state;

        BattleEvent(EventType type, String message, int damage, Snapshot state) {
            this(type, message, damage, 0, state);
        }

        BattleEvent(EventType type, String message, int damage, int recovered, Snapshot state) {
            this.type = type;
            this.message = message;
            this.damage = damage;
            this.recovered = recovered;
            this.state = state;
        }
    }

    public final boolean success;
    public final String message;
    public final Snapshot state;
    public final List<BattleEvent> events;

    ActionResult(boolean success, String message, Snapshot state, List<BattleEvent> events) {
        this.success = success;
        this.message = message;
        this.state = state;
        this.events = Collections.unmodifiableList(new ArrayList<>(events));
    }
}

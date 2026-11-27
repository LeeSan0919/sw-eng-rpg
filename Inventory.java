package swg_eng_rpg;

/** 플레이어가 보유한 도구. 전투마다 새로 만들지 않고 같은 객체를 전달한다. */
public class Inventory {
    public static final int INITIAL_RECOVERY_COUNT = 3;
    public static final int RECOVERY_AMOUNT = 30;
    private int recoveryCount;

    public Inventory() {
        this(INITIAL_RECOVERY_COUNT);
    }

    public Inventory(int recoveryCount) {
        if (recoveryCount < 0) {
            throw new IllegalArgumentException("보유 수량은 음수일 수 없습니다.");
        }
        this.recoveryCount = recoveryCount;
    }

    public int getRecoveryCount() { return recoveryCount; }

    public boolean canUseRecovery(GameCharacter target) {
        return target != null && !target.isDead() && recoveryCount > 0
                && target.getHealthPoint() < target.getMaxHealthPoint();
    }

    /** 게임 로직에서 호출한다. 사용 불가 시 0, 성공 시 실제 회복량을 반환한다. */
    int useRecovery(GameCharacter target) {
        if (!canUseRecovery(target)) return 0;
        int recovered = target.heal(RECOVERY_AMOUNT);
        recoveryCount--;
        return recovered;
    }
}

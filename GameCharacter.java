/** 수업 GameCharacter를 확장한 공통 캐릭터. 턴 진행은 BattleManager가 담당한다. */
public class GameCharacter {
    private String name;
    private int healthPoint;
    private int maxHealthPoint;
    private int strength;

    public GameCharacter(String characterName) {
        this(characterName, 50, 50);
    }

    public GameCharacter(String characterName, int initHp, int initStr) {
        if (initHp <= 0) {
            throw new IllegalArgumentException("최대 체력은 양수여야 합니다.");
        }
        setName(characterName);
        setStrength(initStr);
        maxHealthPoint = initHp;
        healthPoint = initHp;
    }

    /** 일반 공격. 적 처치 보상과 반격은 여기서 처리하지 않는다. */
    public void attack(GameCharacter enemy) {
        if (enemy == null || enemy == this) {
            throw new IllegalArgumentException("공격 대상을 확인하세요.");
        }
        if (isDead() || enemy.isDead()) {
            throw new IllegalStateException("쓰러진 캐릭터는 공격하거나 공격받을 수 없습니다.");
        }
        enemy.takeDamage(strength);
    }

    /** 실제 감소한 체력을 반환한다. */
    public int takeDamage(int amount) {
        requireNonNegative(amount);
        int damage = Math.min(amount, healthPoint);
        healthPoint -= damage;
        return damage;
    }

    /** 실제 회복량을 반환한다. 은신처에서 체력 0인 캐릭터도 회복 가능하다. */
    public int heal(int amount) {
        requireNonNegative(amount);
        int recovered = Math.min(amount, maxHealthPoint - healthPoint);
        healthPoint += recovered;
        return recovered;
    }

    public boolean isDead() {
        return healthPoint == 0;
    }

    public String getName() { return name; }
    public int getHealthPoint() { return healthPoint; }
    public int getMaxHealthPoint() { return maxHealthPoint; }
    public int getStrength() { return strength; }

    public final void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름이 필요합니다.");
        }
        this.name = name.trim();
    }

    /** 수업 코드의 메서드 유지. 체력은 0~최대 체력 범위로 제한한다. */
    public final void setHealthPoint(int healthPoint) {
        this.healthPoint = Math.max(0, Math.min(healthPoint, maxHealthPoint));
    }

    public final void setStrength(int strength) {
        requireNonNegative(strength);
        this.strength = strength;
    }

    /** 자식 클래스의 성장 처리에 사용. 최대 체력 증가 시 자동 회복하지 않는다. */
    protected final void increaseStats(int hpIncrease, int strengthIncrease) {
        requireNonNegative(hpIncrease);
        requireNonNegative(strengthIncrease);
        int newMaxHp = Math.addExact(maxHealthPoint, hpIncrease);
        int newStrength = Math.addExact(strength, strengthIncrease);
        maxHealthPoint = newMaxHp;
        strength = newStrength;
    }

    private static void requireNonNegative(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("수치는 음수일 수 없습니다.");
        }
    }

    public void printStatus() {
        System.out.println("Name: " + name);
        System.out.println("HP: " + healthPoint + "/" + maxHealthPoint);
        System.out.println("Strength: " + strength);
    }
}

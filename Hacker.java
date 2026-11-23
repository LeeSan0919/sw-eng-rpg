/** 플레이어 캐릭터. 수치는 첫 구현용 임시 밸런스이다. */
public class Hacker extends GameCharacter {
    public static final int INITIAL_HP = 100;
    public static final int INITIAL_STRENGTH = 15;
    public static final int HP_PER_LEVEL = 20;
    public static final int STRENGTH_PER_LEVEL = 5;
    public static final int EXPERIENCE_PER_LEVEL = 100;

    private int level = 1;
    private long experience;

    public Hacker(String name) {
        super(name, INITIAL_HP, INITIAL_STRENGTH);
    }

    /** 경험치를 지급하고 상승한 레벨 수를 반환한다. 초과 경험치는 유지한다. */
    public int gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("경험치는 음수일 수 없습니다.");
        }
        experience += amount;
        int gainedLevels = 0;
        while (experience >= getRequiredExperience()) {
            long required = getRequiredExperience();
            increaseStats(HP_PER_LEVEL, STRENGTH_PER_LEVEL);
            experience -= required;
            level++;
            gainedLevels++;
        }
        return gainedLevels;
    }

    public int getLevel() { return level; }
    public long getExperience() { return experience; }

    /** 현재 레벨에서 다음 레벨까지 필요한 전체 경험치. */
    public long getRequiredExperience() {
        return (long) EXPERIENCE_PER_LEVEL * level;
    }

    @Override
    public void printStatus() {
        super.printStatus();
        System.out.println("Level: " + level);
        System.out.println("EXP: " + experience + "/" + getRequiredExperience());
    }
}

package swg_eng_rpg;

/** 방화벽 등 적의 공통 클래스. 같은 종류라도 전투마다 새 객체를 생성한다. */
public class SecurityProgram extends GameCharacter {
    private final String typeId;
    private final int rewardExperience;

    public SecurityProgram(String typeId, String name, int hp,
                           int strength, int rewardExperience) {
        super(name, hp, strength);
        if (typeId == null || typeId.trim().isEmpty()) {
            throw new IllegalArgumentException("적 종류 ID가 필요합니다.");
        }
        if (rewardExperience < 0) {
            throw new IllegalArgumentException("보상 경험치는 음수일 수 없습니다.");
        }
        this.typeId = typeId.trim();
        this.rewardExperience = rewardExperience;
    }

    public String getTypeId() { return typeId; }
    public int getRewardExperience() { return rewardExperience; }
}

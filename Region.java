package swg_eng_rpg;

public enum Region {
    HIDEOUT("은신처", null, null, 0, 0, 0),
    WEB("웹 서버", "firewall", "방화벽", 80, 10, 120),
    FILE("파일 서버", "antivirus", "백신 프로그램", 100, 15, 150),
    AUTH("인증 서버", "auth_guard", "인증 관리자", 120, 16, 180),
    CORE("중앙 서버", "core_guard", "중앙 보안 시스템", 180, 22, 250);

    private final String displayName, enemyType, enemyName;
    private final int enemyHp, enemyStrength, rewardExperience;

    Region(String displayName, String enemyType, String enemyName,
           int enemyHp, int enemyStrength, int rewardExperience) {
        this.displayName = displayName;
        this.enemyType = enemyType;
        this.enemyName = enemyName;
        this.enemyHp = enemyHp;
        this.enemyStrength = enemyStrength;
        this.rewardExperience = rewardExperience;
    }

    public String getDisplayName() { return displayName; }
    public String getEnemyName() { return enemyName; }
    // 은신처에는 적이 없으므로 빈 문자열을 반환한다.
    public String getEnemyTypeId() { return enemyType == null ? "" : enemyType; }
    public boolean isBattleRegion() { return this != HIDEOUT; }

    public String getUnlockCondition() {
        switch (this) {
            case FILE:
            case AUTH: return "웹 서버 공략";
            case CORE: return "파일 서버와 인증 서버 모두 공략";
            default: return "처음부터 접근 가능";
        }
    }

    // 재도전 시에도 항상 초기 체력인 새로운 적을 생성한다.
    public SecurityProgram createEnemy() {
        if (!isBattleRegion()) throw new IllegalStateException("은신처에는 적이 없습니다.");
        return new SecurityProgram(enemyType, enemyName, enemyHp, enemyStrength, rewardExperience);
    }
}

package swg_eng_rpg_001;

import java.util.Scanner;

/** GUI 연결 전 전투 기능을 직접 확인하기 위한 콘솔 실행 진입점. */
public class Main {
    public static void main(String[] args) {
        Hacker hacker = new Hacker("해커");
        SecurityProgram firewall = new SecurityProgram("firewall", "방화벽", 80, 10, 120);
        Inventory inventory = new Inventory();
        BattleManager battle = new BattleManager(hacker, firewall, inventory);

        System.out.println("=== 해커 RPG: 방화벽 전투 ===");
        System.out.println("공격·방어·복구 사용에 성공할 때마다 AI 분석이 1 충전됩니다.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (battle.getSnapshot().outcome == ActionResult.Outcome.ONGOING) {
                showStatus(battle.getSnapshot());
                System.out.println("1. 공격  2. 방어  3. AI 에이전트  4. 복구 사용  5. 인벤토리  0. 종료");
                System.out.print("행동 선택 > ");

                if (!scanner.hasNextLine()) {
                    System.out.println("\n입력이 종료되어 게임을 마칩니다.");
                    return;
                }
                String input = scanner.nextLine().trim();
                if (input.equals("5")) {
                    System.out.println("\n[인벤토리] 복구 프로그램: " + battle.getSnapshot().recoveryCount + "개");
                    System.out.println("효과: 체력 최대 " + Inventory.RECOVERY_AMOUNT + " 회복. 전투 중 사용 성공 시 한 턴 소비.");
                    System.out.println("인벤토리 확인은 턴을 소비하지 않습니다.");
                    continue;
                }
                if (input.equals("0")) {
                    System.out.println("전투를 중단합니다. 진행 상태는 저장되지 않습니다.");
                    return;
                }

                BattleAction action;
                switch (input) {
                    case "1": action = BattleAction.ATTACK; break;
                    case "2": action = BattleAction.DEFEND; break;
                    case "3": action = BattleAction.CALL_AI_AGENT; break;
                    case "4": action = BattleAction.USE_RECOVERY; break;
                    default:
                        System.out.println("0~5 중 하나를 입력하세요. 턴은 소비되지 않습니다.");
                        continue;
                }

                ActionResult result = battle.performAction(action);
                if (!result.success) {
                    System.out.println(result.message);
                    continue;
                }
                System.out.println("\n[" + result.state.turn + "턴 기록]");
                for (ActionResult.BattleEvent event : result.events) {
                    System.out.println(event.message);
                }
                // 콘솔은 출력 즉시 연출 완료. GUI에서는 이미지 연출 종료 후 호출한다.
                battle.completePresentation(result.state.turn);
            }
        }

        ActionResult.Snapshot finalState = battle.getSnapshot();
        System.out.println(finalState.outcome == ActionResult.Outcome.VICTORY
                ? "\n=== 전투 승리 ===" : "\n=== 전투 패배 ===");
        showStatus(finalState);
    }

    private static void showStatus(ActionResult.Snapshot state) {
        System.out.println("\n완료한 턴: " + state.turn);
        System.out.println("해커 | 레벨 " + state.playerLevel
                + " | HP " + state.playerHp + "/" + state.playerMaxHp
                + " | 공격력 " + state.playerStrength);
        System.out.println("경험치: " + state.experience + "/" + state.requiredExperience);
        System.out.println("방화벽 | HP " + state.enemyHp + "/" + state.enemyMaxHp);
        System.out.println("복구 프로그램: " + state.recoveryCount + "개");
        System.out.println("AI 분석: " + state.charge + "/" + BattleManager.REQUIRED_CHARGE
                + (state.charge >= BattleManager.REQUIRED_CHARGE
                   && state.outcome == ActionResult.Outcome.ONGOING ? " (호출 가능)" : ""));
    }
}

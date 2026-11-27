package swg_eng_rpg;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        GameManager game = new GameManager("해커");
        System.out.println("=== 해커 RPG: 네트워크 탐험 ===");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                if (game.isInBattle()) showBattle(game);
                else showMap(game.getView());
                System.out.print("선택 > ");
                if (!scanner.hasNextLine()) {
                    System.out.println("\n입력이 종료되었습니다.");
                    return;
                }
                String input = scanner.nextLine().trim();
                if (input.equals("0")) {
                    System.out.println("게임을 종료합니다. 저장 기능은 아직 없습니다.");
                    return;
                }
                if (game.isInBattle()) handleBattle(game, input);
                else handleMap(game, input);
            }
        }
    }

    private static void showMap(GameManager.GameView view) {
        System.out.println("\n=== 네트워크 지도 ===");
        showPlayer(view);
        System.out.println("현재 위치: " + view.currentRegion.getDisplayName());
        Region[] regions = Region.values();
        for (int i = 0; i < regions.length; i++) {
            Region region = regions[i];
            String status;
            if (view.clearedRegions.contains(region)) status = "공략 완료";
            else if (view.unlockedRegions.contains(region)) status = "접근 가능";
            else status = "잠김: " + region.getUnlockCondition();
            System.out.println((i + 1) + ". " + region.getDisplayName() + " [" + status + "]");
        }
        System.out.println("6. 은신처 정비\n7. 인벤토리 확인\n8. 복구 프로그램 사용\n0. 종료");
        if (view.gameCleared) System.out.println("[전체 공략 완료]");
    }

    private static void handleMap(GameManager game, String input) {
        switch (input) {
            case "1": printResult(game.moveTo(Region.HIDEOUT)); break;
            case "2": printResult(game.moveTo(Region.WEB)); break;
            case "3": printResult(game.moveTo(Region.FILE)); break;
            case "4": printResult(game.moveTo(Region.AUTH)); break;
            case "5": printResult(game.moveTo(Region.CORE)); break;
            case "6": printResult(game.restAtHideout()); break;
            case "7": showInventory(game.getView()); break;
            case "8": printResult(game.useRecoveryItem()); break;
            default: System.out.println("0~8 중 하나를 입력하세요.");
        }
    }

    private static void showBattle(GameManager game) {
        ActionResult.Snapshot state = game.getBattleSnapshot();
        System.out.println("\n=== " + game.getEnemyName() + " 전투 ===");
        System.out.println("완료한 턴: " + state.turn);
        System.out.println("해커 HP: " + state.playerHp + "/" + state.playerMaxHp);
        System.out.println(game.getEnemyName() + " HP: " + state.enemyHp + "/" + state.enemyMaxHp);
        System.out.println("AI 분석: " + state.charge + "/" + BattleManager.REQUIRED_CHARGE);
        System.out.println("복구 프로그램: " + state.recoveryCount + "개");
        System.out.println("1. 공격  2. 방어  3. AI 에이전트\n4. 복구 사용  5. 인벤토리  0. 종료");
    }

    private static void handleBattle(GameManager game, String input) {
        if (input.equals("5")) { showInventory(game.getView()); return; }
        BattleAction action;
        switch (input) {
            case "1": action = BattleAction.ATTACK; break;
            case "2": action = BattleAction.DEFEND; break;
            case "3": action = BattleAction.CALL_AI_AGENT; break;
            case "4": action = BattleAction.USE_RECOVERY; break;
            default: System.out.println("0~5 중 하나를 입력하세요."); return;
        }
        ActionResult result = game.performBattleAction(action);
        if (!result.success) { System.out.println(result.message); return; }
        System.out.println("\n[" + result.state.turn + "턴 기록]");
        for (ActionResult.BattleEvent event : result.events) System.out.println(event.message);
        GameManager.OperationResult completed = game.completeBattlePresentation(result.state.turn);
        if (!completed.success || !game.isInBattle()) printResult(completed);
    }

    private static void showPlayer(GameManager.GameView view) {
        System.out.println(view.playerName + " | 레벨 " + view.level
                + " | HP " + view.hp + "/" + view.maxHp + " | 공격력 " + view.strength);
        System.out.println("경험치: " + view.experience + "/" + view.requiredExperience);
    }

    private static void showInventory(GameManager.GameView view) {
        System.out.println("\n[인벤토리]\n복구 프로그램: " + view.recoveryCount + "개");
        System.out.println("효과: 체력 최대 " + Inventory.RECOVERY_AMOUNT + " 회복");
        System.out.println("전투 중 사용에 성공하면 한 턴을 소비합니다. 조회는 턴을 소비하지 않습니다.");
    }

    private static void printResult(GameManager.OperationResult result) {
        System.out.println(result.message);
    }
}

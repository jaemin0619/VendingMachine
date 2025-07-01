package Gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Drink;
import model.DrinkInventory;
import network.AdminWebSocketClient;
import network.SocketClient;
import service.AdminManager;
import dao.DrinkInventoryDAO;
import service.MoneyManager;
import service.SalesLogger;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

/**
 * 관리자 패널 GUI 클래스
 * - 비밀번호 변경, 재고 보충, 가격 변경, 매출 조회, 수금, 서버 전송 등 관리자 기능 통합
 */
public class AdminPanel extends Stage {
    private final MoneyManager moneyManager;        // 화폐 관련 로직 처리 객체
    private final DrinkInventory inventory;         // 음료 재고 객체
    private final Button[] drinkButtons;            // 메인화면 음료 버튼 참조 (GUI 동기화용)

    public AdminPanel(DrinkInventory inventory, Button[] drinkButtons, MoneyManager moneyManager) {
        this.moneyManager = moneyManager;
        this.inventory = inventory;
        this.drinkButtons = drinkButtons;

        setTitle("관리자 패널");

        // 관리자 메뉴 버튼 UI 구성
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20;");

        // 각각 기능별 버튼 생성 및 이벤트 바인딩
        Button pwChangeBtn = new Button("비밀번호 변경");
        pwChangeBtn.setOnAction(e -> openPasswordChangeDialog());

        Button restockMenuBtn = new Button("보충하기");
        restockMenuBtn.setOnAction(e -> openRestockPanel());

        Button editMenuBtn = new Button("이름/가격 변경");
        editMenuBtn.setOnAction(e -> openEditDrinkInfoDialog());

        Button viewSalesBtn = new Button("총 매출 보기");
        viewSalesBtn.setOnAction(e -> openSalesReport());

        Button dailySalesBtn = new Button("일별 매출");
        dailySalesBtn.setOnAction(e -> showDailySales());

        Button monthlySalesBtn = new Button("월별 매출");
        monthlySalesBtn.setOnAction(e -> showMonthlySales());

        Button viewCoinsBtn = new Button("화폐 현황");
        viewCoinsBtn.setOnAction(e -> showCoinStatus());

        Button collectBtn = new Button("수금하기");
        collectBtn.setOnAction(e -> collectCoins());

        Button sendToServerBtn = new Button("서버로 로그 전송");
        sendToServerBtn.setOnAction(e -> {
            // DB 기반 로그 전송 (Socket 활용)
            SocketClient.sendLogToServerFromDB();
            showAlert("서버 전송", "서버로 DB 로그 전송 완료!");
        });

        // 버튼들을 HBox에 추가
        HBox buttonBox = new HBox(10, pwChangeBtn, restockMenuBtn, editMenuBtn, viewSalesBtn, dailySalesBtn,
                monthlySalesBtn, viewCoinsBtn, collectBtn, sendToServerBtn);
        root.getChildren().add(buttonBox);

        Scene scene = new Scene(root, 1100, 160);
        setScene(scene);
    }

    /** 관리자 재고 보충 패널 */
    private void openRestockPanel() {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 20;");

        for (int i = 0; i < inventory.getDrinks().size(); i++) {
            int index = i;
            Drink drink = inventory.getDrink(index);

            Label label = new Label(drink.getName() + " (현재 재고: " + drink.getStock() + ")");
            TextField inputField = new TextField();
            inputField.setPromptText("보충 수량 입력");

            Button restockBtn = new Button("보충");
            restockBtn.setOnAction(e -> {
                try {
                    int amount = Integer.parseInt(inputField.getText());
                    if (amount <= 0) throw new NumberFormatException();

                    // 재고 증가
                    drink.restock(amount);
                    drinkButtons[index].setText(getButtonText(drink));
                    drinkButtons[index].setDisable(false);

                    // DB 저장 및 WebSocket으로 클라이언트 동기화
                    DrinkInventoryDAO.saveInventoryToDB(inventory);
                    AdminWebSocketClient.sendEditMessage(index, drink.getName(), drink.getPrice(), drink.getStock());

                    showAlert("보충 완료", drink.getName() + " 재고가 " + amount + "개 추가되었습니다.");
                } catch (NumberFormatException ex) {
                    showAlert("입력 오류", "올바른 수량을 입력하세요.");
                }
            });

            HBox row = new HBox(10, label, inputField, restockBtn);
            root.getChildren().add(row);
        }

        Stage dialog = new Stage();
        dialog.setTitle("재고 보충");
        dialog.setScene(new Scene(root, 500, 400));
        dialog.show();
    }

    /** 음료 이름/가격 수정 다이얼로그 */
    private void openEditDrinkInfoDialog() {
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20;");

        for (int i = 0; i < inventory.getDrinks().size(); i++) {
            int index = i;
            Drink drink = inventory.getDrink(index);

            TextField nameField = new TextField(drink.getName());
            TextField priceField = new TextField(String.valueOf(drink.getPrice()));

            Button saveBtn = new Button("저장");
            saveBtn.setOnAction(e -> {
                try {
                    String newName = nameField.getText().trim();
                    int newPrice = Integer.parseInt(priceField.getText());

                    // 음료 정보 업데이트
                    drink.setName(newName);
                    drink.setPrice(newPrice);

                    // DB 및 버튼 동기화
                    drinkButtons[index].setText(getButtonText(drink));
                    DrinkInventoryDAO.saveInventoryToDB(inventory);
                    AdminWebSocketClient.sendEditMessage(index, newName, newPrice, drink.getStock());

                    showAlert("수정 완료", "음료 정보가 수정되었습니다.");

                } catch (NumberFormatException ex) {
                    showAlert("입력 오류", "가격은 숫자여야 합니다.");
                }
            });

            HBox row = new HBox(10, new Label((index + 1) + "번: "), nameField, priceField, saveBtn);
            root.getChildren().add(row);
        }

        Stage dialog = new Stage();
        dialog.setTitle("음료 정보 수정");
        dialog.setScene(new Scene(root, 500, 400));
        dialog.show();
    }

    /** 음료 버튼 텍스트 구성 */
    private String getButtonText(Drink drink) {
        return drink.getName() + "\n(" + drink.getPrice() + "원)\n재고: " + drink.getStock();
    }

    /** 총 매출 보고서 (파일 기반 팝업) */
    private void openSalesReport() {
        SalesLogger.showSalesReportDialog();
    }

    /** 일별 매출 출력 */
    private void showDailySales() {
        Map<LocalDate, Integer> map = SalesLogger.getDailySales();
        StringBuilder sb = new StringBuilder("📅 일별 매출\n");
        for (var entry : map.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("원\n");
        }
        showAlert("일별 매출", sb.toString());
    }

    /** 월별 매출 출력 */
    private void showMonthlySales() {
        Map<String, Integer> map = SalesLogger.getMonthlySales();
        StringBuilder sb = new StringBuilder("📆 월별 매출\n");
        for (var entry : map.entrySet()) {
            sb.append(entry.getKey()).append(" : ").append(entry.getValue()).append("원\n");
        }
        showAlert("월별 매출", sb.toString());
    }

    /** 화폐 현황 출력 */
    private void showCoinStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("💰 자판기 내 화폐 현황:\n\n");
        for (Map.Entry<Integer, Integer> entry : moneyManager.getCoinStock().entrySet()) {
            sb.append(entry.getKey()).append("원: ").append(entry.getValue()).append("개\n");
        }
        sb.append("\n총 보유 금액: ").append(moneyManager.getTotalStoredMoney()).append("원");

        showAlert("화폐 현황", sb.toString());
    }

    /** 수금 기능 (기본 최소 잔액 보유) */
    private void collectCoins() {
        int collected = moneyManager.collectCoins(5); // 최소 5개 잔여
        showAlert("수금 완료", "수금된 금액: " + collected + "원\n(각 화폐 최소 5개 남김)");
    }

    /** 알림창 출력 */
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /** 관리자 비밀번호 변경 다이얼로그 */
    private void openPasswordChangeDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("비밀번호 변경");

        Label currentLabel = new Label("현재 비밀번호:");
        Label newLabel = new Label("새 비밀번호:");

        PasswordField currentPw = new PasswordField();
        PasswordField newPw = new PasswordField();

        // 입력 폼 구성
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(currentLabel, 0, 0);
        grid.add(currentPw, 1, 0);
        grid.add(newLabel, 0, 1);
        grid.add(newPw, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // 결과 변환 및 유효성 검사
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(currentPw.getText(), newPw.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(pair -> {
            String current = pair.getKey();
            String newP = pair.getValue();

            AdminManager adminManager = new AdminManager();
            if (!adminManager.checkPassword(current)) {
                showAlert("오류", "현재 비밀번호가 틀렸습니다.");
            } else if (!adminManager.isValidPassword(newP)) {
                showAlert("오류", "비밀번호는 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.");
            } else {
                adminManager.changePassword(newP);
                showAlert("성공", "비밀번호가 변경되었습니다.");
            }
        });
    }
}

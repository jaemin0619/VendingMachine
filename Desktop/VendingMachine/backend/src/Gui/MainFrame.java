// MainFrame.java
package Gui;

import dao.DrinkInventoryDAO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Drink;
import model.DrinkInventory;
import model.PurchaseHistory;
import network.AdminWebSocketClient;
import network.AdminWebSocketServer;
import service.AdminManager;
import service.MoneyManager;
import service.SalesLogger;

import java.io.*;
import java.net.Socket;
import java.net.URI;

public class MainFrame extends Application {

    // 클라이언트 ID 구분용 (기본값: Client1)
    private String clientId = "Client1";

    // 주요 구성 요소 선언
    private MoneyManager moneyManager = new MoneyManager();
    private Label balanceLabel;
    private DrinkInventory inventory;
    private Button[] drinkButtons = new Button[8];
    private PurchaseHistory purchaseHistory = new PurchaseHistory();
    private AdminManager adminManager = new AdminManager();

    // 서버와의 TCP 연결을 위한 소켓 및 스트림
    private Socket logSocket;
    private BufferedWriter logWriter;
    private BufferedReader logReader; // 서버 경고 수신용 reader

    // 음료 이미지 파일 경로
    private final String[] imageFiles = {
            "/resource/MixCoffee.jpg",
            "/resource/ExpenciveCoffee.jpg",
            "/resource/water.jpg",
            "/resource/canCoffee.jpg",
            "/resource/pocari.jpg",
            "/resource/expenciveCanCoffe.jpg",
            "/resource/Coke.jpg",
            "/resource/legend.jpg"
    };

    // 버튼에 표시될 텍스트 구성
    private String getButtonText(Drink drink) {
        return drink.getName() + "\n(" + drink.getPrice() + "원)\n재고: " + drink.getStock();
    }

    @Override
    public void start(Stage primaryStage) {
        // 명령줄 인자에서 클라이언트 ID 설정
        var args = getParameters().getRaw();
        if (!args.isEmpty()) {
            clientId = args.get(0);
        }

        // DB에서 음료 재고 로드
        inventory = DrinkInventoryDAO.loadInventoryFromDB();

        // Client1일 경우 WebSocket 서버 실행
        if ("Client1".equals(clientId)) {
            new Thread(() -> {
                try {
                    AdminWebSocketServer server = new AdminWebSocketServer(3001, inventory);
                    server.start();
                } catch (Exception e) {
                    System.err.println("WebSocket 서버 실행 실패: " + e.getMessage());
                }
            }).start();
        }

        // 서버와 TCP 연결 시도 (로그 전송 및 경고 수신)
        try {
            logSocket = new Socket("localhost", 12345);
            logWriter = new BufferedWriter(new OutputStreamWriter(logSocket.getOutputStream()));
            logReader = new BufferedReader(new InputStreamReader(logSocket.getInputStream()));

            // 경고 메시지 수신 스레드
            new Thread(() -> {
                String line;
                try {
                    while ((line = logReader.readLine()) != null) {
                        String msg = line.trim();
                        if (!msg.isEmpty()) {
                            Platform.runLater(() -> showAlert("서버 경고", msg));
                        }
                    }
                } catch (IOException e) {
                    System.err.println("서버 경고 수신 실패: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            System.err.println("TCP 연결 실패: " + e.getMessage());
        }

        // GUI 구성 시작
        primaryStage.setTitle("자판기 관리 프로그램 - " + clientId);
        BorderPane root = new BorderPane();
        root.setStyle("-fx-padding: 20; -fx-background-color: #e6f2ff;");

        // 음료 버튼 영역 (그리드)
        GridPane drinkGrid = new GridPane();
        drinkGrid.setHgap(15);
        drinkGrid.setVgap(15);
        drinkGrid.setAlignment(Pos.CENTER);

        for (int i = 0; i < 8; i++) {
            int index = i;
            Drink drink = inventory.getDrink(index);

            // 이미지 불러오기 및 버튼 구성
            InputStream imageStream = getClass().getResourceAsStream(imageFiles[i]);
            ImageView imageView = null;
            if (imageStream != null) {
                Image image = new Image(imageStream, 50, 50, true, true);
                imageView = new ImageView(image);
            }

            Button drinkBtn = new Button(getButtonText(drink), imageView);
            drinkBtn.setContentDisplay(ContentDisplay.TOP);
            drinkBtn.setPrefSize(150, 130);
            drinkBtn.setWrapText(true);
            drinkBtn.setStyle("-fx-font-weight: bold;");
            drinkBtn.setOnAction(e -> handlePurchase(index));

            drinkButtons[i] = drinkBtn;
            drinkGrid.add(drinkBtn, i % 4, i / 4);
        }

        // 화폐 투입 버튼 구성
        HBox moneyBox = new HBox(10);
        moneyBox.setAlignment(Pos.CENTER);
        int[] coins = {10, 50, 100, 500, 1000};
        for (int coin : coins) {
            Button coinBtn = new Button(coin + "원");
            coinBtn.setOnAction(e -> {
                insertMoney(coin);
                moneyManager.addCoinToStock(coin);
                updateDrinkButtons();
            });
            moneyBox.getChildren().add(coinBtn);
        }

        // 기능 버튼 (잔액 반환, 관리자, 구매이력)
        Button returnBtn = new Button("잔액 반환");
        returnBtn.setOnAction(e -> {
            returnMoney();
            updateDrinkButtons();
        });

        Button adminBtn = new Button("관리자 메뉴");
        adminBtn.setOnAction(e -> openAdminLogin());

        Button viewHistoryBtn = new Button("구매 이력 보기");
        viewHistoryBtn.setOnAction(e -> showAlert("최근 구매", purchaseHistory.toString()));

        HBox actionButtons = new HBox(10, returnBtn, adminBtn, viewHistoryBtn);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10, 0, 0, 0));

        // 화면 중앙 배치
        VBox centerBox = new VBox(15, drinkGrid, moneyBox, actionButtons);
        centerBox.setAlignment(Pos.CENTER);

        // 하단 잔액 표시
        balanceLabel = new Label("현재 금액: 0원");
        balanceLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox bottomBox = new HBox(balanceLabel);
        bottomBox.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBox.setPadding(new Insets(10));

        root.setCenter(centerBox);
        root.setBottom(bottomBox);

        // 종료 시 DB 저장 및 소켓 종료 처리
        Scene scene = new Scene(root, 700, 500);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            DrinkInventoryDAO.saveInventoryToDB(inventory);
            try {
                if (logReader != null) logReader.close();
                if (logWriter != null) logWriter.close();
                if (logSocket != null) logSocket.close();
            } catch (IOException e) {
                System.err.println("로그 종료 실패: " + e.getMessage());
            }
        });
        primaryStage.show();

        updateDrinkButtons();

        // WebSocket 클라이언트 연결 시도
        try {
            URI uri = new URI("ws://localhost:3001");
            AdminWebSocketClient wsClient = new AdminWebSocketClient(uri, inventory, drinkButtons);
            wsClient.connect();
        } catch (Exception e) {
            System.err.println("WebSocket 연결 실패: " + e.getMessage());
        }
    }

    // 금액 투입 처리
    private void insertMoney(int amount) {
        boolean success = moneyManager.insertMoney(amount);
        if (!success) {
            if (moneyManager.getBalance() + amount > 7000) {
                showAlert("입금 제한", "총 투입금은 7000원을 초과할 수 없습니다.");
            } else {
                showAlert("입금 제한", "지폐는 5000원까지만 입력할 수 있습니다.");
            }
        }
        updateBalanceLabel();
    }

    // 잔액 반환 처리
    private void returnMoney() {
        var change = moneyManager.returnChange();
        if (change != null && !change.isEmpty()) {
            StringBuilder sb = new StringBuilder("반환된 거스름돈:\n");
            for (var entry : change.entrySet()) {
                sb.append(entry.getKey()).append("원 x ").append(entry.getValue()).append("\n");
            }
            showAlert("잔액 반환", sb.toString());
        } else {
            showAlert("반환 없음", "반환할 잔액이 없습니다.");
        }
        updateBalanceLabel();
    }

    // 음료 구매 처리
    private void handlePurchase(int index) {
        Drink drink = inventory.getDrink(index);

        if (drink.isSoldOut()) {
            showAlert("품절", "해당 음료는 품절입니다.");
            return;
        }

        int price = drink.getPrice();
        if (moneyManager.getBalance() < price) {
            showAlert("잔액 부족", "금액이 부족합니다.");
        } else {
            moneyManager.spendMoney(price);
            drink.reduceStock();
            purchaseHistory.add(drink.getName());

            try {
                SalesLogger.logSale(clientId, drink.getName(), price, 1);
            } catch (IOException e) {
                showAlert("오류", "매출 기록에 실패했습니다.");
            }

            Platform.runLater(() -> {
                updateBalanceLabel();
                updateDrinkButtons();
            });

            showAlert("구매 완료", drink.getName() + " 구매 완료!");
        }
    }

    // 잔액 라벨 갱신
    private void updateBalanceLabel() {
        balanceLabel.setText("현재 금액: " + moneyManager.getBalance() + "원");
    }

    // 버튼 상태 및 색상 업데이트
    private void updateDrinkButtons() {
        for (int i = 0; i < drinkButtons.length; i++) {
            Drink drink = inventory.getDrink(i);
            Button btn = drinkButtons[i];
            btn.setText(getButtonText(drink));
            boolean canBuy = !drink.isSoldOut() && drink.getPrice() <= moneyManager.getBalance();
            btn.setDisable(drink.isSoldOut());
            btn.setStyle(canBuy ? "-fx-background-color: lightgreen; -fx-font-weight: bold;"
                    : "-fx-background-color: lightcoral; -fx-font-weight: bold;");
        }
    }

    // 팝업 알림 표시
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // 관리자 로그인 다이얼로그
    private void openAdminLogin() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("관리자 로그인");
        dialog.setHeaderText("관리자 비밀번호를 입력하세요");

        Label pwLabel = new Label("비밀번호:");
        PasswordField pwField = new PasswordField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(pwLabel, 0, 0);
        grid.add(pwField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return pwField.getText();
            }
            return null;
        });
        dialog.showAndWait().ifPresent(inputPw -> {
            if (adminManager.checkPassword(inputPw)) {
                new AdminPanel(inventory, drinkButtons, moneyManager).show();
            } else {
                showAlert("접근 거부", "비밀번호가 틀렸습니다.");
            }
        });
    }
    public static void main(String[] args) {
        launch(args);
    }
}

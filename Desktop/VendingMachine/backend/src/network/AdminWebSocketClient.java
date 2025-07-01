// AdminWebSocketClient.java
package network;

import model.Drink;
import model.DrinkInventory;
import javafx.application.Platform;
import javafx.scene.control.Button;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;

import java.net.URI;

/**
 * 관리자 웹소켓 클라이언트
 * - 서버로 수정 메시지를 전송하고,
 * - 서버로부터 수신된 메시지로 클라이언트 음료 정보 및 UI를 동기화합니다.
 */
public class AdminWebSocketClient extends WebSocketClient {

    private static AdminWebSocketClient instance;
    private DrinkInventory inventory; // 음료 재고 참조
    private Button[] buttons;         // 버튼 UI 참조 배열

    public AdminWebSocketClient(URI serverUri, DrinkInventory inventory, Button[] buttons) {
        super(serverUri);
        this.inventory = inventory;
        this.buttons = buttons;
        instance = this; // 싱글톤처럼 사용
    }

    /**
     * 서버에 음료 수정 메시지 전송
     */
    public static void sendEditMessage(int id, String name, int price, int stock) {
        if (instance != null && instance.isOpen()) {
            JSONObject json = new JSONObject();
            json.put("type", "edit");
            json.put("id", id);
            json.put("name", name);
            json.put("price", price);
            json.put("stock", stock);
            instance.send(json.toString());
        } else {
            System.err.println("WebSocket 연결이 안 되어 있어 메시지를 보낼 수 없습니다.");
        }
    }

    /**
     * WebSocket 연결 성공 시 호출됨
     */
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("WebSocket 연결됨!");
    }

    /**
     * 서버로부터 메시지 수신 시 처리
     */
    @Override
    public void onMessage(String message) {
        System.out.println("수신: " + message);

        // UI 관련 작업은 JavaFX UI 스레드에서 처리해야 함
        Platform.runLater(() -> {
            try {
                JSONObject json = new JSONObject(message);
                String type = json.getString("type");

                if (type.equals("edit")) {
                    int id = json.getInt("id");
                    String name = json.getString("name");
                    int price = json.getInt("price");
                    int stock = json.getInt("stock");

                    if (id >= 0 && id < inventory.getDrinks().size()) {
                        Drink drink = inventory.getDrink(id);
                        drink.setName(name);
                        drink.setPrice(price);
                        drink.restock(stock - drink.getStock());
                    }

                } else if (type.equals("restock")) {
                    int id = json.getInt("id");
                    int amount = json.getInt("amount");
                    if (id >= 0 && id < inventory.getDrinks().size()) {
                        Drink drink = inventory.getDrink(id);
                        drink.restock(amount);
                    }
                }

                // 음료 버튼 UI 업데이트
                for (int i = 0; i < buttons.length; i++) {
                    Drink d = inventory.getDrink(i);
                    buttons[i].setText(d.getName() + "\n(" + d.getPrice() + "원)\n재고: " + d.getStock());
                }

            } catch (Exception e) {
                System.err.println("JSON 메시지 처리 오류: " + e.getMessage());
            }
        });
    }

    /**
     * 연결 종료 시 호출됨
     */
    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("연결 종료: " + reason);
    }

    /**
     * 오류 발생 시 호출됨
     */
    @Override
    public void onError(Exception ex) {
        System.err.println("오류: " + ex.getMessage());
    }
}

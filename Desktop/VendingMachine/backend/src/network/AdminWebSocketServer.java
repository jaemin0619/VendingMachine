// AdminWebSocketServer.java
package network;

import dao.DrinkInventoryDAO;
import model.Drink;
import model.DrinkInventory;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.json.JSONArray;
import org.json.JSONObject;
import service.SalesLogger;

import java.net.InetSocketAddress;
import java.time.LocalDate;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminWebSocketServer
 * - 관리자 요청(WebSocket)을 수신하여 서버에서 재고, 매출, 비밀번호 등을 처리하는 WebSocket 서버
 */
public class AdminWebSocketServer extends WebSocketServer {

    public static final Logger logger = Logger.getLogger(AdminWebSocketServer.class.getName());
    private final DrinkInventory inventory;

    public AdminWebSocketServer(int port, DrinkInventory inventory) {
        super(new InetSocketAddress(port));
        this.inventory = inventory;
    }

    @Override
    public void onStart() {
        logger.info("WebSocket 서버 시작됨!");
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("연결됨: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("수신: " + message);
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");

            switch (type) {
                case "restock" -> {
                    int restockId = json.getInt("id");
                    int amount = json.getInt("amount");
                    logger.info("재고 보충 요청 - ID: " + restockId + ", 수량: " + amount);
                    Drink restockDrink = inventory.getDrink(restockId);
                    restockDrink.restock(amount);
                    DrinkInventoryDAO.saveInventoryToDB(inventory);
                    broadcast(new JSONObject()
                            .put("type", "edit")
                            .put("id", restockId)
                            .put("name", restockDrink.getName())
                            .put("price", restockDrink.getPrice())
                            .put("stock", restockDrink.getStock())
                            .toString());
                    conn.send("재고 보충 완료");
                }

                case "edit" -> {
                    int editId = json.getInt("id");
                    String name = json.getString("name");
                    int price = json.getInt("price");
                    logger.info("음료 수정 요청 - ID: " + editId + ", 이름: " + name + ", 가격: " + price);
                    Drink editDrink = inventory.getDrink(editId);
                    editDrink.setName(name);
                    editDrink.setPrice(price);
                    DrinkInventoryDAO.saveInventoryToDB(inventory);
                    broadcast(new JSONObject()
                            .put("type", "edit")
                            .put("id", editId)
                            .put("name", editDrink.getName())
                            .put("price", editDrink.getPrice())
                            .put("stock", editDrink.getStock())
                            .toString());
                    conn.send("음료 정보 수정 완료");
                }

                case "collect" -> {
                    logger.info("수금 요청");
                    conn.send("수금 완료");
                }

                case "changePassword" -> {
                    String newPw = json.getString("newPassword");
                    logger.info("비밀번호 변경 요청: " + newPw);
                    conn.send("비밀번호 변경 완료");
                }

                case "viewSales" -> {
                    String viewType = json.getString("viewType");
                    logger.info("📊 매출 조회 요청: " + viewType);

                    JSONArray salesArr = new JSONArray();
                    JSONObject salesResponse = new JSONObject();
                    salesResponse.put("type", "salesData");
                    salesResponse.put("salesType", viewType);

                    switch (viewType) {
                        case "daily" -> {
                            Map<LocalDate, Integer> map = SalesLogger.getDailySales();
                            for (var entry : map.entrySet()) {
                                JSONObject item = new JSONObject();
                                item.put("date", entry.getKey().toString());
                                item.put("total", entry.getValue());
                                salesArr.put(item);
                            }
                        }
                        case "monthly" -> {
                            Map<String, Integer> map = SalesLogger.getMonthlySales();
                            for (var entry : map.entrySet()) {
                                JSONObject item = new JSONObject();
                                item.put("month", entry.getKey());
                                item.put("total", entry.getValue());
                                salesArr.put(item);
                            }
                        }
                        case "total" -> {
                            int sum = SalesLogger.getDailySales().values().stream().mapToInt(Integer::intValue).sum();
                            JSONObject item = new JSONObject();
                            item.put("total", sum);
                            salesArr.put(item);
                        }
                        default -> {
                            conn.send(new JSONObject().put("message", "지원하지 않는 매출 유형입니다: " + viewType).toString());
                            return;
                        }
                    }

                    salesResponse.put("data", salesArr);
                    conn.send(salesResponse.toString());
                }

                case "sendLog" -> {
                    logger.info("로그 전송 요청 수신");
                    conn.send("로그 전송 완료");
                }

                case "getInventory" -> {
                    JSONArray arr = new JSONArray();
                    for (int i = 0; i < inventory.getDrinks().size(); i++) {
                        Drink d = inventory.getDrink(i);
                        JSONObject obj = new JSONObject();
                        obj.put("id", i);
                        obj.put("name", d.getName());
                        obj.put("price", d.getPrice());
                        obj.put("stock", d.getStock());
                        arr.put(obj);
                    }
                    JSONObject res = new JSONObject();
                    res.put("type", "inventory");
                    res.put("data", arr);
                    conn.send(res.toString());
                }

                default -> {
                    logger.warning("알 수 없는 명령: " + type);
                    conn.send("지원하지 않는 명령입니다: " + type);
                }
            }
        } catch (Exception e) {
            logger.warning("JSON 파싱 오류: " + e.getMessage());
            conn.send("명령 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("연결 종료: " + conn.getRemoteSocketAddress() + " (사유: " + reason + ")");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.log(Level.SEVERE, "WebSocket 오류 발생", ex);
    }

    public static void main(String[] args) {
        int port = 3001;
        DrinkInventory inventory = new DrinkInventory();
        AdminWebSocketServer server = new AdminWebSocketServer(port, inventory);
        server.start();
        logger.info("WebSocket 서버 실행 중: ws://localhost:" + port);
    }
}

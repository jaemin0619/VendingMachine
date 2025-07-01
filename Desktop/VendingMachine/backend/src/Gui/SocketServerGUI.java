// SocketServerGUI.java
package Gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 자판기 매출 수신 및 경고 전송을 위한 GUI 기반 TCP 서버 클래스
 */
public class SocketServerGUI extends Application {

    private TextArea logArea; // 로그 출력용 텍스트 영역
    private final int PORT = 12345; // 수신 포트 번호
    private final Map<String, Integer> stockMap = new HashMap<>(); // 음료별 재고 관리용 Map

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("📡 자판기 서버 GUI");

        // 로그 출력창 구성
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(500);

        VBox root = new VBox(logArea);
        Scene scene = new Scene(root, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // 서버 시작 스레드 실행
        new Thread(this::startServer).start();
    }

    /**
     * TCP 서버 소켓 실행 및 클라이언트 수신 루프
     */
    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            log("📡 서버 시작됨! 포트: " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log("🔗 클라이언트 연결됨: " + clientSocket.getInetAddress());

                // 클라이언트별 독립 처리 스레드 생성
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (Exception e) {
            log("❌ 서버 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 클라이언트 연결 처리 로직
     */
    private void handleClient(Socket clientSocket) {
        BufferedReader in = null;
        BufferedWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            while (true) {
                String line;
                try {
                    line = in.readLine();
                    if (line == null) {
                        log("🚪 클라이언트 연결 종료 감지됨");
                        break;
                    }

                    // Base64 디코딩 처리
                    String decoded;
                    try {
                        decoded = new String(Base64.getDecoder().decode(line));
                    } catch (IllegalArgumentException decodeEx) {
                        log("❌ Base64 디코딩 실패: " + decodeEx.getMessage());
                        continue;
                    }

                    log("📥 수신된 매출 로그: " + decoded);

                    // 로그 포맷: 날짜,음료명,가격,수량
                    String[] parts = decoded.split(",");
                    if (parts.length == 4) {
                        String drinkName = parts[1];
                        int qty = Integer.parseInt(parts[3]);

                        // 재고 감소 처리
                        int newStock = stockMap.getOrDefault(drinkName, 10) - qty;
                        stockMap.put(drinkName, newStock);

                        // 재고 부족 시 경고 메시지 전송
                        if (newStock <= 3) {
                            String msg = "⚠️ " + drinkName + " 재고 부족!";
                            try {
                                out.write(msg);
                                out.newLine();
                                out.flush();
                                log("📤 경고 전송됨: " + msg);
                            } catch (IOException sendEx) {
                                log("❌ 경고 전송 실패: " + sendEx.getMessage());
                                break; // 전송 실패 시 루프 종료
                            }
                        }
                    }

                } catch (IOException readEx) {
                    log("❌ 수신 중 오류: " + readEx.getMessage());
                    break;
                }
            }

        } catch (IOException e) {
            log("❌ 클라이언트 처리 중 오류: " + e.getMessage());

        } finally {
            // 자원 정리 및 연결 종료 로그 출력
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                log("🚪 클라이언트 연결 정상 종료됨");
            } catch (IOException ex) {
                log("❌ 소켓 종료 오류: " + ex.getMessage());
            }
        }
    }

    /**
     * 로그 메시지 출력 (GUI 스레드에서 실행)
     */
    private void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

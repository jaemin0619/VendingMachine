// network/SocketClient.java
package network;

import util.DBManager;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Base64;

/**
 * 서버로 매출 로그를 전송하는 클라이언트
 * - DB에서 매출 데이터를 조회하여
 * - Base64로 인코딩한 후 TCP 소켓을 통해 서버로 전송한다.
 */
public class SocketClient {

    /**
     * sales_log 테이블에서 데이터를 읽어 서버로 전송
     * - sale_date, drink_name, price, quantity 필드를 읽어 ","로 연결된 문자열 생성
     * - 해당 문자열을 Base64로 인코딩 후 한 줄씩 전송
     */
    public static void sendLogToServerFromDB() {
        try (
                // 서버에 연결 (localhost:12345)
                Socket socket = new Socket("localhost", 12345);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Connection conn = DBManager.getConnection() // DB 연결
        ) {

            // 매출 로그 조회 쿼리
            String sql = "SELECT sale_date, drink_name, price, quantity FROM sales_log ORDER BY sale_date ASC";
            try (
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    ResultSet rs = pstmt.executeQuery()
            ) {

                while (rs.next()) {
                    // DB 필드 값 읽기
                    LocalDate date = rs.getDate("sale_date").toLocalDate();
                    String name = rs.getString("drink_name");
                    int price = rs.getInt("price");
                    int qty = rs.getInt("quantity");

                    // 쉼표로 구분된 문자열로 구성 후 Base64 인코딩
                    String line = String.format("%s,%s,%d,%d", date, name, price, qty);
                    String encoded = Base64.getEncoder().encodeToString(line.getBytes());

                    // 서버에 전송
                    out.write(encoded);
                    out.newLine();
                }
                out.flush(); // 모든 데이터 전송 완료
                System.out.println("DB 로그 전송 완료");

            }

        } catch (Exception e) {
            // 예외 처리
            System.err.println("DB 로그 전송 실패: " + e.getMessage());
        }
    }
}

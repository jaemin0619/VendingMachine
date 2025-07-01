// service/SalesLogger.java

package service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.control.Alert;
import util.DBManager;
import util.SimpleEncryptor;

/**
 * 매출 로그 관리 클래스
 * - 매출 기록 DB 저장 및 TCP 전송
 * - 일/월별 매출 집계 및 다이얼로그 표시 기능 포함
 */
public class SalesLogger {

    private static BufferedWriter tcpWriter = null;

    /**
     * TCP 스트림 설정 (외부에서 주입)
     */
    public static void setTcpWriter(BufferedWriter writer) {
        tcpWriter = writer;
    }

    /**
     * 매출 기록 처리 (DB 저장 + TCP 전송)
     */
    public static void logSale(String clientId, String drinkName, int price, int quantity) throws IOException {
        // 로그 문자열 생성 및 암호화
        String plainLine = String.format("%s,%s,%d,%d", LocalDate.now(), drinkName, price, quantity);
        String encryptedLine;

        try {
            encryptedLine = SimpleEncryptor.encrypt(plainLine);
            if (encryptedLine == null || encryptedLine.isEmpty()) {
                throw new IOException("암호화된 문자열이 비어 있음");
            }
        } catch (Exception e) {
            throw new IOException("암호화 중 오류 발생: " + e.getMessage(), e);
        }

        // DB 저장
        try (Connection conn = DBManager.getConnection()) {
            String sql = "INSERT INTO sales_log (client_id, sale_date, drink_name, price, quantity, encrypted_data) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, clientId); // 클라이언트 ID
                pstmt.setDate(2, Date.valueOf(LocalDate.now()));
                pstmt.setString(3, drinkName);
                pstmt.setInt(4, price);
                pstmt.setInt(5, quantity);
                pstmt.setString(6, encryptedLine);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("DB 저장 실패: " + e.getMessage());
        }

        //TCP 서버로 암호화된 로그 전송
        try {
            if (tcpWriter != null) {
                tcpWriter.write(encryptedLine);
                tcpWriter.newLine();
                tcpWriter.flush();
            }
        } catch (IOException e) {
            System.err.println("TCP 전송 실패: " + e.getMessage());
        }
    }

    /**
     * 음료별 매출 총합 다이얼로그 출력
     */
    public static void showSalesReportDialog() {
        try (Connection conn = DBManager.getConnection()) {
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SELECT drink_name, SUM(price * quantity) AS total FROM sales_log GROUP BY drink_name");

            StringBuilder sb = new StringBuilder("음료별 총 매출\n");
            while (rs.next()) {
                sb.append(rs.getString("drink_name"))
                        .append(" : ")
                        .append(rs.getInt("total"))
                        .append("원\n");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("매출 보고서");
            alert.setHeaderText(null);
            alert.setContentText(sb.toString());
            alert.showAndWait();

        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("오류");
            alert.setHeaderText(null);
            alert.setContentText("매출 보고서를 불러오는 데 실패했습니다.");
            alert.showAndWait();
        }
    }

    /**
     * 일별 매출 반환
     * @return Map<날짜, 총 매출>
     */
    public static Map<LocalDate, Integer> getDailySales() {
        Map<LocalDate, Integer> dailyMap = new HashMap<>();
        String sql = "SELECT sale_date, SUM(price * quantity) AS total FROM sales_log GROUP BY sale_date";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("sale_date").toLocalDate();
                int total = rs.getInt("total");
                dailyMap.put(date, total);
            }

        } catch (SQLException e) {
            System.err.println("일별 매출 조회 실패: " + e.getMessage());
        }

        return dailyMap;
    }

    /**
     * 월별 매출 반환
     * @return Map<"YYYY-MM", 총 매출>
     */
    public static Map<String, Integer> getMonthlySales() {
        Map<String, Integer> monthlyMap = new HashMap<>();
        String sql = "SELECT DATE_FORMAT(sale_date, '%Y-%m') AS month, SUM(price * quantity) AS total FROM sales_log GROUP BY month";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String month = rs.getString("month");
                int total = rs.getInt("total");
                monthlyMap.put(month, total);
            }

        } catch (SQLException e) {
            System.err.println("월별 매출 조회 실패: " + e.getMessage());
        }

        return monthlyMap;
    }
}

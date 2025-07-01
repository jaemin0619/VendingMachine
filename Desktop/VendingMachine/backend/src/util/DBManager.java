// util/DBManager.java
package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * MySQL 데이터베이스 연결 및 매출 기록 관리 유틸리티
 */
public class DBManager {

    // 🔧 DB 연결 정보
    private static final String URL = "jdbc:mysql://localhost:3306/vending_db";  // DB명: vending_db
    private static final String USER = "root";         // 사용자명
    private static final String PASSWORD = "1234";     // 비밀번호

    /**
     * DB 연결을 반환하는 메서드
     * @return Connection 객체
     * @throws SQLException 연결 실패 시 예외 발생
     */
    public static Connection getConnection() throws SQLException {
        try {
            // JDBC 드라이버 로딩 (MySQL 8.x 기준)
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  // 드라이버 로딩 실패
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 매출 정보를 DB에 삽입하는 메서드
     * @param clientId 자판기 ID
     * @param drinkName 음료 이름
     * @param price 단가
     * @param quantity 수량
     */
    public static void insertSale(String clientId, String drinkName, int price, int quantity) {
        String sql = "INSERT INTO sales_log (client_id, sale_date, drink_name, price, quantity) " +
                "VALUES (?, CURDATE(), ?, ?, ?)";

        try (Connection conn = getConnection();                      // 연결 획득
             PreparedStatement stmt = conn.prepareStatement(sql)) { // 쿼리 준비

            stmt.setString(1, clientId);
            stmt.setString(2, drinkName);
            stmt.setInt(3, price);
            stmt.setInt(4, quantity);

            stmt.executeUpdate();  // INSERT 실행

        } catch (SQLException e) {
            e.printStackTrace(); // SQL 예외 출력
        }
    }
}

package dao;

import model.Drink;
import model.DrinkInventory;
import util.DBManager;

import java.sql.*;
import java.util.LinkedList;

public class DrinkInventoryDAO {

    // 재고 정보를 DB에 저장하는 메서드
    public static void saveInventoryToDB(DrinkInventory inventory) {
        // 기존에 있으면 UPDATE, 없으면 INSERT (MySQL REPLACE INTO 구문)
        String sql = "REPLACE INTO drink_inventory (id, name, price, stock) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBManager.getConnection();             // DB 연결
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // 쿼리 준비

            LinkedList<Drink> drinks = inventory.getDrinks(); // 현재 메모리 상의 음료 리스트 가져오기

            for (int i = 0; i < drinks.size(); i++) {
                Drink d = drinks.get(i);

                pstmt.setInt(1, i + 1);            // id (1부터 시작)
                pstmt.setString(2, d.getName());   // name
                pstmt.setInt(3, d.getPrice());     // price
                pstmt.setInt(4, d.getStock());     // stock

                pstmt.executeUpdate();             // 쿼리 실행
            }

        } catch (SQLException e) {
            e.printStackTrace(); // 저장 실패 시 콘솔에 예외 출력
        }
    }

    // DB에서 재고 정보를 불러오는 메서드
    public static DrinkInventory loadInventoryFromDB() {
        DrinkInventory inventory = new DrinkInventory();  // 빈 DrinkInventory 생성

        String sql = "SELECT * FROM drink_inventory ORDER BY id ASC"; // ID순 정렬해서 읽기

        try (Connection conn = DBManager.getConnection();  // DB 연결
             Statement stmt = conn.createStatement();      // 일반 Statement 사용
             ResultSet rs = stmt.executeQuery(sql)) {      // 쿼리 실행

            while (rs.next()) {  // 결과가 있을 때까지 반복
                String name = rs.getString("name");       // 이름
                int price = rs.getInt("price");           // 가격
                int stock = rs.getInt("stock");           // 재고

                inventory.getDrinks().add(new Drink(name, price, stock)); // 리스트에 추가
            }

        } catch (SQLException e) {
            e.printStackTrace(); // 불러오기 실패 시 콘솔에 예외 출력
        }

        return inventory; // 불러온 재고 객체 반환
    }
}

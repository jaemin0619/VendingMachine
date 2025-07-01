// Drink.java
package model;

import java.util.LinkedList;

/**
 * Drink 클래스는 하나의 음료 정보를 나타냅니다.
 * 이름, 가격, 재고(LinkedList 사용) 정보를 포함하며,
 * 재고 관리 기능을 제공합니다.
 */
public class Drink {
    private String name; // 음료 이름
    private int price;   // 음료 가격
    private LinkedList<Object> stockList; // 음료 재고를 관리하는 리스트

    /**
     * Drink 생성자: 음료명, 가격, 초기 재고 개수를 받아 초기화
     */
    public Drink(String name, int price, int stockCount) {
        this.name = name;
        this.price = price;
        stockList = new LinkedList<>();
        for (int i = 0; i < stockCount; i++) {
            stockList.add(new Object()); // 단순 재고 개수 표현용 객체 추가
        }
    }

    // Getter & Setter
    public String getName() { return name; }
    public int getPrice() { return price; }
    public void setName(String name) { this.name = name; }
    public void setPrice(int price) { this.price = price; }

    /**
     * 현재 재고 개수 반환
     */
    public int getStock() {
        return stockList.size();
    }

    /**
     * 재고가 모두 소진되었는지 확인
     */
    public boolean isSoldOut() {
        return stockList.isEmpty();
    }

    /**
     * 음료 하나 판매 시 재고 감소
     */
    public void reduceStock() {
        if (!stockList.isEmpty()) {
            stockList.removeFirst();
        }
    }

    /**
     * 지정한 수량만큼 재고 보충
     */
    public void restock(int amount) {
        for (int i = 0; i < amount; i++) {
            stockList.add(new Object());
        }
    }
}

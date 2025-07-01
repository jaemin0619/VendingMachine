// DrinkInventory.java
package model;

import dao.DrinkInventoryDAO;
import java.util.LinkedList;

/**
 * DrinkInventory 클래스는 자판기에 등록된 모든 음료의 목록을 관리한다.
 * 내부적으로 LinkedList<Drink> 자료구조를 사용하여 음료 정보를 보관하며,
 * 음료 조회 및 리스트 반환 기능을 제공한다.
 */
public class DrinkInventory {

    private LinkedList<Drink> drinks; // 음료 객체 리스트

    /**
     * 생성자: 빈 음료 리스트로 초기화
     */
    public DrinkInventory() {
        this.drinks = new LinkedList<>();
    }

    /**
     * 전체 음료 리스트 반환
     */
    public LinkedList<Drink> getDrinks() {
        return drinks;
    }

    /**
     * 지정 인덱스의 음료 반환
     * @param index 조회할 음료 인덱스
     * @return Drink 객체
     */
    public Drink getDrink(int index) {
        return drinks.get(index);
    }
}

// PurchaseHistory.java
package model;

import java.util.Stack;

/**
 * PurchaseHistory 클래스는 구매한 음료 기록을 스택 구조로 관리합니다.
 * - 최근 구매 조회
 * - 전체 구매 이력 출력
 * 등의 기능을 제공합니다.
 */
public class PurchaseHistory {

    private Stack<String> history = new Stack<>(); // 구매 이력 저장용 스택

    /**
     * 구매 이력에 새로운 음료 추가
     * @param drinkName 구매한 음료 이름
     */
    public void add(String drinkName) {
        history.push(drinkName);
    }

    /**
     * 가장 최근에서 n번째 구매 내역 조회
     * @param n 최근 n번째 항목
     * @return 음료 이름 또는 '기록 없음'
     */
    public String getRecent(int n) {
        if (history.size() < n) return "기록 없음";
        return history.get(history.size() - n);
    }

    /**
     * 가장 최근 구매한 음료 조회
     * @return 음료 이름 또는 '기록 없음'
     */
    public String getLatest() {
        if (history.isEmpty()) return "기록 없음";
        return history.peek();
    }

    /**
     * 전체 구매 이력을 역순(최신순)으로 출력
     * @return 포맷된 이력 문자열
     */
    public String getAll() {
        StringBuilder sb = new StringBuilder("📦 최근 구매 기록:\n");
        int order = 1;
        for (int i = history.size() - 1; i >= 0; i--) {
            sb.append(order++).append(". ").append(history.get(i)).append("\n");
        }
        return sb.toString();
    }

    /**
     * toString 오버라이드: getAll() 결과 반환
     */
    @Override
    public String toString() {
        return getAll();  // 이 부분이 핵심!
    }
}

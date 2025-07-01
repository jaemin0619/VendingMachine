// service/MoneyManager.java

package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 투입금/거스름돈/수금 등을 관리하는 클래스
 * - 사용자 투입 금액, 잔액 계산, 거스름돈 반환, 수금 기능 포함
 */
public class MoneyManager {

    // 자판기에서 사용하는 화폐 단위
    private final int[] denominations = {1000, 500, 100, 50, 10};

    // 각 화폐 단위별 보유 개수
    private final Map<Integer, Integer> coinStock = new HashMap<>();

    // 사용자가 투입한 금액을 기록
    private final List<Integer> insertedMoney = new ArrayList<>();

    private int currentBalance = 0;         // 현재 투입된 총 금액
    private int totalBillsInserted = 0;     // 지폐 누적 합계 (1000원만)

    public MoneyManager() {
        for (int d : denominations) {
            coinStock.put(d, 10); // 초기 각 화폐 10개 보유
        }
    }

    /**
     * 돈 투입 처리
     * @param amount 투입 금액
     * @return 투입 성공 여부 (지폐 한도 5000원, 총 7000원 제한)
     */
    public boolean insertMoney(int amount) {
        if (currentBalance + amount > 7000) return false;

        if (amount == 1000) {
            if (totalBillsInserted + amount > 5000) return false;
            totalBillsInserted += 1000;
        }

        currentBalance += amount;
        insertedMoney.add(amount); // 입력 내역 기록
        return true;
    }

    /**
     * 현재 잔액 반환
     */
    public int getBalance() {
        return currentBalance;
    }

    /**
     * 물건 구매 시 금액 차감
     */
    public void spendMoney(int amount) {
        currentBalance -= amount;
        insertedMoney.clear(); // 사용 후 투입 목록 초기화
    }

    /**
     * 잔액, 지폐 누적 초기화
     */
    public void resetBalance() {
        currentBalance = 0;
        totalBillsInserted = 0;
        insertedMoney.clear();
    }

    /**
     * 투입한 화폐를 그대로 반환
     */
    public List<Integer> returnInsertedMoney() {
        List<Integer> refund = new ArrayList<>(insertedMoney);
        resetBalance();
        return refund;
    }

    /**
     * 가능한 거스름돈 계산 및 반환
     * @return 단위별 반환 수량 (부족 시 null)
     */
    public Map<Integer, Integer> returnChange() {
        Map<Integer, Integer> change = new HashMap<>();
        int remaining = currentBalance;

        for (int denom : denominations) {
            int stock = coinStock.getOrDefault(denom, 0);
            int count = 0;

            while (remaining >= denom && stock > 0) {
                remaining -= denom;
                stock--;
                count++;
            }

            if (count > 0) {
                change.put(denom, count);
                coinStock.put(denom, stock);
            }
        }

        if (remaining > 0) {
            return null; // 거스름돈 부족
        } else {
            resetBalance();
            return change;
        }
    }

    /**
     * 거스름돈 반환 가능 여부 확인
     */
    public boolean isChangeAvailable() {
        return returnChange() != null;
    }

    /**
     * 현재 보유 중인 동전 현황 반환
     */
    public Map<Integer, Integer> getCoinStock() {
        return coinStock;
    }

    /**
     * 보유 중인 모든 동전의 총액 반환
     */
    public int getTotalStoredMoney() {
        int total = 0;
        for (Map.Entry<Integer, Integer> entry : coinStock.entrySet()) {
            total += entry.getKey() * entry.getValue();
        }
        return total;
    }

    /**
     * 동전 재고 추가 (보충)
     */
    public void addCoinToStock(int amount) {
        if (coinStock.containsKey(amount)) {
            coinStock.put(amount, coinStock.get(amount) + 1);
        }
    }

    /**
     * 수금 처리 - 최소 보유 개수 제외하고 모두 수거
     * @param minimumPerDenomination 각 화폐 단위 최소 보유 수량
     * @return 수금된 총액
     */
    public int collectCoins(int minimumPerDenomination) {
        int collected = 0;
        for (int denom : denominations) {
            int current = coinStock.getOrDefault(denom, 0);
            if (current > minimumPerDenomination) {
                int collect = current - minimumPerDenomination;
                collected += denom * collect;
                coinStock.put(denom, minimumPerDenomination);
            }
        }
        return collected;
    }

    /**
     * 투입된 화폐 목록 반환 (UI 출력용)
     */
    public List<Integer> getInsertedMoney() {
        return new ArrayList<>(insertedMoney);
    }
}

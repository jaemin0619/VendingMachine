// Structure/DrinkBST.java
package Structure;

import model.Drink;

/**
 * 이진 탐색 트리(Binary Search Tree)를 이용한 음료 검색 구조
 * - 음료 이름 기준 정렬 및 탐색
 */
public class DrinkBST {

    // 내부 노드 클래스
    private class Node {
        Drink drink;
        Node left, right;

        Node(Drink drink) {
            this.drink = drink;
        }
    }

    private Node root;  // 트리의 루트 노드

    /**
     * 음료 삽입 (이름 기준 이진탐색 삽입)
     */
    public void insert(Drink drink) {
        root = insertRec(root, drink);
    }

    // 재귀적으로 삽입 처리
    private Node insertRec(Node root, Drink drink) {
        if (root == null) return new Node(drink);
        if (drink.getName().compareTo(root.drink.getName()) < 0)
            root.left = insertRec(root.left, drink);
        else
            root.right = insertRec(root.right, drink);
        return root;
    }

    /**
     * 음료 이름으로 검색
     * @param name 검색할 음료 이름
     * @return Drink 객체 반환 (없으면 null)
     */
    public Drink search(String name) {
        return searchRec(root, name);
    }

    // 재귀적으로 검색
    private Drink searchRec(Node root, String name) {
        if (root == null) return null;
        int cmp = name.compareTo(root.drink.getName());
        if (cmp == 0) return root.drink;
        return cmp < 0 ? searchRec(root.left, name) : searchRec(root.right, name);
    }

    /**
     * 중위 순회로 음료 이름/가격 출력 (이름순 정렬된 출력)
     */
    public void printInOrder() {
        inOrderRec(root);
    }

    private void inOrderRec(Node node) {
        if (node != null) {
            inOrderRec(node.left);
            System.out.println(node.drink.getName() + ": " + node.drink.getPrice() + "원");
            inOrderRec(node.right);
        }
    }
}

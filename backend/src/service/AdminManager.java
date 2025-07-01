// service/AdminManager.java
package service;

import util.EncryptionUtil;

/**
 * 관리자 인증 및 비밀번호 관리 클래스
 * - 비밀번호 암호화, 검증, 변경 기능 제공
 */
public class AdminManager {

    private String encryptedPassword; // 암호화된 비밀번호 저장

    /**
     * 생성자 - 초기 비밀번호를 암호화하여 설정
     */
    public AdminManager() {
        this.encryptedPassword = EncryptionUtil.encryptSHA256("admin123!");
    }

    /**
     * 입력된 비밀번호와 기존 비밀번호가 일치하는지 검증
     * @param input 입력된 평문 비밀번호
     * @return 일치 여부
     */
    public boolean checkPassword(String input) {
        return EncryptionUtil.encryptSHA256(input).equals(encryptedPassword);
    }

    /**
     * 새로운 비밀번호의 유효성 검사
     * - 8자 이상
     * - 숫자 포함
     * - 특수문자 포함
     * @param password 검사할 비밀번호
     * @return 유효한 비밀번호 여부
     */
    public boolean isValidPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*\\d.*") && // 숫자 포함
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"); // 특수문자 포함
    }

    /**
     * 비밀번호 변경
     * @param newPassword 새로운 평문 비밀번호
     */
    public void changePassword(String newPassword) {
        this.encryptedPassword = EncryptionUtil.encryptSHA256(newPassword);
    }
}

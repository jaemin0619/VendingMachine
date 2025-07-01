// util/SimpleEncryptor.java
package util;

import java.util.Base64;

/**
 * 간단한 문자열 암호화/복호화를 위한 유틸리티 클래스
 * - Base64 인코딩을 사용 (보안보다는 가독성과 간단한 인코딩 목적)
 */
public class SimpleEncryptor {

    /**
     * 입력 문자열을 Base64 방식으로 인코딩 (암호화 유사 동작)
     * @param input 원본 문자열
     * @return Base64로 인코딩된 문자열
     */
    public static String encrypt(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    /**
     * Base64로 인코딩된 문자열을 디코딩하여 원래 문자열로 복원
     * @param encrypted Base64 문자열
     * @return 복호화된 원본 문자열 (잘못된 입력 시 빈 문자열 반환)
     */
    public static String decrypt(String encrypted) {
        try {
            return new String(Base64.getDecoder().decode(encrypted));
        } catch (IllegalArgumentException e) {
            // 디코딩 실패 시 (유효하지 않은 Base64 형식) 빈 문자열 반환
            return "";
        }
    }
}

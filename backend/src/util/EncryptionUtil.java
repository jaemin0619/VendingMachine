// util/EncryptionUtil.java
package util;

import java.security.MessageDigest;

/**
 * 문자열을 SHA-256 방식으로 암호화하는 유틸리티 클래스
 */
public class EncryptionUtil {

    /**
     * 주어진 문자열을 SHA-256 해시로 암호화
     * @param input 평문 문자열 (ex: 비밀번호)
     * @return SHA-256 해시값 (16진수 문자열)
     */
    public static String encryptSHA256(String input) {
        try {
            // SHA-256 해시 함수 객체 생성
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 문자열을 바이트 배열로 변환한 후 해시 계산
            byte[] hash = md.digest(input.getBytes());

            // 바이트 배열을 16진수 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b); // 1바이트씩 처리
                if (hex.length() == 1) hexString.append('0'); // 자리수 보정
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("SHA-256 암호화 실패", e);
        }
    }
}

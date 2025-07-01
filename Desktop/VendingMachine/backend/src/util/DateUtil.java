// util/DateUtil.java
package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 관련 유틸리티 클래스
 * - 오늘 날짜 및 월을 포맷된 문자열로 반환
 */
public class DateUtil {

    /**
     * 오늘 날짜를 "yyyy-MM-dd" 형식으로 반환
     * @return 오늘 날짜 문자열
     */
    public static String getToday() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    /**
     * 이번 달을 "yyyy-MM" 형식으로 반환
     * @return 이번 달 문자열
     */
    public static String getMonth() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}

package com.hancomins.logexpress.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    /**
     * 문자열을 구분자로 분리하여 배열로 반환합니다.
     * 따옴표나 작은따옴표로 감싸진 구분 문자나 이스케이프 문자 뒤의 구분 문자는 무시합니다.
     *
     * @param input      입력 문자열
     * @param separator  구분 문자
     * @param keepQuotes 따옴표와 작은 따옴표를 결과에 포함할지 여부
     * @return           분리된 문자열 배열
     */
    public static String[] splitStringWithSeparator(String input, char separator, boolean keepQuotes) {
        if (input == null || input.isEmpty()) {
            return new String[0];
        }

        List<String> result = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inDoubleQuotes = false;
        boolean inSingleQuotes = false;
        boolean escape = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (escape) {
                // 이스케이프 문자가 있으면 다음 문자 그대로 추가
                current.setLength(current.length() - 1);  // 이스케이프 문자 제거
                current.append(c);
                escape = false;
            } else if (c == '\\') {
                // 이스케이프 문자 시작
                escape = true;
                current.append(c);  // 이스케이프 문자도 포함
            } else if (c == '"' && !inSingleQuotes) {
                // 더블쿼트 안에 있는 경우 토글
                inDoubleQuotes = !inDoubleQuotes;
                if (keepQuotes) {
                    current.append(c);
                }
            } else if (c == '\'' && !inDoubleQuotes) {
                // 싱글쿼트 안에 있는 경우 토글
                inSingleQuotes = !inSingleQuotes;
                if (keepQuotes) {
                    current.append(c);
                }
            } else if (c == separator && !inDoubleQuotes && !inSingleQuotes) {
                // 구분자를 발견하고 따옴표 안이 아닌 경우
                result.add(current.toString());
                current.setLength(0); // StringBuilder 초기화
            } else {
                // 일반 문자 추가
                current.append(c);
            }
        }

        // 마지막 부분 추가
        result.add(current.toString());

        return result.toArray(new String[0]);
    }

    public static boolean isNullOrEmptyAfterTrim(String srt) {
        return trimToNonNull(srt).isEmpty();
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String trimToNonNull(String str) {
        return str == null ? "" : str.trim();
    }
}

package com.mediforme.mediforme.search.support;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * 약 이름 정규화 유틸
 */
public final class TextNormalizer {

    private static final Pattern PAREN = Pattern.compile("\\([^)]*\\)");
    private static final Pattern DOSAGE = Pattern.compile(
        "(?i)\\d+(?:\\.\\d+)?\\s*(?:mg|g|ml|mcg|ug|밀리그램|그램|밀리리터)"
    );
    private static final Pattern NON_ALNUM_KOR = Pattern.compile("[^가-힣a-z0-9]");

    private TextNormalizer() {}

    /**
     * 매칭용 정규화. 괄호·용량·공백·대소문자·특수문자 제거
     */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String t = Normalizer.normalize(raw.trim(), Normalizer.Form.NFKC);
        t = PAREN.matcher(t).replaceAll("");
        t = DOSAGE.matcher(t).replaceAll("");
        t = t.toLowerCase();
        t = NON_ALNUM_KOR.matcher(t).replaceAll("");
        return t;
    }
}

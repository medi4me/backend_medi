package com.mediforme.mediforme.search.support;

import org.springframework.stereotype.Component;

/**
 * Jaro-Winkler 유사도 (0.0~1.0)
 * 공통 접두사에 가산점 — 약 이름 OCR 오타 매칭에 사용
 */
@Component
public class JaroWinklerSimilarity {

    private static final double PREFIX_SCALE = 0.1;
    private static final int MAX_PREFIX = 4;

    public double score(String s1, String s2) {
        if (s1 == null || s2 == null) return 0.0;
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        double jaro = jaro(s1, s2);
        if (jaro == 0.0) return 0.0;

        int prefix = 0;
        int max = Math.min(MAX_PREFIX, Math.min(s1.length(), s2.length()));
        for (int i = 0; i < max; i++) {
            if (s1.charAt(i) == s2.charAt(i)) prefix++;
            else break;
        }
        return jaro + prefix * PREFIX_SCALE * (1 - jaro);
    }

    private double jaro(String s1, String s2) {
        int matchDistance = Math.max(s1.length(), s2.length()) / 2 - 1;
        if (matchDistance < 0) matchDistance = 0;

        boolean[] s1Matches = new boolean[s1.length()];
        boolean[] s2Matches = new boolean[s2.length()];
        int matches = 0;

        for (int i = 0; i < s1.length(); i++) {
            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, s2.length());
            for (int j = start; j < end; j++) {
                if (s2Matches[j]) continue;
                if (s1.charAt(i) != s2.charAt(j)) continue;
                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }
        if (matches == 0) return 0.0;

        int transpositions = 0;
        int k = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (!s1Matches[i]) continue;
            while (!s2Matches[k]) k++;
            if (s1.charAt(i) != s2.charAt(k)) transpositions++;
            k++;
        }

        double m = matches;
        return (m / s1.length() + m / s2.length() + (m - transpositions / 2.0) / m) / 3.0;
    }
}

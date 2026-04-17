package com.mediforme.mediforme.search.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JaroWinklerSimilarityTest {

    private final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    @Test
    @DisplayName("완전 일치는 1.0")
    void exactMatch() {
        assertThat(similarity.score("타이레놀", "타이레놀")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("null/빈 문자열은 0.0")
    void nullOrEmpty() {
        assertThat(similarity.score(null, "x")).isEqualTo(0.0);
        assertThat(similarity.score("x", null)).isEqualTo(0.0);
        assertThat(similarity.score("", "x")).isEqualTo(0.0);
        assertThat(similarity.score("x", "")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("접두사 일치는 가산점 (Winkler)")
    void prefixBoost() {
        double withPrefix = similarity.score("타이레놀정", "타이레놀");
        double withoutPrefix = similarity.score("놀정타이레", "타이레놀");
        assertThat(withPrefix).isGreaterThan(withoutPrefix);
    }

    @Test
    @DisplayName("완전 불일치는 낮은 점수")
    void completelyDifferent() {
        double s = similarity.score("타이레놀", "아스피린");
        assertThat(s).isLessThan(0.5);
    }

    @Test
    @DisplayName("한 글자 차이는 높은 점수 (OCR 오타 시나리오)")
    void nearMatchHighScore() {
        double s = similarity.score("타이레놀", "타이래놀");
        assertThat(s).isGreaterThan(0.8);
    }
}

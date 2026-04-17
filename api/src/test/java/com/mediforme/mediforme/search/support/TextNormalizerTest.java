package com.mediforme.mediforme.search.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizerTest {

    @Test
    @DisplayName("null/빈 문자열은 빈 문자열 반환")
    void nullOrBlank() {
        assertThat(TextNormalizer.normalize(null)).isEmpty();
        assertThat(TextNormalizer.normalize("")).isEmpty();
        assertThat(TextNormalizer.normalize("   ")).isEmpty();
    }

    @Test
    @DisplayName("공백·특수문자 제거")
    void stripsWhitespaceAndSymbols() {
        assertThat(TextNormalizer.normalize("타이레놀 500")).isEqualTo("타이레놀500");
        assertThat(TextNormalizer.normalize("Ty-le-nol!")).isEqualTo("tylenol");
    }

    @Test
    @DisplayName("용량 단위 제거 (mg, ml, 밀리그램 등)")
    void stripsDosage() {
        assertThat(TextNormalizer.normalize("타이레놀 500mg")).isEqualTo("타이레놀");
        assertThat(TextNormalizer.normalize("아스피린 100 mg")).isEqualTo("아스피린");
        assertThat(TextNormalizer.normalize("Tylenol 500MG")).isEqualTo("tylenol");
    }

    @Test
    @DisplayName("괄호와 괄호 안 내용 제거")
    void stripsParens() {
        assertThat(TextNormalizer.normalize("타이레놀(아세트아미노펜)"))
            .isEqualTo("타이레놀");
    }

    @Test
    @DisplayName("대소문자 통일")
    void lowercases() {
        assertThat(TextNormalizer.normalize("Tylenol")).isEqualTo("tylenol");
        assertThat(TextNormalizer.normalize("TYLENOL")).isEqualTo("tylenol");
    }

    @Test
    @DisplayName("영문+숫자+한글 혼합")
    void mixedAlphanumeric() {
        assertThat(TextNormalizer.normalize("Tylenol 서방정 500mg"))
            .isEqualTo("tylenol서방정");
    }
}

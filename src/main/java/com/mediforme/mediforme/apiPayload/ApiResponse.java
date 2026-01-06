package com.mediforme.mediforme.apiPayload;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.mediforme.mediforme.apiPayload.code.BaseCode;
import com.mediforme.mediforme.apiPayload.code.status.SuccessStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"success", "code", "message", "result"})
public class ApiResponse<T> {

    @JsonProperty("success")
    private final boolean success;            // 성공 여부 (true/false)

    private final String code;                  // 응답 코드
    private final String message;               // 응답 메시지

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;                           // 성공 시 payload, 실패 시 null


    /**
     * 성공 응답 (200 OK)
     */
    public static <T> ApiResponse<T> onSuccess(T result) {
        return new ApiResponse<>(
            true,
            SuccessStatus._OK.getCode(),
            SuccessStatus._OK.getMessage(),
            result
        );
    }

    /**
     * 성공 응답 (SuccessStatus 이외의 성공 코드/메시지 필요 시)
     * - BaseCode가 성공 코드라는 전제에서만 사용
     */
    public static <T> ApiResponse<T> of(BaseCode code, T result) {
        return new ApiResponse<>(
            true,
            code.getCode(),
            code.getMessage(),
            result
        );
    }


    /**
     * 실패 응답 (4xx/5xx)
     * - 실패 응답에서는 result는 null로 고정하는 것이 실무에서 안전함
     */
    public static <T> ApiResponse<T> onFailure(String code, String message) {
        return new ApiResponse<>(
            false,
            code,
            message,
            null
        );
    }
}

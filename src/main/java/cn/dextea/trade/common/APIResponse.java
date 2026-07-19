package cn.dextea.trade.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class APIResponse<T> {

    private int code;

    private String message;

    private T data;

    public static <T> APIResponse<T> success(T data) {
        return APIResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> success() {
        return success(null);
    }

    public static <T> APIResponse<T> error(int code, String message) {
        return APIResponse.<T>builder()
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}

package cn.dextea.trade.common.api;
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
                .message("成功")
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
    public static <T> APIResponse<T> error(int code, String message, T data) {
        return APIResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .build();
    }
}

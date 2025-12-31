package com.pinkpig.payment.types.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Response<T> implements Serializable {

    private String code;
    private String info;
    private T data;

    public static <T> Response<T> success(T data) {
        return new Response<>("0000", "调用成功", data);
    }

    public static <T> Response<T> fail(String code, String info) {
        return new Response<>(code, info, null);
    }
}
package com.itheima.mp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {

    NORMAL(1,"正常"),
    FREEZE(2,"冻结");

    @EnumValue
    private final Integer code;
    @JsonValue
    private final String message;

    UserStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

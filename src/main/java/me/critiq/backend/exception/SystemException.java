package me.critiq.backend.exception;

import lombok.Data;
import me.critiq.backend.enums.ResponseStatusEnum;

@Data
public class SystemException extends RuntimeException {
    private Integer code;
    private String message;

    public SystemException(ResponseStatusEnum responseStatusEnum) {
        super(responseStatusEnum.getMessage());
        this.code = responseStatusEnum.getCode();
        this.message = responseStatusEnum.getMessage();
    }
}

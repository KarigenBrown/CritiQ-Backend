package me.critiq.backend.handler.exeception;

import lombok.extern.slf4j.Slf4j;
import me.critiq.backend.exception.SystemException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.internalServerError()
                .body("服务器异常");
    }

    // @ExceptionHandler(SystemException.class)
    public ResponseEntity<String> handleSystemException(SystemException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.of(Optional.of(ex.getMessage()));
    }
}

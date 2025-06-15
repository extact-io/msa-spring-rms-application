package io.extact.msa.spring.rms.webapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import io.extact.msa.spring.platform.fw.exception.message.SimpleErrorMessage;
import io.extact.msa.spring.platform.fw.interfaces.webapi.ExceptionHandled;
import io.extact.msa.spring.rms.application.universal.LoginFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * rms-application固有例外に対する例外ハンドラー。
 */
@RestControllerAdvice(annotations = ExceptionHandled.class)
@RequiredArgsConstructor
@Slf4j
public class RmsRestControllerExceptionHandler {

    private static final String RMS_EXCEPTION_HEAD = "rms-exception";


    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<SimpleErrorMessage> handleLoginFailedException(LoginFailedException e, WebRequest req) {

        log.warn("exception occured. message={}", e.getMessage());

        SimpleErrorMessage message = new SimpleErrorMessage(e.getClass().getSimpleName(), e.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header(RMS_EXCEPTION_HEAD, e.getClass().getSimpleName())
                .body(message);
    }
}

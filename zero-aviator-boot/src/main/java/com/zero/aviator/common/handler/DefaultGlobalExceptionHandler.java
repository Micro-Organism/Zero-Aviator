package com.zero.aviator.common.handler;

import com.alibaba.fastjson2.JSON;
import com.zero.aviator.common.enums.ErrorCode;
import com.zero.aviator.common.exception.HttpResult;
import com.zero.aviator.common.exception.UserFriendlyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;


@Slf4j
@Configuration
@ControllerAdvice
public class DefaultGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        HttpResult httpResult = HttpResult.failure(status.is5xxServerError() ? ErrorCode.serverError.getDesc() : ErrorCode.paramError.getDesc());
        log.error("handleException, ex caught, contextPath={}, httpResult={}, ex.msg={}", request.getContextPath(), JSON.toJSONString(httpResult), ex.getMessage());
        return super.handleExceptionInternal(ex, httpResult, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity handleException(HttpServletRequest request, Exception ex) {
        boolean is5xxServerError;
        HttpStatus httpStatus;
        HttpResult httpResult;
        if (ex instanceof UserFriendlyException) {
            UserFriendlyException userFriendlyException = (UserFriendlyException) ex;
            is5xxServerError = userFriendlyException.getHttpStatusCode() >= 500;
            httpStatus = HttpStatus.valueOf(userFriendlyException.getHttpStatusCode());
            httpResult = HttpResult.failure(userFriendlyException.getErrorCode(), userFriendlyException.getMessage());
        } else if (ex instanceof IllegalArgumentException) {
            // Spring assertions are used in parameter judgment. requireTrue will throw an IllegalArgumentException. The client cannot handle 5xx exceptions, so 200 is still returned.
            httpStatus = HttpStatus.OK;
            is5xxServerError = false;
            httpResult = HttpResult.failure("Parameter verification error or data abnormality!");
        } else {
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            is5xxServerError = true;
            httpResult = HttpResult.failure(ErrorCode.serverError.getDesc());
        }
        if (is5xxServerError) {
            log.error("handleException, ex caught, uri={}, httpResult={}", request.getRequestURI(), JSON.toJSONString(httpResult), ex);
        } else {
            log.error("handleException, ex caught, uri={}, httpResult={}, ex.msg={}", request.getRequestURI(), JSON.toJSONString(httpResult), ex.getMessage());
        }
        return new ResponseEntity<>(httpResult, httpStatus);
    }

}

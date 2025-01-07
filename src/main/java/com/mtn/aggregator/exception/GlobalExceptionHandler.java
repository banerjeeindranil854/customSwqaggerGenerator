package com.mtn.aggregator.exception;

import brave.baggage.BaggageField;
import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import com.mtn.aggregator.helpers.logging.LoggingHelper;
import com.mtn.madapi.commons.models.error.APIError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.WebUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final BaggageField transactionIdTraceField;
    private final BaggageField sequenceNoTraceField;
    private final LoggingHelper loggingHelper;

    @ExceptionHandler({
            IllegalArgumentException.class,
            CanonicalErrorCodeException.class,
            MethodArgumentNotValidException.class,
    })
    public final ResponseEntity<APIError> handleException(Exception ex, WebRequest request) {

        log.info(
                "Exception thrown :: {}, Message :: {}", ex.getClass().getSimpleName(), ex.getLocalizedMessage()
        );

        var headers = new HttpHeaders();

        if (ex instanceof IllegalArgumentException) {
            var illegalArgumentException = (IllegalArgumentException) ex;

            return handleIllegalArgumentException(illegalArgumentException, headers, request);
        } else if (ex instanceof CanonicalErrorCodeException) {
            var dataShareResponseCodeException = (CanonicalErrorCodeException) ex;

            return handleDataShareException(
                    dataShareResponseCodeException,
                    dataShareResponseCodeException.getHttpStatus(),
                    headers,
                    request);
        }else if (ex instanceof MethodArgumentNotValidException) {

            var methodArgumentNotValidException = (MethodArgumentNotValidException) ex;
            return handleMethodArgumentNotValidException(
                    methodArgumentNotValidException,
                    headers,
                    request);
        }  else {
            return handleExceptionInternal(ex, null, headers, HttpStatus.INTERNAL_SERVER_ERROR, request);
        }
    }

    private ResponseEntity<APIError> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpHeaders headers, WebRequest request) {
        var apiError = new APIError();
        apiError.setTimestamp(getLocalZonedDateTime());
        apiError.setStatusCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
        apiError.setStatusMessage(HttpStatus.NOT_FOUND.getReasonPhrase());
        apiError.setSupportMessage(HttpStatus.NOT_FOUND.name());
        apiError.setPath(request.getContextPath());

        return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
    }


    private ResponseEntity<APIError> handleDataShareException(
            CanonicalErrorCodeException ex,
            HttpStatus httpStatus,
            HttpHeaders headers,
            WebRequest request) {
        var apiError = new APIError();
        apiError.setTimestamp(getLocalZonedDateTime());
        apiError.setStatusCode(Objects.isNull(ex.getApiError())?ex.getCanonicalStatusCode():ex.getApiError().getStatusCode());
        apiError.setStatusMessage(Objects.isNull(ex.getApiError())?ex.getMessage():ex.getApiError().getStatusMessage());
        apiError.setSupportMessage(Objects.isNull(ex.getApiError())?ex.getHttpStatus().name():ex.getApiError().getSupportMessage());
        apiError.setPath(ex.getPath());
        apiError.setSequenceNo(Objects.isNull(ex.getApiError())?sequenceNoTraceField.getValue():ex.getApiError().getSequenceNo());
        apiError.setTransactionId(Objects.isNull(ex.getApiError())?ex.getTransactionId():ex.getApiError().getTransactionId());

        return handleExceptionInternal(ex, apiError, headers, httpStatus, request);
    }

    private ResponseEntity<APIError> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            WebRequest request) {
        var apiError = new APIError();
        apiError.setTimestamp(getLocalZonedDateTime());
        apiError.setStatusCode(CanonicalErrorCode.BAD_REQUEST.getCanonicalCode());
        apiError.setStatusMessage(HttpStatus.BAD_REQUEST.getReasonPhrase());
        apiError.setSupportMessage(getErrorMessage(ex));
        apiError.setPath(request.getContextPath());

        return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
    }

    protected ResponseEntity<APIError> handleExceptionInternal(
            Exception ex, APIError body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, SCOPE_REQUEST);
        }

        body.setTransactionId(transactionIdTraceField.getValue());
        body.setSequenceNo(sequenceNoTraceField.getValue());

        loggingHelper.responseObject(log, body);
        return new ResponseEntity<>(body, headers, status);
    }

    private String getLocalZonedDateTime() {
        var dateTime = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    private  String getErrorMessage (MethodArgumentNotValidException methodArgumentNotValid){
        for(FieldError fieldError : methodArgumentNotValid.getBindingResult().getFieldErrors()) {
            if (!fieldError.getField().isEmpty()) {
                return fieldError.getField().concat(" field ").concat(Objects.requireNonNull(fieldError.getDefaultMessage()));
            }
        }
        return null;
    }
}

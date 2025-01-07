package com.mtn.aggregator.exception;

import com.mtn.aggregator.enums.response.CanonicalErrorCode;
import com.mtn.madapi.commons.models.error.APIError;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;


@Slf4j
@Data
@Builder
public class CanonicalErrorCodeException extends RuntimeException {
    private final String message;
    private final String canonicalStatusCode;
    private final HttpStatus httpStatus;
    private final String path;
    private final String transactionId;
    private final APIError apiError;

    public CanonicalErrorCodeException(String message, String canonicalStatusCode, HttpStatus httpStatus, String path, String transactionId, APIError apiError) {
        super(message);
        this.message = message;
        this.canonicalStatusCode = canonicalStatusCode;
        this.httpStatus = httpStatus;
        this.path = path;
        this.transactionId = transactionId;
        this.apiError = apiError;
    }

    public CanonicalErrorCodeException(CanonicalErrorCode canonicalErrorCode, String path, String transactionId, APIError apiError){
        super(canonicalErrorCode.getMessage());
        this.message = canonicalErrorCode.getMessage();
        this.canonicalStatusCode = canonicalErrorCode.getCanonicalCode();
        this.httpStatus = canonicalErrorCode.getHttpStatus();
        this.path = path;
        this.transactionId = transactionId;
        this.apiError = apiError;
    }

    public CanonicalErrorCodeException(CanonicalErrorCode canonicalErrorCode, String path, String transactionId, String message, APIError apiError) {
        super(message);
        this.message = message;
        this.canonicalStatusCode = canonicalErrorCode.getCanonicalCode();
        this.httpStatus = canonicalErrorCode.getHttpStatus();
        this.path = path;
        this.transactionId = transactionId;
        this.apiError = apiError;
    }

    public CanonicalErrorCodeException(CanonicalErrorCode canonicalErrorCode, String path,APIError apiError){
        super(canonicalErrorCode.getMessage());
        this.message = canonicalErrorCode.getMessage();
        this.canonicalStatusCode = canonicalErrorCode.getCanonicalCode();
        this.httpStatus = canonicalErrorCode.getHttpStatus();
        this.path = path;
        this.transactionId = this.getTransactionId();
        this.apiError = apiError;
    }

    public static CanonicalErrorCodeException canonicalException(CanonicalErrorCode exception, String path, APIError apiError) {

        CanonicalErrorCodeException canonicalErrorCodeException = new CanonicalErrorCodeException(exception, path,apiError);
        log.info(
                "Exception {} :: statusCode {} :: message {} :: path :: {}",
                canonicalErrorCodeException.getClass().getSimpleName(),
                canonicalErrorCodeException.getCanonicalStatusCode(),
                canonicalErrorCodeException.getMessage(),
                path
        );

        return canonicalErrorCodeException;
    }


    public static CanonicalErrorCodeException invalidRequestException(CanonicalErrorCode canonicalErrorCode,
                                                                      String path, APIError apiError) {
        return canonicalException(canonicalErrorCode, path, apiError);
    }

    public static CanonicalErrorCodeException invalidRequestException(CanonicalErrorCode canonicalErrorCode,
                                                                      String path, String transactionId) {
        return new CanonicalErrorCodeException(canonicalErrorCode, path, transactionId, null);
    }
}

package com.mtn.aggregator.enums.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Optional;

@Getter
public enum CanonicalErrorCode implements IResponseCode {

    HTTP_CONNECTION_ERROR(
            "2000", "Could not establish a connection to the server", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_INPUT_PARAMETERS(
            "5001", "The input parameters provided are invalid.", HttpStatus.BAD_REQUEST),
    OK("0000", "Success", HttpStatus.OK),
    CONSENT_VALIDATION_DELIVERED("0000", "Consent validation message delivered to user, your request will be process shortly.", HttpStatus.OK),
    SUB_NOT_FOUND("1000", "The subscription ID supplied does not exist.", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(
            "3002",
            "The endpoint cannot handle the supplied HTTP request method.",
            HttpStatus.METHOD_NOT_ALLOWED),
    INTERNAL_SERVER_ERROR(
            "5000",
            "An internal server error occurred and processing could not be completed.",
            HttpStatus.BAD_REQUEST),
    BAD_REQUEST("4000", "Bad request", HttpStatus.BAD_REQUEST),
    SERVICE_UNAVAILABLE("15", "The service is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    PARTIAL_STATUS("2006", "Partial content", HttpStatus.PARTIAL_CONTENT),
    MULTI_STATUS("0001", "Multi-status", HttpStatus.MULTI_STATUS),
    NOT_FOUND("4000", "MSISDN not found", HttpStatus.NOT_FOUND),
    INVALID_SENDER_MSISDN("1080", "Invalid sender MSISDN.", HttpStatus.BAD_REQUEST),
    INVALID_RECEIVER_MSISDN("1112", "Invalid receiver MSISDN.", HttpStatus.BAD_REQUEST),
    TIMEOUT("3003", "The request timed out", HttpStatus.REQUEST_TIMEOUT),
    MSISDN_NOT_ELIGIBLE_TO_BUNDLE("4002", "MSISDN not eligible to subscribe to this bundle.", HttpStatus.UNAUTHORIZED),
    SYSTEM_ERROR("3001", "There was an error while processing the request.", HttpStatus.UNPROCESSABLE_ENTITY),
    INVALID_RECEIVER_COUNTRY_CODE("1060", "Invalid receiver MSISDN country code", HttpStatus.BAD_REQUEST),
    INVALID_SENDER_COUNTRY_CODE("1070", "Invalid sender MSISDN country code", HttpStatus.BAD_REQUEST),
    BENEFICIARY_MSISDN_NOT_FOUND("4001", "User or Password does’t match", HttpStatus.BAD_REQUEST),
    PRODUCT_CODE_NOT_FOUND("4003", "Product code not found in request", HttpStatus.NOT_FOUND),
    NODE_ID_NOT_PROVIDED("1001", "NodeId not provided", HttpStatus.BAD_REQUEST),
    FAILED_TO_AUTHENTICATE_USER("3005", "Failed to Authenticate User", HttpStatus.UNAUTHORIZED),
    INVALID_PRODUCT_CODE("1113", "Invalid Product Code", HttpStatus.BAD_REQUEST),
    TRANSACTION_UNSUCCESSFUL("3003", "The transaction was unsuccessful", HttpStatus.UNPROCESSABLE_ENTITY),
    INSUFFICIENT_INVALID("3008", "The transaction was unsuccessful", HttpStatus.UNAUTHORIZED),
    INSUFFICIENT_FUND("3007", "Activation of product failed due to insufficient balance", HttpStatus.UNAUTHORIZED),
    INVALID_TRANSFER_TYPE("3009", "Invalid transfer type", HttpStatus.BAD_REQUEST),
    CONSENT_NOT_DELIVERED("1042", "Request not successful", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_PIN_CODE("1114", "Invalid Pin Code.", HttpStatus.BAD_REQUEST),
    INVALID_TRANSFER_AMOUNT("1115", "Invalid Transfer Amount.", HttpStatus.BAD_REQUEST),
    TRANSFER_LIMIT_ERROR("4004", "Sorry, you have reached your daily limit. Please try again later. Thank you.", HttpStatus.BAD_REQUEST),
    INVALID_PIN_ERROR("4005", "Sorry! you have dialed invalid old pin. Please check and try.", HttpStatus.BAD_REQUEST),
    OLD_PIN_ERROR("4006", "Sorry! you have dialed invalid old pin. Please check and try", HttpStatus.BAD_REQUEST),
    DEFAULT_PIN_ERROR("4007", "The transfer cannot be performed with the provided pin.", HttpStatus.BAD_REQUEST),
    SMS_NOT_SENT("1021", "Sms not sent.", HttpStatus.BAD_REQUEST),
    NO_ACTIVE_DATA_BUNDLE("4008", "You do not have an active data bundle.", HttpStatus.BAD_REQUEST),
    ACTIVATION_NOT_SUCCESSFUL("4009", "Activation of Data Transfer plan was not successful.", HttpStatus.BAD_REQUEST),
    MISSING_CHANNEL_ID("4010", "Channel ID missing in request.", HttpStatus.BAD_REQUEST),
    MISSING_CALLBACK_URL("4012", "CallbackUrl is missing.", HttpStatus.BAD_REQUEST),
    ACTIVATION_SHARE_INSUFFICIENT_BALANCE("4011", "Activation of MTN Share failed due to insufficient balance.", HttpStatus.BAD_REQUEST),
    SORRY_DATA_INSUFFICIENT_BALANCE("4013", "Sorry, your data balance is not sufficient for this transfer.You must have at least 50MB data balance after every transfer.Dial *131*1# to buy data plan.", HttpStatus.BAD_REQUEST),
    PROCESSING_FAILURE("1106", "The transaction processing was unsuccessful.", HttpStatus.PRECONDITION_FAILED),
    INVALID_AIRTIME_AMOUNT("1144", "Invalid Airtime Amount", HttpStatus.BAD_REQUEST),
    SUBSCRIBER_MSISDN_BARRED("1143", "Subscriber MSISDN Barred", HttpStatus.BAD_REQUEST),
    INVALID_MSISDN("1142", "Invalid MSISDN", HttpStatus.BAD_REQUEST),
    FAILED_REQUEST("1141", "Failed request", HttpStatus.BAD_REQUEST),
    INVALID_TRANSACTION_ID("3088", "Transaction Id is required", HttpStatus.BAD_REQUEST),
    NOT_FOUND_I("1140", "Not found", HttpStatus.NOT_FOUND),
    CALLBACK_NOT_FOUND("4040", "Callback Not found", HttpStatus.NOT_FOUND),
    TRANSACTION_ID_REQUIRED("4014", "TransactionId is required", HttpStatus.BAD_REQUEST),
    AUTHORIZATION_REQUIRED("4016", "Authorization required", HttpStatus.UNAUTHORIZED),
    SENDER_MSISDN_REQUIRED("4017", "Sender Msisdn is required", HttpStatus.BAD_REQUEST),
    REGISTRATION_CHANNEL_REQUIRED("3033", "Registration channel is required", HttpStatus.BAD_REQUEST),
    RECEIVER_MSISDN_REQUIRED("1136", "Receiver Msisdn is required", HttpStatus.BAD_REQUEST),
    PIN_REQUIRED("1137", "Receiver Pin is required", HttpStatus.BAD_REQUEST),
    SENDER_MSISDN_COUNTRY_CODE_NOT_FOUND("1138", "Sender msisdn country code not found", HttpStatus.BAD_REQUEST),
    MSISDN_BLACKLISTED("3059", "Msisdn already blacklisted", HttpStatus.BAD_REQUEST),
    RECEIVER_MSISDN_COUNTRY_CODE_NOT_FOUND("1139", "Receiver msisdn country code not found", HttpStatus.NOT_FOUND),
    MANDATORY_FIELD("1116", "Mandatory field <parameter> missing in the request", HttpStatus.BAD_REQUEST),
    VALIDATION_CLASS("1117", "Validation class missing for request API.", HttpStatus.BAD_REQUEST),
    MBAS_INTERNAL("3700", "mBAS Internal Error", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_REQUEST("1118", "Unauthorized request ", HttpStatus.UNAUTHORIZED),
    SUBSCRIBER_HAS("1119", "Subscriber has insufficient balance", HttpStatus.BAD_REQUEST),
    INVALID_REQUEST("1120", "Invalid Request ", HttpStatus.BAD_REQUEST),
    INVALID_SUBSCRIBER("1121", "Invalid Subscriber Number", HttpStatus.BAD_REQUEST),
    NOT_A("1122", "Not a Valid Plan", HttpStatus.BAD_REQUEST),
    ALREADY_SUBSCRIBED("1123", "Already Subscribed Requested Services", HttpStatus.BAD_REQUEST),
    BLANK_REQUEST("1124", "Blank Request Received", HttpStatus.BAD_REQUEST),
    SUBSCRIBER_NOT("1125", "Subscriber Not in Active State", HttpStatus.BAD_REQUEST),
    ACCEPTED_OK("1126", "Accepted OK ", HttpStatus.BAD_REQUEST),
    SERVICE_ID("1127", "Service id not configured for service node", HttpStatus.BAD_REQUEST),
    INVALID_BEARER("1128", "Invalid bearer Id", HttpStatus.BAD_REQUEST),
    SERVICE_NODE("1129", "Service node in request xml not matching with allowed service node in request URL", HttpStatus.BAD_REQUEST),
    PROBLEM_IN("1130", "Problem in generating subscription id", HttpStatus.BAD_REQUEST),
    REQUEST_UNDER("4030", "Request under process.", HttpStatus.PROCESSING),
    INVALID_SUBSCRIBER_CREDENTIALS("1131", "Invalid Subscriber Credentials/Answer", HttpStatus.BAD_REQUEST),
    DAP_TECHNICAL("3710", "DAP Technical Error", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("1132", "Invalid token, access token validation failed", HttpStatus.BAD_REQUEST),
    SERVICE_CLASS_NOT_ALLOWED("1133", "Service class not allowed for this product", HttpStatus.UNAUTHORIZED),
    SERVICE_CLASS("1134", "Service class list not configured for this product", HttpStatus.UNAUTHORIZED),
    SERVICE_VALIDATION("1135", "Service Validation type not configured for this product", HttpStatus.UNAUTHORIZED),
    UNMAPPED_RESPONSE("3800", "Unmapped response code", HttpStatus.BAD_REQUEST),
    AGENT_ID_REQUIRED("1144", "Agent Id is required", HttpStatus.BAD_REQUEST),
    DUPLICATE_TRANSACTION_ID("4090", "Duplicate transaction id", HttpStatus.CONFLICT),
    TRANSACTION_ID_TOO_LONG("1145", "Transaction Id should be maximum of 20 characters", HttpStatus.BAD_REQUEST),
    NON_NUMERIC_TRANSACTION_ID("1146", "TransactionId must be numeric", HttpStatus.BAD_REQUEST),
    INVALID_OFFER("1109", "Invalid Offer category.",HttpStatus.BAD_REQUEST),
    COULD_NOT_RETRIEVE_MAIN_ACCOUNT("9221", "Could not retrieve Main Account balance for customer.",HttpStatus.BAD_REQUEST),
    INVALID_REQUEST_PARAMS( "9231", "Invalid request parameters.",HttpStatus.BAD_REQUEST),
    INVALID_INPUT_PROVIDED( "4001", "The input parameters provided are invalid.",HttpStatus.BAD_REQUEST),
    INVALID_AUTH_SUPPLIED("4000", "The supplied authentication is invalid.",HttpStatus.UNAUTHORIZED),
    SERVICE_NOT_AVAILABLE("3000", "The service is not available.",HttpStatus.SERVICE_UNAVAILABLE),
    AUTHENTICATION_INVALID( "3001", "The supplied authentication is invalid.",HttpStatus.UNAUTHORIZED),
    ENDPOINT_CANNOT_HANDLE( "3002", "The endpoint cannot handle the supplied HTTP request method.",HttpStatus.METHOD_NOT_ALLOWED),
    TIMED_OUT("3004", " The request timed out.",HttpStatus.REQUEST_TIMEOUT),
    NO_RECORD("3005", "No record was found.",HttpStatus.NOT_FOUND),
    INTERFACE_NOT_ALLOWED( "3007", "Interface not allowed.",HttpStatus.METHOD_NOT_ALLOWED),
    NOT_ELIGIBLE_OFFER("4003", "You are not eligible for this offer.",HttpStatus.UNAUTHORIZED);



    String canonicalCode;
    Class<? extends Exception>[] exceptions;
    HttpStatus httpStatus;
    String message;

    @SafeVarargs
    CanonicalErrorCode(final String canonicalCode, final String message, HttpStatus httpStatus, final Class<? extends Exception>... exceptions) {
        this.canonicalCode = canonicalCode;
        this.message = message;
        this.exceptions = exceptions;
        this.httpStatus = httpStatus;
    }

    public static Optional<CanonicalErrorCode> resolveForException(Exception exception) {
        for (CanonicalErrorCode value : CanonicalErrorCode.values()) {
            for (Class<? extends Exception> valueException : value.getExceptions()) {
                if (valueException.equals(exception.getClass())) {
                    return Optional.of(value);
                }
            }
        }

        return Optional.empty();
    }

    public static CanonicalErrorCode getCanonicalErrorCode(String code) {
        for (CanonicalErrorCode responseCode : CanonicalErrorCode.values()) {
            if (responseCode.canonicalCode.equals(code)) {
                return responseCode;
            }
        }
        throw new IllegalArgumentException("Invalid Code Sent By Microservice: " + code);
    }

    public static Optional<CanonicalErrorCode> resolveForCode(String code) {
        for (CanonicalErrorCode value : CanonicalErrorCode.values()) {
            if (code.equals(value.getCanonicalCode())) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    public String getCanonicalCode() {
        return canonicalCode;
    }

    public String getMessage() {
        return message;
    }

    public Class<? extends Exception>[] getExceptions() {
        return exceptions;
    }

}

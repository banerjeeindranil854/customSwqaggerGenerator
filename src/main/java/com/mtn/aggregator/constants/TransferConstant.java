package com.mtn.aggregator.constants;

public class TransferConstant {

    private TransferConstant() {}

    public static final int INDEX_TRANSACTION_ID_CUSTOM_DATA = 0;

    public static final int INDEX_RECEIVER_MSISDN_CUSTOM_DATA = 1;

    public static final int INDEX_PRODUCT_CODE_CUSTOM_DATA = 2;

    public static final int INDEX_TYPE_CUSTOM_DATA = 3;

    public static final int INDEX_AGENT_ID_CUSTOM_DATA = 4;

    public static final int INDEX_TRANSFER_AMOUNT_CUSTOM_DATA = 5;

    public static final int INDEX_CALLBACK_URL_CUSTOM_DATA = 6;

    public static final int INDEX_NODE_ID_CUSTOM_DATA = 7;

    public static final int SIZE_OF_CONSENT_CUSTOM_DATA = 8;


    public static final String NOTIFICATION_PATH = "/notification";

    public static final String PARTNER_NAME_HEADER_LABEL = "partnerName";
    public static final String TRANSACTION_ID_HEADER_LABEL = "transactionId";
    public static final String SEQUENCE_NO_HEADER_LABEL = "sequenceNo";
    public static final String SENDER_MSISDN_PLACEHOLDER = "{senderMsisdn}";
}

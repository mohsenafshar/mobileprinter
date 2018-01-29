package com.simonguest.btxfr;

public class MessageType {
    public static final int DATA_SENT_OK = 0x00;
    public static final int READY_FOR_DATA = 0x01;
    public static final int READY_FOR_TEXT_DATA = 0x06;
    public static final int DATA_RECEIVED = 0x02;
    public static final int DATA_TEXT_RECEIVED = 0x05;
    public static final int DATA_PROGRESS_UPDATE = 0x03;

    public static final int SENDING_DATA = 0x04;
    public static final int DIGEST_DID_NOT_MATCH = 0x50;
    public static final int COULD_NOT_CONNECT = 0x51;
    public static final int INVALID_HEADER = 0x52;
    public static final int UPDATE_STATE_CONNECTING = 0x53;
    public static final int UPDATE_STATE_CONNECTED = 0x54;
}

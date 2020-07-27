package com.montefiore.gaulthiergain.distributedcache;

import java.io.Serializable;

public class Message implements Serializable {

    private String mobile;
    private final int type;
    private final Object pdu;

    Message(int type, Object pdu) {
        this.type = type;
        this.pdu = pdu;
    }

    Message(int type, String mobile, Object pdu) {
        this.type = type;
        this.mobile = mobile;
        this.pdu = pdu;
    }

    public int getType() {
        return type;
    }

    public Object getPdu() {
        return pdu;
    }

    public String getMobile() {
        return mobile;
    }

    @Override
    public String toString() {
        return "Message{" +
                "type=" + type +
                ", mobile='" + mobile + '\'' +
                ", pdu=" + pdu +
                '}';
    }
}

package pl.pjtom.distributed_monitor.communication;

import java.io.Serializable;

public class Message implements Serializable {
    private MessageType msgType;
    private String senderId;
    private Serializable payload;

    public Message(MessageType msgType, String senderId, Serializable payload) {
        this.msgType = msgType;
        this.senderId = senderId;
        this.payload = payload;
    }

    public MessageType getMessageType() {
        return msgType;
    }

    public String getSenderId() {
        return senderId;
    }

    public Serializable getPayload() {
        return payload;
    }


}

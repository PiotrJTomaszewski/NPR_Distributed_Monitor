package pl.pjtom.distributed_monitor.communication;

import java.io.Serializable;
import java.util.HashMap;

public class Message implements Serializable {
    private MessageType msgType;
    private String senderId;
    private Serializable payload;
    private HashMap<String, Integer> seqNoMap = null;
    private Integer seqNoInt = null;

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

    public void setSeqNoMap(HashMap<String, Integer> seqNoMap) {
        this.seqNoMap = seqNoMap;
    }

    public void setSeqNo(Integer seqNo) {
        this.seqNoInt = seqNo;
    }

    public Integer getSeqNo(String identifier) {
        if (this.seqNoMap != null) {
            return seqNoMap.get(identifier);
        } else {
            return seqNoInt;
        }
    }

}

package pl.pjtom.distributed_monitor.communication;

public enum MessageType {
    INIT, INIT_ACK, CS_REQUEST, TOKEN, SIGNAL, OBJECT_SYNC
    // CS_REPLY, OBJECT_SYNC, SIGNAL, ACK, CLOSE, INTERNAL_NOTIFY_CS_REPLY_QUEUE
}

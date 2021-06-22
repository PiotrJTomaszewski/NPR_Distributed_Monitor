package pl.pjtom.distributed_monitor.node;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class OtherNode extends Node {
    private ZMQ.Socket broadcastRcvSocket;
    private ZMQ.Socket sendSocket;
    private volatile boolean waitingForResponse = false;
    private Object waitingForResponseLock = new Object();

    public OtherNode(String identifier, String address, String listenOnAddress, String repPort, String pubPort, ZContext zmqContext) {
        super(identifier);
        this.address = address;
        this.listenOnAddress = listenOnAddress;
        this.listenPort = repPort;
        this.sendPort = pubPort;
        this.broadcastRcvSocket = zmqContext.createSocket(SocketType.SUB);
        connectBroadcastRcvSocket();
        this.sendSocket = zmqContext.createSocket(SocketType.PUSH);
        connectSendSocket();
    }

    private void connectBroadcastRcvSocket() {
        final int maxRetry = 10;
        for (int i = 0; i < maxRetry; i++) {
            if (broadcastRcvSocket.connect("tcp://" + address + ":" + sendPort)) {
                broadcastRcvSocket.subscribe("");
                break;
            } else {
                System.err.printf("Cannot connect to the node %s on port %s. Retry %d/%d\n", identifier, sendPort, i, maxRetry);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    private void connectSendSocket() {
        final int maxRetry = 10;
        for (int i = 0; i < maxRetry; i++) {
            if (sendSocket.connect("tcp://" + address + ":" + listenPort)) {
                break;
            } else {
                System.err.printf("Cannot connect to the node %s on port %s. Retry %d/%d\n", identifier, listenPort, i, maxRetry);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }

    public ZMQ.Socket getBroadcastRecvSocket() {
        return broadcastRcvSocket;
    }

    public ZMQ.Socket getSendSocket() {
        return sendSocket;
    }

    public void setWaitingForResponse(boolean waiting) {
        synchronized(waitingForResponseLock) {
            waitingForResponse = waiting;
        }
    }

    public boolean getWaitingForResponse() {
        synchronized(waitingForResponseLock) {
            return waitingForResponse;
        }
    }

}

package pl.pjtom.distributed_monitor.node;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


public class MyNode extends Node{
    private ZMQ.Socket broadcastSendSocket;
    private ZMQ.Socket recvSocket;

    public MyNode(String identifier) {
        super(identifier);
    }

    public void init(String address, String listenOnAddress, String listenPort, String sendPort, ZContext zmqContext) {
        this.address = address;
        this.listenOnAddress = listenOnAddress;
        this.listenPort = listenPort;
        this.sendPort = sendPort;
        this.broadcastSendSocket = zmqContext.createSocket(SocketType.PUB);
        this.broadcastSendSocket.bind("tcp://" + listenOnAddress + ":" + sendPort);
        this.recvSocket = zmqContext.createSocket(SocketType.PULL);
        this.recvSocket.bind("tcp://" + listenOnAddress + ":" + listenPort);
    }

    public ZMQ.Socket getBroadcastSendSocket() {
        return broadcastSendSocket;
    }

    public ZMQ.Socket getRecvSocket() {
        return recvSocket;
    }

}

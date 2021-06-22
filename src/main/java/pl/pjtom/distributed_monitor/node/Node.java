package pl.pjtom.distributed_monitor.node;

public class Node {
    protected String identifier;
    protected String address;
    protected String listenOnAddress;
    protected String listenPort;
    protected String sendPort;

    public Node(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getListenOnAddress() {
        return listenOnAddress;
    }

    public void setListenOnAddress(String listenOnAddress) {
        this.listenOnAddress = listenOnAddress;
    }

    public String getListenPort() {
        return listenPort;
    }

    public void setListenPort(String listenPort) {
        this.listenPort = listenPort;
    }

    public String getSendPort() {
        return sendPort;
    }

    public void setSendPort(String sendPort) {
        this.sendPort = sendPort;
    }
}

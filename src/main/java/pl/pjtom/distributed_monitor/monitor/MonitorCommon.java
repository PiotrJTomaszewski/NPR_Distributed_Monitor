package pl.pjtom.distributed_monitor.monitor;

import java.io.Serializable;
import java.util.HashMap;

import pl.pjtom.distributed_monitor.CondVar;
import pl.pjtom.distributed_monitor.Token;
import pl.pjtom.distributed_monitor.node.MyNode;
import pl.pjtom.distributed_monitor.node.OtherNode;

public class MonitorCommon {
    public enum MonitorState {
        OTHER_STUFF, WAITING_FOR_CS, IN_CS, AWAITING
    }

    private volatile MonitorState monitorState = MonitorState.OTHER_STUFF;
    private volatile Serializable sharedObject;
    private final int distCondVarCount;
    private CondVar[] distCondVars;
    private CondVar csCondVar = new CondVar(1);
    private String myIdentifier;
    private HashMap<String, Integer> requestNumber = new HashMap<>();
    private boolean hasToken = false;
    private Token token;

    public MonitorCommon(Serializable sharedObject, int condVarCount) {
        this.sharedObject = sharedObject;
        this.distCondVarCount = condVarCount;
        this.distCondVars = new CondVar[condVarCount];
        for (int i=0; i<condVarCount; i++) {
            distCondVars[i] = new CondVar(1);
        }
    }

    public void initCSStructures(MyNode myNode, HashMap<String, OtherNode> otherNodes, boolean hasToken) {
        myIdentifier = myNode.getIdentifier();
        requestNumber.put(myNode.getIdentifier(), 0);
        for (OtherNode node: otherNodes.values()) {
            requestNumber.put(node.getIdentifier(), 0);
        }
        if (hasToken) {
            token = new Token(myNode, otherNodes);
            this.hasToken = true;
        }
    }

    public void setMonitorState(MonitorState state) {
        this.monitorState = state;
    }

    public MonitorState getMonitorState() {
        return monitorState;
    }

    public void setSharedObject(Serializable sharedObject) {
        this.sharedObject = sharedObject;
    }

    public Serializable getSharedObject() {
        return sharedObject;
    }

    public int getDistCondVarCount() {
        return distCondVarCount;
    }

    public CondVar getDistCondVar(int condVarId) {
        return distCondVars[condVarId];
    }

    public CondVar getCSCondVar() {
        return csCondVar;
    }

    public Integer incrementMyRequestNumber() {
        Integer currentVal = requestNumber.get(myIdentifier);
        requestNumber.replace(myIdentifier, currentVal+1);
        return currentVal+1;
    }

    public void updateMyRequestNumberInToken() {
        token.setLastRequestNumber(myIdentifier, requestNumber.get(myIdentifier));
    }

    public void updateRequestNumber(String identifier, Integer receivedValue) {
        Integer current = requestNumber.get(identifier);
        requestNumber.replace(identifier, Math.max(receivedValue, current));
    }

    public boolean checkRequestNumberWithToken(String identifier) {
        return requestNumber.get(identifier) == (token.getLastRequestNumber(identifier) + 1);
    }

    public void updateToken() {
        token.updateQueue(requestNumber);
    }

    public String tokenQueuePop() {
        return token.queuePop();
    }

    public void setHasToken(boolean hasToken) {
        this.hasToken = hasToken;
    }

    public boolean getHasToken() {
        return hasToken;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

}

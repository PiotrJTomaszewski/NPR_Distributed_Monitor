package pl.pjtom.distributed_monitor;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import pl.pjtom.distributed_monitor.node.MyNode;
import pl.pjtom.distributed_monitor.node.OtherNode;

public class Token implements Serializable {
    private HashMap<String, Integer> lastRequestNumber = new HashMap<>();
    private Deque<String> queue = new ArrayDeque<>();
    private Serializable sharedObject = null;

    public Token(MyNode myNode, HashMap<String, OtherNode> otherNodes) {
        lastRequestNumber.put(myNode.getIdentifier(), 0);
        for (OtherNode node: otherNodes.values()) {
            lastRequestNumber.put(node.getIdentifier(), 0);
        }
    }

    public void setLastRequestNumber(String identifier, Integer newLastRequestNumber) {
        lastRequestNumber.replace(identifier, newLastRequestNumber);
    }

    public Integer getLastRequestNumber(String identifier) {
        return lastRequestNumber.get(identifier);
    }

    public void updateQueue(HashMap<String, Integer> requestNumber) {
        int RNi, LNi;

        for (String identifier: requestNumber.keySet()) {
            if (!queue.contains(identifier)) {
                RNi = requestNumber.get(identifier);
                LNi = lastRequestNumber.get(identifier);
                if (RNi >= LNi + 1) {
                    queue.add(identifier);
                }
            }
        }
    }

    public String queuePop() {
        if (queue.size() > 0) {
            return queue.pop();
        }
        return null;
    }

    public void setSharedObject(Serializable sharedObject) {
        this.sharedObject = sharedObject;
    }

    public Serializable getSharedObject() {
        return sharedObject;
    }

    public void debugPrintQueue() {
        Object[] queueArray = queue.toArray();
        for (int i=0; i<queueArray.length; i++) {
            Debug.printf(Debug.DebugLevel.LEVEL_HIGHEST, Debug.Color.YELLOW, "In queue -> %s", queueArray[i]);
        }
    }

}

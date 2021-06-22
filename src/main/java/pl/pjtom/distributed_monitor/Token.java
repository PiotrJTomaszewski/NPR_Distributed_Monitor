package pl.pjtom.distributed_monitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;

import pl.pjtom.distributed_monitor.node.MyNode;
import pl.pjtom.distributed_monitor.node.OtherNode;

public class Token implements Serializable {
    private HashMap<String, Integer> lastRequestNumber = new HashMap<>();
    private LinkedList<String> queue = new LinkedList<>();

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
                if (RNi == LNi + 1) {
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

    public void debugPrintQueue() {
        for (int i=0; i<queue.size(); i++) {
            Debug.printf(Debug.DebugLevel.LEVEL_HIGHEST, Debug.Color.YELLOW, "In queue -> %s", queue.get(i));
        }
    }

}

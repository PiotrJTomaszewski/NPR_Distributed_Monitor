package pl.pjtom.distributed_monitor.communication;

import java.util.Comparator;
import java.util.PriorityQueue;

public class MessageQueue {
    private class MessageSeqNoComparator implements Comparator<Message>{
        @Override
        public int compare(Message msgOne, Message msgTwo) {
            Integer msgOneSeqNo = msgOne.getSeqNo(myNodeId);
            Integer msgTwoSeqNo = msgTwo.getSeqNo(myNodeId);

            if (msgOneSeqNo < msgTwoSeqNo) {
                return -1;
            } else if (msgOneSeqNo > msgTwoSeqNo) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private String myNodeId;
    private PriorityQueue<Message> msgQueue = new PriorityQueue<>(new MessageSeqNoComparator());
    
    public MessageQueue(String myNodeId) {
        this.myNodeId = myNodeId;
    }

    public void add(Message msg) {
        msgQueue.add(msg);
    }

    public Message pop() {
        return msgQueue.poll();
    }

    public Message peek() {
        return msgQueue.peek();
    }

}

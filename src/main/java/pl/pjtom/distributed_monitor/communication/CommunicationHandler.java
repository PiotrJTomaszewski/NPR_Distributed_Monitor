package pl.pjtom.distributed_monitor.communication;

import java.io.Serializable;

import pl.pjtom.distributed_monitor.CondVar;
import pl.pjtom.distributed_monitor.Debug;
import pl.pjtom.distributed_monitor.Debug.DebugLevel;
import pl.pjtom.distributed_monitor.monitor.MonitorCommon;
import pl.pjtom.distributed_monitor.node.OtherNode;


public class CommunicationHandler {
    private CommunicationCommon commCommon;
    private MonitorCommon monCommon;
    private Thread communicationThread;

    public CommunicationHandler(MonitorCommon monCommon) {
        commCommon = new CommunicationCommon(monCommon);
        this.monCommon = monCommon;
        runCommunicationThread();
        initNodeSync();
    }

    public void close() {
        commCommon.setCanClose(true);
        broadcast(MessageType.CLOSE, null);
        try {
            communicationThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        commCommon.cleanup();
    }

    private void runCommunicationThread() {
        communicationThread = new Thread(new CommunicationThread(commCommon, monCommon));
        communicationThread.start();
    }

    private void initNodeSync() {
        CondVar condVar = commCommon.getInitialCondVar();
        condVar.lock();
        condVar.setTargetVal(commCommon.getOhterNodesCount());
        condVar.setCurrentVal(0);
        Message msg = new Message(MessageType.INIT, commCommon.getMyIdentifier(), null);
        for (OtherNode node: commCommon.getOtherNodesCollection()) {
            Debug.printf(DebugLevel.LEVEL_MORE, Debug.Color.YELLOW, "Sending INIT to %s\n", node.getIdentifier());
            commCommon.send(msg, node.getIdentifier());
        }
        condVar.await();
        condVar.unlock();
    }

    public String getMyIdentifier() {
        return commCommon.getMyIdentifier();
    }

    public void send(MessageType msgType, Serializable payload, String recipientId) {
        commCommon.send(new Message(msgType, commCommon.getMyIdentifier(), payload), recipientId);
    }

    public void broadcast(MessageType msgType, Serializable payload) {
        commCommon.broadcast(new Message(msgType, commCommon.getMyIdentifier(), payload));
    }

}

package pl.pjtom.distributed_monitor.communication;

import org.zeromq.ZMQ;

import pl.pjtom.distributed_monitor.CondVar;
import pl.pjtom.distributed_monitor.Debug;
import pl.pjtom.distributed_monitor.Token;
import pl.pjtom.distributed_monitor.Debug.DebugLevel;
import pl.pjtom.distributed_monitor.monitor.MonitorCommon;
import pl.pjtom.distributed_monitor.monitor.MonitorCommon.MonitorState;
import pl.pjtom.distributed_monitor.node.OtherNode;

public class CommunicationThread implements Runnable {
    private CommunicationCommon commCommon;
    private MonitorCommon monCommon;
    private ZMQ.Poller poller;
    private int closeMsgRecvNeeded;

    public CommunicationThread(CommunicationCommon commCommon, MonitorCommon monCommon) {
        this.commCommon = commCommon;
        this.monCommon = monCommon;
        this.closeMsgRecvNeeded = commCommon.getOhterNodesCount();

        poller = commCommon.getContext().createPoller(commCommon.getOhterNodesCount() + 1);
        poller.register(commCommon.getMyNode().getRecvSocket(), ZMQ.Poller.POLLIN);
        for (OtherNode node : commCommon.getOtherNodesCollection()) {
            poller.register(node.getBroadcastRecvSocket(), ZMQ.Poller.POLLIN);
        }
    }

    private void handleOuterMessage(Message msg) {
        CondVar condVar;
        switch (msg.getMessageType()) {
            case INIT:
                Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received INIT from %s, replying INIT_ACK", msg.getSenderId());
                commCommon.send(new Message(MessageType.INIT_ACK, commCommon.getMyIdentifier(), null), msg.getSenderId());
                break;
            case INIT_ACK:
                condVar = commCommon.getInitialCondVar();
                condVar.lock();
                condVar.incrementCurrentVal();
                Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received INIT_ACK from %s, have %d, need %d",
                        msg.getSenderId(), condVar.getCurrentVal(), condVar.getTargetVal());
                condVar.signalIfReady();
                condVar.unlock();
                break;
            case CS_REQUEST:
                condVar = monCommon.getCSCondVar();
                condVar.lock();
                monCommon.updateRequestNumber(msg.getSenderId(), (Integer)msg.getPayload());
                Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received CS_REQUEST from %s RNi: %d", msg.getSenderId(), (Integer)msg.getPayload());
                if (monCommon.getHasToken() && monCommon.getMonitorState() != MonitorState.IN_CS) {
                    if (monCommon.checkRequestNumberWithToken(msg.getSenderId())) {
                        Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received CS_REQUEST from %s I have a token and don't need it so sending to him", msg.getSenderId());
                        commCommon.send(new Message(MessageType.TOKEN, commCommon.getMyIdentifier(), monCommon.getToken()), msg.getSenderId());
                        monCommon.setHasToken(false);
                    } else if (monCommon.getMonitorState() == MonitorState.FINISHED) {
                        Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received CS_REQUEST from %s I have a token and I've finished so sending it to him", msg.getSenderId());
                        commCommon.send(new Message(MessageType.TOKEN, commCommon.getMyIdentifier(), monCommon.getToken()), msg.getSenderId());
                        monCommon.setHasToken(false);
                    } else {
                        Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received CS_REQUEST from %s It's an old request", msg.getSenderId());
                    }
                }
                condVar.unlock();
                break;
            case TOKEN:
                condVar = monCommon.getCSCondVar();
                condVar.lock();
                monCommon.setToken((Token)msg.getPayload());
                monCommon.setHasToken(true);
                condVar.incrementCurrentVal();
                condVar.signalIfReady();
                condVar.unlock();
                break;
            case SIGNAL:
                int condVarId = (int) msg.getPayload();
                condVar = monCommon.getDistCondVar(condVarId);
                condVar.lock();
                condVar.setCurrentVal(1);
                condVar.signalIfReady();
                condVar.unlock();
                break;
            case OBJECT_SYNC:
                monCommon.setSharedObject(msg.getPayload());
                break;
            case CLOSE:
                --closeMsgRecvNeeded;
                Debug.printf(DebugLevel.LEVEL_HIGHEST, Debug.Color.GREEN, "Received CLOSE from %s, need %d more ", msg.getSenderId(), closeMsgRecvNeeded);
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        Message msg;

        while (!commCommon.getCanClose() || closeMsgRecvNeeded > 0) {
            poller.poll(1000);
            if (poller.pollin(0)) {
                msg = commCommon.bytesToMessage(poller.getSocket(0).recv(ZMQ.DONTWAIT));
                Debug.printf(DebugLevel.LEVEL_MORE, Debug.Color.BLUE, "Received from: %s, message type: %s\n",
                        msg.getSenderId(), msg.getMessageType().toString());
                handleOuterMessage(msg);
            }
            for (int i = 1; i < poller.getSize(); i++) {
                if (poller.pollin(i)) {
                    msg = commCommon.bytesToMessage(poller.getSocket(i).recv(ZMQ.DONTWAIT));
                    Debug.printf(DebugLevel.LEVEL_MORE, Debug.Color.BLUE,
                            "Received as broadcast from: %s, message type: %s\n", msg.getSenderId(),
                            msg.getMessageType().toString());
                    handleOuterMessage(msg);
                }
            }
        }
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "Closing receiver thread");
        this.poller.close();
    }
}

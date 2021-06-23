package pl.pjtom.distributed_monitor.monitor;

import java.io.Serializable;
import java.util.NoSuchElementException;

import pl.pjtom.distributed_monitor.CondVar;
import pl.pjtom.distributed_monitor.Debug;
import pl.pjtom.distributed_monitor.Debug.DebugLevel;
import pl.pjtom.distributed_monitor.communication.CommunicationHandler;
import pl.pjtom.distributed_monitor.communication.MessageType;
import pl.pjtom.distributed_monitor.monitor.MonitorCommon.MonitorState;

public class DistributedMonitor {
    private CommunicationHandler comHandler;
    private MonitorCommon monCom;

    public DistributedMonitor(int condVarCount) {
        Debug.init();
        monCom = new MonitorCommon(condVarCount);
        comHandler = new CommunicationHandler(monCom);
    }

    public void distWait(int condVarId) {
        if (condVarId < 0 || condVarId >= monCom.getDistCondVarCount()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        CondVar condVar = monCom.getDistCondVar(condVarId);
        condVar.lock();
        condVar.setIsWaiting(true);
        if (monCom.getMonitorState() == MonitorState.IN_CS) {
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "WAIT: Leaving critical section");
            distRelease();
        }
        monCom.setMonitorState(MonitorState.AWAITING);
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "WAIT: Waiting for %d", condVarId);
        condVar.await();
        condVar.unlock();
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "WAIT: Trying to enter critical section");
        distAcquire();
    }

    public void distWait() {
        distWait(0);
    }

    public void distNotifyAll(int condVarId) {
        if (condVarId < 0 || condVarId >= monCom.getDistCondVarCount()) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "NOTIFY: Notifying all on %d", condVarId);
        comHandler.broadcast(MessageType.SIGNAL, condVarId);
    }

    public void distNotifyAll() {
        distNotifyAll(0);
    }

    public void distAcquire() {
        Integer newSequenceNumber;
        CondVar csCondVar = monCom.getCSCondVar();
        csCondVar.lock();
        if (!monCom.getHasToken()) {
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "ACQUIRE: Requesting critical section");
            monCom.setMonitorState(MonitorState.WAITING_FOR_CS);
            csCondVar.setIsWaiting(true);
            newSequenceNumber = monCom.incrementMyRequestNumber();
            comHandler.broadcast(MessageType.CS_REQUEST, newSequenceNumber);
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "ACQUIRE: Waiting for critical section");
            csCondVar.awaitWithCallbackOnTimeout(5000L, CSWaitTimeoutCallback);
            monCom.setMonitorState(MonitorState.IN_CS);
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "ACQUIRE: I'm in a critical section");
        } else {
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "ACQUIRE: I have a token so I can enter critical section");
            monCom.setMonitorState(MonitorState.IN_CS);
        }
        csCondVar.unlock();
    }

    public void distRelease() {
        monCom.getCSCondVar().lock();
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "Leaving critical section");
        monCom.setMonitorState(MonitorState.OTHER_STUFF);
        monCom.updateMyRequestNumberInToken();
        monCom.updateTokenQueue();
        String recipientId = monCom.tokenQueuePop();
        if (recipientId != null && recipientId.equals(comHandler.getMyIdentifier())) {
            recipientId = monCom.tokenQueuePop();
        } 
        if (recipientId != null) {
            monCom.setHasToken(false);
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "Sending token to %s", recipientId);
            comHandler.send(MessageType.TOKEN, monCom.getToken(), recipientId);
        } else {
            Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "Noone wants the token");
        }
        monCom.getCSCondVar().unlock();
    }

    public String getMyIdentifier() {
        return comHandler.getMyIdentifier();
    }

    public void setSharedObject(Serializable sharedObject) {
        if (monCom.getHasToken())
            monCom.getToken().setSharedObject(sharedObject);
        else throw new NoSuchElementException();
    }

    public Serializable getSharedObject() {
        if (monCom.getHasToken())
            return monCom.getToken().getSharedObject();
        else throw new NoSuchElementException();
    }

    public boolean hasToken() {
        return monCom.getHasToken();
    }

    public void close() {
        monCom.setMonitorState(MonitorState.FINISHED);
        Debug.printf(DebugLevel.LEVEL_BASIC, Debug.Color.GREEN, "Closing");
        comHandler.close();
    }

    private CondVar.CondVarTimeoutCallback CSWaitTimeoutCallback = () -> {
        comHandler.broadcast(MessageType.CS_REQUEST, monCom.incrementMyRequestNumber());
    };

    public void signalWaitReady() {
        comHandler.initNodeSync();
    }

}

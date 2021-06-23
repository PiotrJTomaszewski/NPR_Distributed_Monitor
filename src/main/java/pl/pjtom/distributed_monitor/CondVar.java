package pl.pjtom.distributed_monitor;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CondVar {
    private ReentrantLock lock = new ReentrantLock();
    private Condition cond = lock.newCondition();
    private int currentVal = 0;
    private int targetVal;
    private boolean isWaiting = false;

    public CondVar(int targetVal) {
        this.targetVal = targetVal;
    }

    public CondVar() {}

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void await() {
        isWaiting = true;
        while (currentVal < targetVal) {
            try {
                cond.await();
            } catch (InterruptedException e) {}
        }
        isWaiting = false;
        currentVal = 0;
    }

    public void awaitWithCallbackOnTimeout(long timeoutMillis, CondVarTimeoutCallback callback) {
        isWaiting = true;
        while (currentVal < targetVal) {
            try {
                cond.awaitNanos(timeoutMillis * 1000000);
            } catch (InterruptedException e) {}
            if (currentVal < targetVal) {
                callback.call();
            }
        }
        isWaiting = false;
        currentVal = 0;
    }

    public void setTargetVal(int targetVal) {
        this.targetVal = targetVal;
    }

    public void setCurrentVal(int currentVal) {
        this.currentVal = currentVal;
    }

    public void incrementCurrentVal() {
            this.currentVal++;
    }

    public void signalIfReady() {
        if (currentVal >= targetVal) {
            if (isWaiting) {
                cond.signalAll();
            } else {
                currentVal = 0;
            }
        }
    }

    public int getCurrentVal() {
        return currentVal;
    }

    public int getTargetVal() {
        return targetVal;
    }

    public void setIsWaiting(boolean isWaiting) {
        this.isWaiting = isWaiting;
    }

    public interface CondVarTimeoutCallback {
        public void call();
    }
}

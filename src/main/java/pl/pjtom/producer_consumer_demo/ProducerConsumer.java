package pl.pjtom.producer_consumer_demo;

import java.util.Random;

import pl.pjtom.distributed_monitor.monitor.DistributedMonitor;

/**
 * Przykład użycia biblioteki "DistributedMonitor". Problem
 * producenta-konsumenta zrealizowany za pomocą współdzielonego bufora.
 */
public class ProducerConsumer {
    private final int COND_VAR_COUNT = 2;
    private final int ITEM_IN = 0;
    private final int ITEM_OUT = 1;
    private Random rand = new Random();

    public class Producer {
        public Producer(DistributedMonitor distMon) {
            Buffer buf;
            for (int i = 0; i < 100; i++) {
                // Do some work
                try {
                    Thread.sleep(rand.nextInt(300) + 100);
                } catch (InterruptedException e) {
                }
                distMon.distAcquire();
                buf = (Buffer) distMon.getSharedObject();
                while (buf.isFull()) {
                    distMon.distWait(ITEM_OUT);
                    buf = (Buffer) distMon.getSharedObject();
                }
                buf.put(i);
                System.out.printf("Put %d\n", i);
                distMon.distSync(buf);
                distMon.distNotifyAll(ITEM_IN);
                distMon.distRelease();
            }
        }
    }

    public class Consumer {
        public Consumer(DistributedMonitor distMon) {
            Buffer buf;
            int val;
            for (int i = 0; i < 100; i++) {
                distMon.distAcquire();
                buf = (Buffer) distMon.getSharedObject();
                while (buf.isEmpty()) {
                    distMon.distWait(ITEM_IN);
                    buf = (Buffer) distMon.getSharedObject();
                }
                val = buf.get();
                distMon.setSharedObject(buf);
                distMon.distSync();
                distMon.distNotifyAll(ITEM_OUT);
                distMon.distRelease();
                // Do some work
                System.out.println(val);
                try {
                    Thread.sleep(rand.nextInt(300) + 100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public ProducerConsumer() {
        Buffer sharedBuffer = new Buffer();
        DistributedMonitor distMon = new DistributedMonitor(sharedBuffer, COND_VAR_COUNT);
        switch (distMon.getMyIdentifier()) {
            case "Jeden":
            case "Trzy":
                new Producer(distMon);
                break;
            case "Dwa":
            case "Cztery":
                new Consumer(distMon);
                break;
            default:
                System.out.println("No job for me");
                break;
        }
        // End connections and cleanup
        distMon.close();
    }
}

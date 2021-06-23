package pl.pjtom.producer_consumer_demo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private FileWriter writer;
    private volatile Buffer buf;

    private void initWriter(String filename) {
        File file = new File(filename);
        try {
            file.createNewFile();
            writer = new FileWriter(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class Producer {
        public Producer(DistributedMonitor distMon) {
            distMon.signalWaitReady();
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
                if (distMon.getMyIdentifier().equals("Jeden")) {
                    buf.put(i+1000);
                    System.out.printf("Put %d\n", i+1000);
                    try {
                        writer.write("Put " + String.valueOf(i+1000) + "\n");
                    } catch (IOException e) {System.out.print(e.getStackTrace()); System.exit(-1);}
                } else {
                    buf.put(i);
                    System.out.printf("Put %d\n", i);
                    try {
                        writer.write("Put " + String.valueOf(i) + "\n");
                    } catch (IOException e) {System.out.print(e.getStackTrace()); System.exit(-1);}
                }
                distMon.setSharedObject(buf);
                distMon.distNotifyAll(ITEM_IN);
                distMon.distRelease();
            }
        }
    }

    public class Consumer {
        public Consumer(DistributedMonitor distMon) {
            int val;
            distMon.signalWaitReady();
            for (int i = 0; i < 100; i++) {
                distMon.distAcquire();
                buf = (Buffer) distMon.getSharedObject();
                while (buf.isEmpty()) {
                    distMon.distWait(ITEM_IN);
                    buf = (Buffer) distMon.getSharedObject();
                }
                val = buf.get();
                distMon.setSharedObject(buf);
                distMon.distNotifyAll(ITEM_OUT);
                distMon.distRelease();
                System.out.println(val);
                try {
                    writer.write(String.valueOf(val) + "\n");
                } catch (IOException e) {System.out.print(e.getStackTrace()); System.exit(-1);}
                // Do some work
                try {
                    Thread.sleep(rand.nextInt(300) + 100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public ProducerConsumer() {
        DistributedMonitor distMon = new DistributedMonitor(COND_VAR_COUNT);
        initWriter(distMon.getMyIdentifier());
        if (distMon.hasToken()) {
            distMon.setSharedObject(new Buffer(10));
        }
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
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        distMon.close();
    }
}

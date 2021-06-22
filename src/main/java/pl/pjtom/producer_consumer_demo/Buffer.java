package pl.pjtom.producer_consumer_demo;

import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;

public class Buffer implements Serializable {
    final int MAX_SIZE = 10;
    int[] buf;
    int writeIndex = 0;
    int readIndex = 0;
    int currentSize = 0;

    public Buffer() {
        buf = new int[MAX_SIZE];
    }

    public void put(int value) {
        if (currentSize < MAX_SIZE) {
            buf[writeIndex] = value;
            writeIndex = (writeIndex + 1) % MAX_SIZE;
            currentSize++;
        } else {
            throw new BufferOverflowException();
        }
    }

    public int get() {
        int val;
        if (currentSize > 0) {
            val = buf[readIndex];
            readIndex = (readIndex + 1) % MAX_SIZE;
            currentSize--;
            return val;
        } else {
            throw new BufferUnderflowException();
        }
    }

    public boolean isFull() {
        return currentSize == MAX_SIZE;
    }

    public boolean isEmpty() {
        return currentSize == 0;
    }
}
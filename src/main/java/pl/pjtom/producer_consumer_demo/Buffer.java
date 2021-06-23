package pl.pjtom.producer_consumer_demo;

import java.io.Serializable;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Buffer implements Serializable {
    private Deque<Integer> data = new ArrayDeque<>();
    private final int MAX_SIZE;

    public Buffer(int MAX_SIZE) {
        this.MAX_SIZE = MAX_SIZE;
    }

    public void put(Integer val) {
        if (data.size() < MAX_SIZE) {
            data.add(val);
        } else {
            throw new BufferOverflowException();
        }
    }

    public Integer get() {
        if (data.size() > 0) {
            return data.pop();
        } else {
            throw new BufferUnderflowException();
        }
    }

    public boolean isEmpty() {
        return data.size() == 0;
    }

    public boolean isFull() {
        return data.size() == MAX_SIZE;
    }
}

package mpp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Integer.parseInt;
import static java.lang.System.exit;

public class Ex2q7 {
    public static class Consensus<T> {
        // the *only* field in this class
        private AtomicReference<T> reference = new AtomicReference<>(null);

        public T decide(T value) {
            reference.compareAndSet(null, value);
            return reference.get();
        }
    }

    public interface Operation<T> {
        Integer apply(T ds);
    }
    // public class QueueEnq implements Operation<MyQueue>
    // public class QueueDeq implements Operation<MyQueue>
    // etc.

    public static class UniversalConstruction<T> {
        private volatile OpNode<T>[] head;
        private volatile OpNode<T>[] current;
        private volatile T[] seqObject;

        public UniversalConstruction(int numThreads) {
            this.head = (OpNode<T>[]) new OpNode[numThreads];
            this.current = (OpNode<T>[]) new OpNode[numThreads];
            OpNode<T> tail = new OpNode<>(null);
            tail.seq = 1;
            for (int i = 0; i < numThreads; i++) {
                this.head[i] = tail;
                this.current[i] = tail;
            }
            this.seqObject = (T[]) new Object[numThreads];
        }

        // will be called exactly once for each thread
        public void init(T dsEmpty) {
            this.seqObject[getThreadId()] = dsEmpty;
        }

        public Integer execute(Operation<T> op) {
            OpNode<T> prefer = new OpNode<>(op);
            insertOpNode(prefer);
            return advanceSeqObject(prefer);
        }


        private Integer advanceSeqObject(OpNode<T> prefer) {
            int threadId = getThreadId();
            Integer returnVal = null;
            while (this.current[threadId] != prefer) {
                this.current[threadId] = this.current[threadId].next;
                returnVal = this.current[threadId].operation.apply(this.seqObject[threadId]);
            }
            return returnVal;
        }

        private void insertOpNode(OpNode<T> prefer) {
            int threadId = getThreadId();
            while (prefer.seq == 0) {
                OpNode<T> before = getMaxSeq(this.head);
                OpNode<T> after = before.decideNext.decide(prefer);
                before.next = after;
                after.seq = before.seq + 1;
                this.head[threadId] = after;
            }
        }

        private OpNode<T> getMaxSeq(OpNode<T>[] array) {
            OpNode<T> max = array[0];
            for (int i = 1; i < array.length; i++)
                if (max.seq < array[i].seq)
                    max = array[i];
            return max;
        }

        private int getThreadId() {
            return (int) Thread.currentThread().getId() & this.head.length;
        }

        private class OpNode<T> {
            public volatile Operation<T> operation;
            public volatile Consensus<OpNode<T>> decideNext = new Consensus<>();
            public volatile OpNode<T> next;
            public volatile int seq = 0;

            public OpNode(Operation<T> operation) {
                this.operation = operation;
            }
        }
    }

    interface MyQueue {
        void enqueue(Integer value);

        Integer dequeue();
    }

    public static class SerQueue implements MyQueue {
        private Node head, tail;

        public SerQueue() {
            this.head = this.tail = new Node(null);
        }

        @Override
        public void enqueue(Integer value) {
            this.head.next = new Node(value);
            this.head = this.head.next;
        }

        @Override
        public Integer dequeue() {
            if (this.tail.next != null) {
                this.tail = this.tail.next;
                return this.tail.value;
            } else {
                return null;
            }

        }

        private class Node {
            final public Integer value;
            public Node next = null;

            public Node(Integer value) {
                this.value = value;
            }
        }
    }

    public static class LockFreeQueue implements MyQueue {
        private final UniversalConstruction<SerQueue> uc;

        public LockFreeQueue(int numThreads) {
            uc = new UniversalConstruction<>(numThreads);
        }

        @Override
        public void enqueue(Integer value) {
            uc.execute(new Enqueue(value));
        }

        private class Enqueue implements Operation<SerQueue> {
            final Integer insertionValue;

            public Enqueue(Integer insertionValue) {
                this.insertionValue = insertionValue;
            }

            @Override
            public Integer apply(SerQueue ds) {
                ds.enqueue(this.insertionValue);
                return null;
            }
        }

        @Override
        public Integer dequeue() {
            return uc.execute(new Dequeue());
        }

        private class Dequeue implements Operation<SerQueue> {

            @Override
            public Integer apply(SerQueue ds) {
                return ds.dequeue();
            }
        }
    }

    public static class DeadlockFreeQueue implements MyQueue {

        private final ReentrantLock lock = new ReentrantLock();
        private final SerQueue q = new SerQueue();

        @Override
        public void enqueue(Integer value) {
            try {
                lock.lock();
                q.enqueue(value);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public Integer dequeue() {
            try {
                lock.lock();
                return q.dequeue();
            } finally {
                lock.unlock();
            }
        }
    }

    /*
    PRINTS THE THROUGHPUT WITH NEW LINE AND THAT'S IT
     */
    public static void main(String[] args) {
        assert args.length == 2;
        int threadsAmount = parseInt(args[0]);
        int implementation = parseInt(args[0]);
        final int EXECUTION_TIME_SECS = 10;
        final MyQueue queue;
        final boolean[] runThreads = {true};
        switch (implementation) {
            case 1:
                queue = new LockFreeQueue(threadsAmount);
                break;
            case 2:
                queue = new DeadlockFreeQueue();
                break;
            default:
                throw new Error("Invalid implementation arg" + implementation);
        }
        final OpsCounterThread[] threads = new OpsCounterThread[threadsAmount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new OpsCounterThread(runThreads,queue);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        try {
            Thread.sleep(EXECUTION_TIME_SECS * 1000);
            runThreads[0] = false;
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted: ");
            e.printStackTrace();
            exit(1);
        }
        double throughput = 0;
        for (OpsCounterThread thread : threads) {
            throughput += (thread.getOpsCount()/EXECUTION_TIME_SECS);
        }
        System.out.println(throughput);
    }

    static class OpsCounterThread extends Thread{
        private long opsCount=0;
        final boolean[] runThreads;
        final MyQueue queue;

        public OpsCounterThread(boolean[] runThreads, MyQueue queue) {
            this.runThreads = runThreads;
            this.queue = queue;
        }

        @Override
        public void run() {
            Random random = new Random();
            while (runThreads[0]) {
                if (this.opsCount % 2 == 0) {
                    int i = random.nextInt();
                    queue.enqueue(i);
                } else {
                    queue.dequeue();
                }
                this.opsCount++;
            }
        }

        public long getOpsCount() {
            return this.opsCount;
        }
    }
}

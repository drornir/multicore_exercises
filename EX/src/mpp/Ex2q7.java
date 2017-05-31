package mpp;

import java.util.concurrent.atomic.AtomicReference;

public class Ex2q7 {
    public class Consensus<T> {
        // the *only* field in this class
        private AtomicReference<T> reference = new AtomicReference<>(null);

        public T decide(T value) {
            reference.compareAndSet(null, value);
            return reference.get();
        }
    }

    public interface Operation<T> {
        Object apply(T ds);
    }
    // public class QueueEnq implements Operation<MyQueue>
    // public class QueueDeq implements Operation<MyQueue>
    // etc.

    public class UniversalConstruction<T> {
        private volatile OpNode<T>[] head;
        private volatile OpNode<T>[] current;
        private volatile OpNode<T> tail = new OpNode<>(null);
        private volatile T[] seqObject;

        public UniversalConstruction(int numThreads) {
            tail.seq = 1;
            this.head = (OpNode<T>[]) new OpNode[numThreads];
            this.current = (OpNode<T>[]) new OpNode[numThreads];
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

        public Object execute(Operation<T> op) {
            OpNode<T> prefer = new OpNode<>(op);
            insertOpNode(prefer);
            return advanceSeqObject(prefer);
        }


        private Object advanceSeqObject(OpNode<T> prefer) {
            int threadId = getThreadId();
            Object returnVal = null;
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

        public class OpNode<T> {
            public volatile Operation<T> operation;
            public volatile Consensus<OpNode<T>> decideNext = new Consensus<>();
            public volatile OpNode<T> next;
            public volatile int seq = 0;

            public OpNode(Operation<T> operation) {
                this.operation = operation;
            }
        }
    }

    public static void main(String[] args) {

    }
}

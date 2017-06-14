package mpp;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Ex3q8 {

    /**
     * prints results in csv format like this: threads, small_bo, large_bo, clh
     *
     * @param args first arg is string of threadsAmount
     */
    public static void main(String[] args) {
        final int threadsAmount = Integer.parseInt(args[0]);
        final int sbo = 16, lbo = 1024*1024, benchmarkTimes = 10;
        MyLock smallBackoff = new BackoffLock(1, sbo),
                largeBackoff = new BackoffLock(1, lbo),
                clh = new CLHLock();
        System.out.println(String.format(
                "%d, %d, %d, %d",
                threadsAmount,
                new Benchmark(smallBackoff, threadsAmount).benchmark(benchmarkTimes),
                new Benchmark(largeBackoff, threadsAmount).benchmark(benchmarkTimes),
                new Benchmark(clh, threadsAmount).benchmark(benchmarkTimes)
        ));
    }


    static class Benchmark {
        volatile long benchmarkCounter;
        final MyLock lock;
        final int threadsCount;
        final long MAX_CNT_VALUE = 1000000;

        Benchmark(MyLock lock, int threadsCount) {
            this.lock = lock;
            this.threadsCount = threadsCount;
            this.benchmarkCounter = 0;
        }

        public long benchmark(int times) {
            double avg = 0;
            for (int i = 0; i < times; i++) {
                avg += benchmark() / times;
            }
            return Math.round(avg);
        }

        /*
         * tries to get a shared counter up to a million as fast as possible, and measures how much ms it took
         */
        public long benchmark() {
            this.benchmarkCounter = 0;
            Thread[] threads = new Thread[threadsCount];
            for (int i = 0; i < threads.length; i++) {
                threads[i] = new Thread() {
                    @Override
                    public void run() {
                        boolean stop = false;
                        while (!stop) {
                            lock.lock();
                            try {
                                stop = ((++benchmarkCounter) >= MAX_CNT_VALUE);
                            } finally {
                                lock.unlock();
                            }
                        }
                    }
                };
            }
            StopWatch sw = new StopWatch();
            sw.start();
            for (Thread thread : threads) {
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return sw.end();
        }
    }

    static class StopWatch {
        private long s;

        void start() {
            s = System.currentTimeMillis();
        }

        long end() {
            return System.currentTimeMillis() - s;
        }
    }

    interface MyLock {
        void lock();

        void unlock();
    }

    public static class CLHLock implements MyLock {
        AtomicReference<QNode> tail;
        ThreadLocal<QNode> myPred, myNode;

        public CLHLock() {
            tail = new AtomicReference<>(new QNode());
            tail.get().locked = false;
            myPred = new ThreadLocal<QNode>() {
                @Override
                protected QNode initialValue() {
                    return new QNode();
                }
            };
            myNode = new ThreadLocal<QNode>() {
                @Override
                protected QNode initialValue() {
                    return new QNode();
                }
            };
        }

        public void lock() {
            QNode qnode = myNode.get();
            qnode.locked = true;
            QNode pred = tail.getAndSet(qnode);
            myPred.set(pred);
            while (pred.locked) ;
        }

        public void unlock() {
            QNode qnode = myNode.get();
            qnode.locked = false;
            myNode.set(myPred.get());
        }

    }

    public static class QNode {
        volatile boolean locked = true;

    }

    public static class BackoffLock implements MyLock {

        private AtomicBoolean state = new AtomicBoolean(false);
        private final int minDelay;
        private final int maxDelay;

        public BackoffLock(int minDelay, int maxDelay) {
            this.minDelay = minDelay;
            this.maxDelay = maxDelay;
        }

        public void lock() {
            Backoff backoff = new Backoff(minDelay, maxDelay);
            while (true) {
                while (state.get()) ;//test
                if (!state.getAndSet(true)) {//TAS
                    return;
                } else {
                    backoff.backoff();
                }
            }
        }

        public void unlock() {
            state.set(false);
        }

    }

    public static class Backoff { //this class is sequential
        final int minDelay, maxDelay;
        int limit;
        final Random random;

        public Backoff(int min, int max) {
            minDelay = min;
            maxDelay = max;
            limit = minDelay;
            random = new Random();
        }

        public void backoff() {
            int delay = random.nextInt(limit);
            limit = Math.min(maxDelay, 2 * limit);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new Error("Backoff Interrupted", e);
            }
        }
    }


}

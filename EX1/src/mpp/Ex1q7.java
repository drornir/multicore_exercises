package mpp;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Dror Nir on 05/05/2017.
 */
public class Ex1q7 {
    static int cnt = 0;
    static ReentrantLock lock = new ReentrantLock();

    public static void main(String[] args) {
        final int ITERATIONS = 1000000;
        int n;
        try {
            n = Integer.parseInt(args[0]);
        } catch (Exception e) {
            System.out.println("Wrong Input");
            return;
        }

        Thread[] threads = new Thread[n];

        long start = System.currentTimeMillis();

        // Initialize threads
        for (int i = 0; i < n; i++) {
            threads[i] = new Thread() {
                public void run() {
                    for (int i = 0; i < ITERATIONS; i++) {
                        int temp = cnt;
                        temp++;
                        lock.lock();
                        try { // just for good practice
                            cnt = temp;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            };
        }

        for (Thread t : threads) {
            t.start();
        }

        try {
            for (int i = 0; i < n; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
        }

        System.out.format("Time: %d,\t Counter: %d\n\n", (long) System.currentTimeMillis() - start, cnt);
    }
}

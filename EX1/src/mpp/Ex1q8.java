package mpp;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Dror Nir on 05/05/2017.
 */
public class Ex1q8 {
    static AtomicInteger cnt = new AtomicInteger();

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
                        cnt.incrementAndGet();
                    }
                }
            };
        }

        for (Thread t : threads) {
            t.run();
        }

        try {
            for (int i = 0; i < n; i++) {
                threads[i].join();
            }
        } catch (InterruptedException e) {
        }

        System.out.format("Time: %d,\t Counter: %d\n\n", (long) System.currentTimeMillis() - start, cnt.get());
    }
}

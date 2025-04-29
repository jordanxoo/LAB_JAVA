package zadDodatkowePorownanie;

import java.util.concurrent.*;
        import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPerformanceComparison {

    private static final int NUM_THREADS = 10000;
    private static AtomicInteger counter = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Porównanie wydajności różnych mechanizmów synchronizacji dla " + NUM_THREADS + " wątków");

        long cyclicBarrierTime = testWithCyclicBarrier();
        System.out.println("Czas wykonania z CyclicBarrier: " + cyclicBarrierTime + " ms");
        counter.set(0);

        long countDownLatchTime = testWithCountDownLatch();
        System.out.println("Czas wykonania z CountDownLatch: " + countDownLatchTime + " ms");

        counter.set(0);

        long whileLoopTime = testWithWhileLoop();
        System.out.println("Czas wykonania z pętlą while: " + whileLoopTime + " ms");

        System.out.println("\nPodsumowanie:");
        System.out.println("CyclicBarrier: " + cyclicBarrierTime + " ms");
        System.out.println("CountDownLatch: " + countDownLatchTime + " ms");
        System.out.println("Pętla while: " + whileLoopTime + " ms");
    }

    private static long testWithCyclicBarrier() {
        System.out.println("\nUruchamianie testu z CyclicBarrier...");

        final CyclicBarrier barrier = new CyclicBarrier(NUM_THREADS + 1);
        Thread[] threads = new Thread[NUM_THREADS];
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                counter.incrementAndGet();
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long testWithCountDownLatch() {
        System.out.println("\nUruchamianie testu z CountDownLatch...");

        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(NUM_THREADS);
        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startSignal.await();
                    counter.incrementAndGet();
                    doneSignal.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            threads[i].start();
        }

        long startTime = System.currentTimeMillis();
        startSignal.countDown();

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static long testWithWhileLoop() {
        System.out.println("\nUruchamianie testu z pętlą while...");

        Thread[] threads = new Thread[NUM_THREADS];
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i] = new Thread(() -> {
                counter.incrementAndGet();
            });
        }

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < NUM_THREADS; i++) {
            threads[i].start();
        }

        boolean allTerminated;
        do {
            allTerminated = true;
            for (Thread thread : threads) {
                if (thread.isAlive()) {
                    allTerminated = false;
                    break;
                }
            }
        } while (!allTerminated);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }
}
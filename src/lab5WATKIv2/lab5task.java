package lab5WATKIv2;

import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.sqrt;

public class lab5task {

    public static class calcTask implements Runnable {
        private final int name;
        private int result;
        private boolean isCompleted;
        private boolean isCancelled;
        private final int stop;

        public calcTask(int name, int stop) {
            this.name = name;
            this.stop = stop;
            this.result = 0;
            this.isCompleted = false;
            this.isCancelled = false;
        }

        @Override
        public void run() {
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("Thread " + name + " was interrupted");
                this.isCancelled = true;
                return;
            }
            for (int i = 0; i < stop; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Thread " + name + " was interrupted");
                    this.isCancelled = true;
                    return;
                }
                if (isPrime(i)) {
                    result++;
                }
            }
            isCompleted = true;
            System.out.println("Task " + name + " completed. Result: " + result);
        }

        public boolean isPrime(int n) {
            if (n < 2) {
                return false;
            }
            for (int i = 2; i < sqrt(n); i++) {
                if (n % i == 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return "Thread " + name + " Completed: " + isCompleted + " Cancelled: " + isCancelled + " Result: " + result;
        }
    }

    public static class calcTaskManager {
        private final Thread[] threads;
        private final calcTask[] tasks;
        private final Random random = new Random();

        public calcTaskManager(int numOfThreads) {
            threads = new Thread[numOfThreads];
            tasks = new calcTask[numOfThreads];
            for (int i = 0; i < numOfThreads; i++) {
                tasks[i] = new calcTask(i, random.nextInt(100_000_000));
                threads[i] = new Thread(tasks[i]);
            }
        }

        public void startAll() {
            for (Thread t : threads) {
                t.start();
            }
        }

        public void listAllTasks() {
            for (calcTask task : tasks) {
                System.out.println(task);
            }
        }

        public void cancelTask(int index) {
            if (index >= 0 && index < threads.length) {
                threads[index].interrupt();
                tasks[index].isCancelled = true;
            }
        }

        public void queryTaskStatus(int index) {
            if (index >= 0 && index < tasks.length) {
                System.out.println(tasks[index]);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        calcTaskManager manager = new calcTaskManager(10);
        manager.startAll();

        Scanner scanner = new Scanner(System.in);
        CountDownLatch exitLatch = new CountDownLatch(1);

        ScheduledExecutorService menuScheduler = Executors.newSingleThreadScheduledExecutor();
        menuScheduler.scheduleWithFixedDelay(() -> {
            System.out.println("\n=== MENU ===");
            System.out.println("1. List all tasks");
            System.out.println("2. Query task status");
            System.out.println("3. Cancel task");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            switch (choice) {
                case 1 -> manager.listAllTasks();
                case 2 -> {
                    System.out.print("Enter task index: ");
                    int idx = scanner.nextInt();
                    manager.queryTaskStatus(idx);
                }
                case 3 -> {
                    System.out.print("Enter task index: ");
                    int idx = scanner.nextInt();
                    manager.cancelTask(idx);
                    System.out.println("Cancelled task " + idx);
                }
                case 4 -> {
                    System.out.println("Exiting...");
                    exitLatch.countDown();
                }
                default -> System.out.println("Invalid choice");
            }
        }, 0, 100, TimeUnit.MILLISECONDS);

        exitLatch.await();
        menuScheduler.shutdownNow();
        for (Thread t : manager.threads) {
            t.interrupt();
        }
        scanner.close();
    }
}

package lab6WATKIv2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.sqrt;

public class lab6TaskManager {
    public static class FutureTaskManager<T> extends FutureTask<T> {
        private String taskName;

        public FutureTaskManager(Callable<T> callable, String taskName) {
            super(callable);
            this.taskName = taskName;
        }
        @Override
        protected void done() {
            if(Thread.currentThread().isInterrupted()) {
                System.out.println("Zadanie " + taskName + " zostalo przerwane.");
                return;
            }
            if(isCancelled()) {
                System.out.println("Zadanie " + taskName + " zostalo anulowane.");
                return;
            }
            if(isDone()) {
                try {
                    T result = get();
                    System.out.println("Zadanie " + taskName + " zakonczone, wynik: " + result);
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println("Blad w zadaniu " + taskName + ": " + e.getMessage());
                }
            }
        }

        @Override
        public String toString(){
            return "[FutureTask " + taskName + "]";
        }
    }

    static class primeSearchClass implements Callable<Integer>{
        private int start;
        private int end;

        public primeSearchClass(int start, int end) {
            this.start = start;
            this.end = end;
        }
        @Override
        public Integer call() throws Exception {
            List<Integer> primes = new ArrayList<>();
            int counter = 0;
            for (int i = start; i < end; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Zadanie zostało przerwane. Przeszukiwanie w zakresie: " + start + " - " + end);
                    return counter;
                }
                if (isPrime(i)){
                    primes.add(i);
                    counter++;
                }
            }
            return counter;
        }

        public Boolean isPrime(int n) {
            if (n < 2)
                return false;
            for (int i = 2; i <= sqrt(n); i++) {
                if (n % i == 0)
                    return false;
            }
            return true;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(3);
        Random r = new Random();
        Scanner scanner = new Scanner(System.in);
        ArrayList<FutureTaskManager<Integer>> tasks = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            tasks.add(new FutureTaskManager<>(new primeSearchClass(100000, r.nextInt(21000500)), "Zadanie " + i));
        }

        for (FutureTaskManager<Integer> task : tasks) {
            exec.execute(task);
        }

        AtomicBoolean running = new AtomicBoolean(true);
        CyclicBarrier barrier = new CyclicBarrier(1, () -> {
            try {
                if (!running.get()) {
                    return;
                }

                System.out.println("\n=== MENU ===");
                System.out.println("1. List all tasks");
                System.out.println("2. Get task result");
                System.out.println("3. Cancel task");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();

                switch (choice) {
                    case 1 -> {
                        for (int i = 0; i < tasks.size(); i++) {
                            FutureTaskManager<Integer> task = tasks.get(i);
                            System.out.println(i + ": " + task + " (Done: " + task.isDone() + ", Canceled: " + task.isCancelled() + ")");
                        }
                    }
                    case 2 -> {
                        System.out.print("Enter task index: ");
                        int index = scanner.nextInt();
                        if (index >= 0 && index < tasks.size()) {
                            FutureTaskManager<Integer> task = tasks.get(index);
                            if (task.isDone() && !task.isCancelled()) {
                                try {
                                    System.out.println("Result: " + task.get());
                                } catch (InterruptedException | ExecutionException e) {
                                    System.out.println("Error retrieving result: " + e.getMessage());
                                }
                            } else if (task.isCancelled()) {
                                System.out.println("Task was canceled.");
                            } else {
                                System.out.println("Task is still running.");
                            }
                        } else {
                            System.out.println("Invalid task index.");
                        }
                    }
                    case 3 -> {
                        System.out.print("Enter task index to cancel: ");
                        int index = scanner.nextInt();
                        if (index >= 0 && index < tasks.size()) {
                            FutureTaskManager<Integer> task = tasks.get(index);
                            task.cancel(false);
                            System.out.println("Task " + task + " canceled.");
                        } else {
                            System.out.println("Invalid task index.");
                        }
                    }
                    case 4 -> {
                        exec.shutdownNow();
                        running.set(false);
                        System.out.println("Exiting...");
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Wystąpił błąd: " + e.getMessage());
            }
        });
        while (running.get()) {
            try {
                barrier.await();
                Thread.sleep(100);
            } catch (BrokenBarrierException e) {
                System.out.println("Bariera została przerwana: " + e.getMessage());
                break;
            }
        }

        if (!exec.isShutdown()) {
            exec.shutdownNow();
        }
        scanner.close();
    }
}
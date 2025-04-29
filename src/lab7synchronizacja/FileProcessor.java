package lab7synchronizacja;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileProcessor {

    // odpowiada za koordynację zadań i danych
    public static class TaskBroker {
        private final String sourceFilePath;
        private final Lock queueLock = new ReentrantLock(); // Lock dla wewnętrznej "kolejki"
        private final Condition newDataSignal = queueLock.newCondition();
        private final Lock outputLock = new ReentrantLock(); // Współdzielony lock dla pliku wynikowego
        private final ExecutorService workerPool;
        private boolean sourceReadingDone = false;
        private List<String> equationList = new ArrayList<>();
        private List<Integer> sourceLineNumbers = new ArrayList<>();
        private int consumeIndex = 0; // Indeks dla konsumenta

        public TaskBroker(String filepath, ExecutorService executorService) {
            this.sourceFilePath = filepath;
            this.workerPool = executorService;
        }

        public void provideEquation(String equation, int lineNumber) {
            queueLock.lock();
            try {
                equationList.add(equation);
                sourceLineNumbers.add(lineNumber);
                newDataSignal.signal();
            } finally {
                queueLock.unlock();
            }
        }

        public EquationInfo fetchEquation() throws InterruptedException {
            queueLock.lock();
            try {
                while (consumeIndex >= equationList.size() && !sourceReadingDone) {
                    newDataSignal.await();
                }
                if (sourceReadingDone && consumeIndex >= equationList.size()) {
                    return null; // Koniec danych
                }
                String equation = equationList.get(consumeIndex);
                int lineNumber = sourceLineNumbers.get(consumeIndex);
                consumeIndex++;
                return new EquationInfo(equation, lineNumber);
            } finally {
                queueLock.unlock();
            }
        }

        public void signalInputComplete() {
            queueLock.lock();
            try {
                sourceReadingDone = true;
                newDataSignal.signalAll(); // budzi wszystkich czekających
            } finally {
                queueLock.unlock();
            }
        }

        public ExecutorService getWorkerPool() {
            return workerPool;
        }

        public Lock getOutputLock() {
            return outputLock;
        }

        public Future<?> initiateTasks() throws IOException {
            Path path = Paths.get(sourceFilePath);
            ensureInputFileExists(path);

            List<String> lines = Files.readAllLines(path);
            System.out.println("Odczytano " + lines.size() + " linii z pliku: " + sourceFilePath);

            LineProviderTask reader = new LineProviderTask(this, lines);
            workerPool.submit(reader);

            CalculationDispatcherTask dispatcher = new CalculationDispatcherTask(this, sourceFilePath);
            Future<?> dispatcherFuture = workerPool.submit(dispatcher);

            return dispatcherFuture;
        }

        private static void ensureInputFileExists(Path filePath) {
            if (!Files.exists(filePath)) {
                System.out.println("Tworzenie przykładowego pliku: " + filePath);
                // Przykładowe dane - można dostosować
                List<String> lines = List.of(
                        "2 + 3 * 4 =", "(10 - 5) * 3 =", "2 * 3 - (18 - 4) / 2 + 2^3 =",
                        "5 + 5 * 2 / (4 - 2) =", "3^2 + 4^2 =", "(7 + 3) * (6 - 2) =",
                        "10 / 2 + 15 / 3 =", "2^3 * 3 + 1 =", "20 - 4 * 3 + 2 =",
                        "(8 + 2 * 5) / (2 + 3) =", "1 / 0 =", "invalid format"
                );
                try {
                    Files.write(filePath, lines);
                } catch (IOException e) {
                    System.err.println("Nie udało się utworzyć pliku: " + e.getMessage());
                }
            }
        }
    }
    // Klasa przechowująca informacje o równaniu
    public static class EquationInfo {
        private final String equationText;
        private final int originalLine;
        public EquationInfo(String equation, int lineNumber) { this.equationText = equation; this.originalLine = lineNumber; }
        public String getEquationText() { return equationText; }
        public int getOriginalLine() { return originalLine; }
    }

    // zadanie odczytujące plik
    public static class LineProviderTask implements Runnable {
        private final TaskBroker broker;
        private final List<String> fileLines;
        public LineProviderTask(TaskBroker broker, List<String> lines) { this.broker = broker; this.fileLines = lines; }
        @Override
        public void run() {
            // System.out.println(Thread.currentThread().getName() + ": Rozpoczynam czytanie...");
            try {
                for (int i = 0; i < fileLines.size(); i++) {
                    String line = fileLines.get(i).trim();
                    if (!line.isEmpty() && line.contains("=")) {
                        broker.provideEquation(line, i);
                    }
                }
            } finally {
                broker.signalInputComplete();
                // System.out.println(Thread.currentThread().getName() + ": Zakończono czytanie.");
            }
        }
    }

    // FutureTask realizujący obliczenia i zapis
    public static class EquationUpdateTask extends FutureTask<Double> {
        private final String baseExpression;
        private final int lineNumber;
        private final Path targetPath;
        private final Lock writeLock; // Współdzielony lock

        public EquationUpdateTask(Callable<Double> callable, String equation, int lineNumber, String filePath, Lock outputLock) {
            super(callable);
            this.baseExpression = equation.replace("=", "").trim();
            this.lineNumber = lineNumber;
            this.targetPath = Paths.get(filePath);
            this.writeLock = outputLock;
        }
        @Override
        protected void done() {
            writeLock.lock();
            try {
                String lineToWrite;
                if (isCancelled()) {
                    lineToWrite = baseExpression + " = CANCELLED";
                } else {
                    try {
                        double result = get(); // Pobierz wynik
                        if (Double.isNaN(result)) {
                            lineToWrite = baseExpression + " = CALCULATION ERROR (NaN)";
                        } else {
                            lineToWrite = baseExpression + " = " + result;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        lineToWrite = baseExpression + " = INTERRUPTED";
                        System.err.println("Interrupted while getting result for line " + lineNumber);
                    } catch (ExecutionException e) {
                        lineToWrite = baseExpression + " = ERROR: " + e.getCause().getMessage();
                        System.err.println("Execution error for line " + lineNumber + ": " + baseExpression + " -> " + e.getCause());
                    } catch (Exception e) {
                        lineToWrite = baseExpression + " = UNEXPECTED ERROR";
                        System.err.println("Unexpected error in done() for line " + lineNumber + ": " + e);
                    }
                }
                // Zapisz wynik do pliku
                try {
                    List<String> currentLines = Files.readAllLines(targetPath);
                    if (lineNumber >= 0 && lineNumber < currentLines.size()) {
                        currentLines.set(lineNumber, lineToWrite);
                        Files.write(targetPath, currentLines);
                    } else {
                        System.err.println("Error writing file: Invalid line number " + lineNumber);
                    }
                } catch (IOException ioEx) {
                    System.err.println("I/O error updating file for line " + lineNumber + ": " + ioEx.getMessage());
                }
            } finally {
                writeLock.unlock();
            }
        }
    }

    // Callable wykonujące obliczenia RPN
    public static class RPNCalculator implements Callable<Double> {
        private final String rawEquation;
        public RPNCalculator(String equation) { this.rawEquation = equation; }

        @Override
        public Double call() throws Exception {
            try {
                String expression = rawEquation.replace("=", "").trim();
                if (expression.isEmpty()) throw new IllegalArgumentException("Empty expression");
                String rpn = infixToRPN(expression);
                return evaluateRPN(rpn);
            } catch (Exception e) {
                // Przekazujemy wyjątek dalej, aby ExecutionException miało przyczynę
                throw new Exception("Calculation failed for: " + rawEquation + " -> " + e.getMessage(), e);
            }
        }

        // Implementacja RPN - bez zmian logicznych
        private String infixToRPN(String infix) {
            StringBuilder output = new StringBuilder();
            Stack<Character> operators = new Stack<>();
            infix = infix.replaceAll("\\s+", "");
            StringBuilder numberBuilder = new StringBuilder();
            for (int i = 0; i < infix.length(); i++) {
                char c = infix.charAt(i);
                if (Character.isDigit(c) || c == '.') {
                    numberBuilder.append(c);
                    if (i == infix.length() - 1 || (!Character.isDigit(infix.charAt(i + 1)) && infix.charAt(i + 1) != '.')) {
                        output.append(numberBuilder).append(" ");
                        numberBuilder.setLength(0);
                    }
                } else if (c == '(') {
                    operators.push(c);
                } else if (c == ')') {
                    while (!operators.isEmpty() && operators.peek() != '(') output.append(operators.pop()).append(" ");
                    if (!operators.isEmpty() && operators.peek() == '(') operators.pop();
                    else throw new IllegalArgumentException("Mismatched parentheses");
                } else if (isOperator(c)) {
                    while (!operators.isEmpty() && operators.peek() != '(' && precedence(operators.peek()) >= precedence(c)) output.append(operators.pop()).append(" ");
                    operators.push(c);
                }
            }
            while (!operators.isEmpty()) {
                if (operators.peek() == '(') throw new IllegalArgumentException("Mismatched parentheses");
                output.append(operators.pop()).append(" ");
            }
            return output.toString().trim();
        }
        private double evaluateRPN(String rpn) {
            Stack<Double> stack = new Stack<>();
            String[] tokens = rpn.split("\\s+");
            for (String token : tokens) {
                if (token.isEmpty()) continue;
                if (token.length() == 1 && isOperator(token.charAt(0))) {
                    if (stack.size() < 2) throw new IllegalArgumentException("Insufficient operands for operator: " + token);
                    double b = stack.pop(); double a = stack.pop();
                    switch (token.charAt(0)) {
                        case '+': stack.push(a + b); break;
                        case '-': stack.push(a - b); break;
                        case '*': stack.push(a * b); break;
                        case '/': if (b == 0) throw new ArithmeticException("Division by zero"); stack.push(a / b); break;
                        case '^': stack.push(Math.pow(a, b)); break;
                        default: throw new IllegalArgumentException("Unknown operator: " + token);
                    }
                } else {
                    try { stack.push(Double.parseDouble(token)); }
                    catch (NumberFormatException e) { throw new IllegalArgumentException("Invalid token: " + token); }
                }
            }
            if (stack.size() != 1) throw new IllegalArgumentException("Invalid RPN expression, stack size: " + stack.size());
            return stack.pop();
        }
        private static boolean isOperator(char c) { return c == '+' || c == '-' || c == '*' || c == '/' || c == '^'; }
        private static int precedence(char operator) { switch (operator) { case '+': case '-': return 1; case '*': case '/': return 2; case '^': return 3; } return -1; }
    }

    // Zadanie pobierające z brokera i wysyłające do wykonania
    public static class CalculationDispatcherTask implements Runnable {
        private final TaskBroker broker;
        private final String targetFilePath;
        private final Lock outputLock;
        public CalculationDispatcherTask(TaskBroker broker, String filePath) {
            this.broker = broker;
            this.targetFilePath = filePath;
            this.outputLock = broker.getOutputLock();
        }
        @Override
        public void run() {
            // System.out.println(Thread.currentThread().getName() + ": Dispatcher starting...");
            try {
                while (true) {
                    EquationInfo data = broker.fetchEquation();
                    if (data == null) {
                        break; // Koniec danych
                    }
                    RPNCalculator calculator = new RPNCalculator(data.getEquationText());
                    EquationUpdateTask updateTask = new EquationUpdateTask(
                            calculator, data.getEquationText(), data.getOriginalLine(), targetFilePath, outputLock
                    );
                    broker.getWorkerPool().execute(updateTask);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Dispatcher task interrupted.");
            } catch (RejectedExecutionException e) {
                System.err.println("Dispatcher task could not submit calculation - pool likely shutting down.");
            }
        }
    }

    public static void main(String[] args) {
        String inputFile = "equations.txt";
        int coreCount = Runtime.getRuntime().availableProcessors();
        ExecutorService processingPool = Executors.newFixedThreadPool(coreCount);
        System.out.println("Processing pool started with " + coreCount + " threads.");

        TaskBroker broker = new TaskBroker(inputFile, processingPool);
        Future<?> dispatcherFuture = null;

        try {
            dispatcherFuture = broker.initiateTasks();

            if (dispatcherFuture != null) {
                try {
                    dispatcherFuture.get(); // Czekaj aż dispatcher skończy wysyłać zadania
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Main: Interrupted while waiting for dispatcher.");
                } catch (ExecutionException e) {
                    System.err.println("Main: Execution error in dispatcher task: " + e.getCause());
                }
            }
        } catch (IOException e) {
            System.err.println("Main: IO Error during task initiation: " + e.getMessage());
            processingPool.shutdownNow();
            return;
        }

        System.out.println("Main: wszystkie zadania zostały wysłane do przetworzenia.");
        processingPool.shutdown();
        try {
            System.out.println("Main: czekanie na zakończenie przetwarzania...");
            if (!processingPool.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println("nie udało się zakończyć przetwarzania w czasie 5 minut.");
                List<Runnable> remaining = processingPool.shutdownNow();
                System.err.println("nierozpoczete zadania: " + remaining.size());
            } else {
                System.out.println("Main: przetwarzanie zakończone pomyślnie.");
            }
        } catch (InterruptedException e) {
            System.err.println("Main: przerwano oczekiwanie na zakończenie przetwarzania.");
            processingPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Koniec.");
    }
}
package lab8TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientServer {

    public static class Question {
        private final String text;
        private final List<String> options;
        private final List<Integer> correctAnswers;

        public Question(String text, List<String> options, List<Integer> correctAnswers) {
            this.text = text;
            this.options = options;
            this.correctAnswers = correctAnswers;
        }
        public String getText() {
            return text;
        }

        public List<String> getOptions() {
            return options;
        }
        public List<Integer> getCorrectAnswers() {
            return correctAnswers;
        }
    }
    public static class Server {
        private final static int MAX_STUDENTS = 250;
        private static final int RESPONSE_TIME = 30;
        private static List<Question> questions = new ArrayList<>();
        private static final Lock resultLock = new ReentrantLock();
        private static final Lock responseLock = new ReentrantLock();
        private static AtomicInteger activeStudents = new AtomicInteger(0);
        private static ExecutorService executor;
        private static int serverPort;
        private static void loadConfig() {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream("server_config.txt"));
                serverPort = Integer.parseInt(properties.getProperty("port"));
            } catch (IOException e) {
                serverPort = 8888;
                System.out.println("Nie udało się wczytać konfiguracji");
                System.out.println("Używam domyślnego portu: " + serverPort);
            }
        }
        private static void loadQuestions() {
            try {
                List<String> lines = Files.readAllLines(Paths.get("bazaPytan.txt"));
                for (int i = 0; i < lines.size(); i += 6) {
                    if (i + 5 < lines.size()) {
                        String questionText = lines.get(i);
                        List<String> options = new ArrayList<>();
                        for (int j = 1; j <= 4; j++) {
                            options.add(lines.get(i + j));
                        }
                        String correctAnswersLine = lines.get(i + 5);
                        List<Integer> correctAnswers = new ArrayList<>();
                        for (String s : correctAnswersLine.split(",")) {
                            correctAnswers.add(Integer.parseInt(s.trim()));
                        }

                        questions.add(new Question(questionText, options, correctAnswers));
                    }
                }
                System.out.println("Wczytano: " + questions.size() + " pytań");
            } catch (IOException e) {
                System.out.println("Nie udało się wczytać pytań, błąd: " + e.getMessage());
            }
        }
        public static void main(String[] args) {
            loadConfig();
            loadQuestions();
            executor = Executors.newFixedThreadPool(MAX_STUDENTS);
            try {
                ServerSocket serverSocket = new ServerSocket(serverPort);
                System.out.println("Server uruchomiony na porcie: " + serverPort);

                while (true) {
                    if (activeStudents.get() < MAX_STUDENTS) {
                        Socket clientSocket = serverSocket.accept();
                        activeStudents.incrementAndGet();
                        executor.submit(new TestSession(clientSocket));
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (IOException | InterruptedException e) {
                System.out.println("Błąd servera " + e.getMessage());
            } finally {
                executor.shutdown();
            }
        }
        static class TestSession implements Runnable {
            private final Socket socket;
            private PrintWriter out;
            private BufferedReader in;
            private String studentID;

            public TestSession(Socket socket) {
                this.socket = socket;
            }
            @Override
            public void run() {
                try {
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println("STUDENT_ID");
                    studentID = in.readLine();
                    List<String> studentAnswers = new ArrayList<>();
                    int score = 0;
                    for (int i = 0; i < questions.size(); i++) {
                        Question question = questions.get(i);

                        out.println("QUESTION:" + (i + 1) + "/" + questions.size());
                        out.println(question.getText());

                        for (int j = 0; j < question.getOptions().size(); j++) {
                            out.println((j + 1) + ". " + question.getOptions().get(j));
                        }
                        out.println("QUESTION_END");
                        socket.setSoTimeout(RESPONSE_TIME * 1000);
                        try {
                            String answer = in.readLine();

                            if (answer == null || answer.equalsIgnoreCase("REZYGNACJA")) {
                                studentAnswers.add("BRAK_ODPOWIEDZI");
                                break;
                            } else {
                                studentAnswers.add(answer);

                                try {
                                    Set<Integer> studentAnswerSet = new HashSet<>();
                                    for (String answerPart : answer.split(",")) {
                                        int answerNum = Integer.parseInt(answerPart.trim());
                                        if (answerNum > 0 && answerNum <= 4) {
                                            studentAnswerSet.add(answerNum);
                                        }
                                    }
                                    Set<Integer> correctAnswersSet = new HashSet<>(question.getCorrectAnswers());

                                    if (studentAnswerSet.equals(correctAnswersSet)) {
                                        score++;
                                    }
                                } catch (NumberFormatException e) {
                                }
                            }
                        } catch (SocketTimeoutException e) {
                            studentAnswers.add("TIMEOUT");
                        }
                    }
                    responseLock.lock();
                    try {
                        PrintWriter responseWriter = new PrintWriter(new FileWriter("bazaOdpowiedzi.txt", true));
                        responseWriter.println("Student ID: " + studentID);
                        responseWriter.println("Czas testu: " + new Date());

                        for (int i = 0; i < studentAnswers.size(); i++) {
                            responseWriter.println("Pytanie " + (i + 1) + ": " + studentAnswers.get(i));
                        }
                        responseWriter.println("----------------");
                        responseWriter.close();
                    } finally {
                        responseLock.unlock();
                    }
                    resultLock.lock();
                    try {
                        PrintWriter resultWriter = new PrintWriter(new FileWriter("wyniki.txt", true));
                        resultWriter.println("Student ID: " + studentID);
                        resultWriter.println("Wynik: " + score + "/" + questions.size());
                        resultWriter.println("Czas testu: " + new Date());
                        resultWriter.println("-------------");
                        resultWriter.close();
                    } finally {
                        resultLock.unlock();
                    }
                    out.println("TEST_COMPLETE");
                    out.println("Twój wynik: " + score + "/" + questions.size());

                } catch (IOException e) {
                    System.out.println("Bład sesji: " + e.getMessage());
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Błąd zamykania połączenia");
                    }
                    activeStudents.decrementAndGet();
                }
            }
        }
    }
    public static class Client {
        private static String serverIP;
        private static int serverPort;
        private static Scanner scanner = new Scanner(System.in);

        public static void main(String[] args) {
            loadConfig();

            try {
                Socket socket = new Socket(serverIP, serverPort);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("Połączono z serverem kolokwium.");
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("STUDENT_ID")) {
                        System.out.print("Podaj swój numer indeksu: ");
                        String studentID = scanner.nextLine();
                        out.println(studentID);
                    } else if (inputLine.startsWith("QUESTION:")) {
                        System.out.println("\nPytanie " + inputLine.substring(9));
                        StringBuilder questionContent = new StringBuilder();
                        String line;
                        while (!(line = in.readLine()).equals("QUESTION_END")) {
                            questionContent.append(line).append("\n");
                        }
                        System.out.println(questionContent);

                        System.out.println("Wybierz numery poprawnych odpowiedzi (np. 1,2,4) lub wpisz 'rezygnacja' aby zakończyć test: ");
                        String answer = scanner.nextLine();
                        if (answer.equalsIgnoreCase("rezygnacja")) {
                            out.println("REZYGNACJA");
                            System.out.println("Zrezygnowano z dalszego rozwiązywania testu");
                        } else {
                            out.println(answer);
                        }
                    } else if (inputLine.equalsIgnoreCase("TEST_COMPLETE")) {
                        String result = in.readLine();
                        System.out.println("\nTest Zakończony!");
                        System.out.println(result);
                        break;
                    } else {
                        System.out.println(inputLine);
                    }
                }
                socket.close();
            } catch (UnknownHostException e) {
                System.out.println("Nieznany host: " + serverIP);
            } catch (IOException e) {
                System.out.println("Błąd I/O podczas połączenia z serverem: " + e.getMessage());
            }
        }

        private static void loadConfig() {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream("client_config.txt"));
                serverIP = properties.getProperty("server_ip", "localhost");
                serverPort = Integer.parseInt(properties.getProperty("port", "8888"));
            } catch (IOException e) {
                System.out.println("Nie można wczytać konfiguracji, używam domyślnej konfiguracji");
                serverPort = 8888;
                serverIP = "localhost";
            }
        }
    }
}
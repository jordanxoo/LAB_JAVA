package lab9JDBC;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientServerWithDB {

    public static class Question {
        private final String question;
        private final List<String> options;
        private final List<Integer> answers;

        public Question(String question, List<String> options, List<Integer> answers) {
            this.question = question;
            this.options = options;
            this.answers = answers;
        }

        public String getQuestion() {
            return question;
        }

        public List<String> getOptions() {
            return options;
        }

        public List<Integer> getAnswers() {
            return answers;
        }
    }

    public static class Server {
        private static final int MAX_STUDENTS = 250;
        private static final int RESPONSE_TIME = 30; // seconds
        private static ExecutorService executor;
        private static final Lock answersLock = new ReentrantLock();
        private static final Lock resultLock = new ReentrantLock();
        private static final List<Question> questions = new ArrayList<>();
        private static int portNumber;

        public static void main(String[] args) {
            DatabaseManager.initializeDatabase();
            loadConfig();
            loadQuestions();
            executor = Executors.newFixedThreadPool(MAX_STUDENTS);

            try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
                System.out.println("Serwer uruchomiony na porcie " + portNumber);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(new testSession(clientSocket));
                }
            } catch (IOException e) {
                System.err.println("Błąd serwera: " + e.getMessage());
            } finally {
                if (executor != null) executor.shutdown();
            }
        }

        private static void saveResultsToDatabase(String studentID, List<String> studentAnswers, int score) {
            try(Connection conn = DatabaseManager.getConnection()) {
                try(PreparedStatement pstmt = conn.prepareStatement(
                        "INSERT INTO results (student_id, test_time, score, total_questions) VALUES (?,?,?,?)")) {
                    pstmt.setString(1, studentID);
                    pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    pstmt.setInt(3, score);
                    pstmt.setInt(4, questions.size());
                    pstmt.executeUpdate();
                }

                try(PreparedStatement pstmtAnswers = conn.prepareStatement(
                        "INSERT INTO answers (student_id, question_id, answer) VALUES (?,?,?)")) {
                    for (int i = 0; i < studentAnswers.size(); i++) {
                        pstmtAnswers.setString(1, studentID);
                        pstmtAnswers.setInt(2, i + 1);
                        pstmtAnswers.setString(3, studentAnswers.get(i));
                        pstmtAnswers.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                System.out.println("Błąd podczas zapisu do bazy danych: " + e.getMessage());
            }
        }
        private static void loadConfig() {
            try (InputStream in = new FileInputStream("server_config.txt")) {
                Properties props = new Properties();
                props.load(in);
                portNumber = Integer.parseInt(props.getProperty("port", "8888"));
            } catch (IOException e) {
                System.out.println("Nie można wczytać konfiguracji, używam portu 8888");
                portNumber = 8888;
            }
        }

        private static void loadQuestions() {
            try (Connection conn = DatabaseManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM questions"))
            {
                while (rs.next()) {
                    String question = rs.getString("question");
                    List<String> options = Arrays.asList(rs.getString("options").split("\n"));
                    List<Integer> answers = new ArrayList<>();
                    for (String ans : rs.getString("answers").split(",")) {
                        answers.add(Integer.parseInt(ans.trim()));
                    }
                    questions.add(new Question(question, options, answers));
                }
                if (questions.isEmpty()) {
                    System.out.println("Brak pytań w bazie. Ładowanie z pliku...");
                    DatabaseManager.loadQuestionsFromFile("bazaPytan.txt");
                    loadQuestions();
                }
            } catch (SQLException e) {
                System.out.println("Błąd podczas ładowania pytań z bazy danych: " + e.getMessage());
            }
        }
        static class testSession implements Runnable {
            private final Socket socket;
            private PrintWriter out;
            private BufferedReader in;
            private static String studentID;

            testSession(Socket socket) {
                this.socket = socket;
            }
            public static String getStudentID()
            {
                return studentID;
            }

            @Override
            public void run() {
                try {
                    socket.setSoTimeout(RESPONSE_TIME * 1000);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("STUDENT_ID");
                    studentID = in.readLine();

                    List<String> studentAnswers = new ArrayList<>();
                    int score = 0;

                    for (int i = 0; i < questions.size(); i++) {
                        Question q = questions.get(i);
                        out.println("QUESTION " + (i + 1) + "/" + questions.size());
                        out.println(q.getQuestion());
                        for (int j = 0; j < q.getOptions().size(); j++) {
                            out.println((j + 1) + ". " + q.getOptions().get(j));
                        }
                        out.println("QUESTION_END");

                        String answer;
                        try {
                            answer = in.readLine();
                        } catch (SocketTimeoutException ste) {
                            answer = "TIMEOUT";
                            out.println("Przekroczono 30 sekund na odpowiedź. Przechodzę dalej.");
                        }
                        if (answer == null || answer.equalsIgnoreCase("TIMEOUT")) {
                            studentAnswers.add("TIMEOUT");
                            continue;
                        }
                        if (answer.equalsIgnoreCase("REZYGNACJA")) {
                            studentAnswers.add("BRAK_ODPOWIEDZI");
                            break;
                        }
                        studentAnswers.add(answer);

                        try {
                            Set<Integer> studentSet = new HashSet<>();
                            for (String part : answer.split(",")) studentSet.add(Integer.parseInt(part.trim()));
                            if (studentSet.equals(new HashSet<>(q.getAnswers()))) score++;
                        } catch (NumberFormatException ignored) {}
                    }

                    out.println("TEST_COMPLETE");
                    out.println("Twój wynik: " + score + "/" + questions.size());

                    saveResultsToDatabase(studentID, studentAnswers, score);
//                    answersLock.lock();
//                    try (PrintWriter w = new PrintWriter(new FileWriter("bazaOdpowiedzi.txt", true))) {
//                        w.println("STUDENT ID: " + studentID);
//                        w.println("CZAS TESTU: " + new Date());
//                        for (int i = 0; i < studentAnswers.size(); i++)
//                            w.println("Pytanie " + (i + 1) + ": " + studentAnswers.get(i));
//                        w.println("--------------------------");
//                    } finally {
//                        answersLock.unlock();
//                    }
//                    resultLock.lock();
//                    try (PrintWriter w = new PrintWriter(new FileWriter("wyniki.txt", true))) {
//                        w.println("STUDENT ID: " + studentID);
//                        w.println("CZAS TESTU: " + new Date());
//                        w.println("Wynik: " + score + "/" + questions.size());
//                        w.println("-------------------------");
//                    } finally {
//                        resultLock.unlock();
//                    }

                } catch (IOException e) {
                    System.err.println("Błąd sesji: " + e.getMessage());
                } finally {
                    try { socket.close(); } catch (IOException ignored) {}
                }
            }
        }
    }

    public static class Client {
        private static String serverIP;
        private static int serverPort;
        private static final int RESPONSE_TIME = 30; // seconds

        public static void main(String[] args) {
            DatabaseManager.initializeDatabase();
            loadConfig();
            ExecutorService inputExecutor = Executors.newSingleThreadExecutor();
            try (Socket socket = new Socket(serverIP, serverPort);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 Scanner scanner = new Scanner(System.in)) {

                System.out.println("Połączono z serwerem " + serverIP + ":" + serverPort);
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.equals("STUDENT_ID")) {
                        System.out.print("Podaj numer indeksu: ");
                        out.println(scanner.nextLine());

                    } else if (inputLine.startsWith("QUESTION")) {
                        System.out.println("\n" + inputLine);
                        while (!(inputLine = in.readLine()).equals("QUESTION_END")) {
                            System.out.println(inputLine);
                        }
                        System.out.println("Masz " + RESPONSE_TIME + " sekund na odpowiedź (lub wpisz REZYGNACJA)");
                        Future<String> future = inputExecutor.submit(scanner::nextLine);
                        String answer;
                        try {
                            answer = future.get(RESPONSE_TIME, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            future.cancel(true);
                            answer = "TIMEOUT";
                            System.out.println("Upłynął czas na odpowiedź. Przechodzę do następnego pytania.");
                        }
                        out.println(answer);

                    } else if (inputLine.equals("TEST_COMPLETE")) {
                        System.out.println("Test zakończony.");
                        System.out.println(in.readLine());
                        break;
                    } else {
                        System.out.println(inputLine);
                    }
                }
            } catch (IOException e) {
                System.err.println("Błąd klienta: " + e.getMessage());
            } finally {
                inputExecutor.shutdownNow();
            }
        }

        private static void loadConfig() {
            try (InputStream is = new FileInputStream("client_config.txt")) {
                Properties props = new Properties();
                props.load(is);
                serverIP = props.getProperty("serverIP", "localhost");
                serverPort = Integer.parseInt(props.getProperty("port", "8888"));
            } catch (IOException e) {
                System.out.println("Nie można wczytać konfiguracji, używam localhost:8888");
                serverIP = "localhost";
                serverPort = 8888;
            }
        }
    }
}

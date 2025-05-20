package lab9JDBC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL="jdbc:mysql://localhost:3306/";
    private static final String DB_Name = "test_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(DB_URL + DB_Name, DB_USER,DB_PASSWORD);

    }
    public static void initializeDatabase()
    {
        try(Connection connection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASSWORD);
            Statement statement = connection.createStatement()) {

            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_Name);

            try (Connection dbConn = getConnection()){
                Statement dbStmt = dbConn.createStatement();

                dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS questions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "question TEXT, " +
                        "options TEXT, " +
                        "answers TEXT)");

                dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS results (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "student_id VARCHAR(50), " +
                        "test_time TIMESTAMP, " +
                        "score INT, " +
                        "total_questions INT)");

                dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS answers (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "student_id VARCHAR(50), " +
                        "question_id INT, " +
                        "answer TEXT, " +
                        "FOREIGN KEY (question_id) REFERENCES questions(id))");
            }

        }catch (SQLException e)
        {
            System.out.println("Bład inicializacji bazy danych: " + e.getMessage());
        }
    }

    public static void loadQuestionsFromFile(String filePath)
    {
        try(Connection conn = getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO questions (question,options,answers) VALUES (?,?,?)");
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null)
            {
                String question = line;
                StringBuilder options = new StringBuilder();
                for (int i=0;i<4;i++)
                {
                    options.append(reader.readLine()).append("\n");
                }
                String answers = reader.readLine();
                pstmt.setString(1,question);
                pstmt.setString(2,options.toString().trim());
                pstmt.setString(3,answers);

                pstmt.executeUpdate();
            }
        }catch (SQLException | IOException e)
        {
            System.out.println("Błąd podczas ładowania pytan z bazy danych: " + e.getMessage());
        }
    }



}

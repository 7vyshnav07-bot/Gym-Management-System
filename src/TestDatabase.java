import java.sql.Connection;
import db.DatabaseConnection;

public class TestDatabase {

    public static void main(String[] args) {

        try {
            Connection connection =
                    DatabaseConnection.getConnection();

            System.out.println("DATABASE CONNECTION SUCCESS");

            connection.close();

        } catch (Exception e) {

            System.out.println("DATABASE CONNECTION FAILED");

            e.printStackTrace();
        }
    }
}
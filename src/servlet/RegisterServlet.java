package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import security.PasswordUtil;
import db.DatabaseConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {

            // Read JSON request body
            StringBuilder json = new StringBuilder();

            BufferedReader reader =
                    request.getReader();

            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            String jsonData = json.toString();

            // Extract registration details
            String fullName =
                    extractJsonValue(jsonData, "fullName");

            String username =
                    extractJsonValue(jsonData, "username");

            String email =
                    extractJsonValue(jsonData, "email");

            String phone =
                    extractJsonValue(jsonData, "phone");

            String dateOfBirth =
                    extractJsonValue(jsonData, "dateOfBirth");

            String country =
                    extractJsonValue(jsonData, "country");

            String gender =
                    extractJsonValue(jsonData, "gender");

            String password =
                    extractJsonValue(jsonData, "password");

            // Basic validation
            if (fullName == null || fullName.trim().isEmpty()
                    || username == null || username.trim().isEmpty()
                    || email == null || email.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()
                    || password == null || password.isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "Required registration fields are missing.");

                return;
            }

            username = username.trim();

            // Insert user into database
            String sql =
                    "INSERT INTO users " +
                    "(full_name, username, email, phone, " +
                    "date_of_birth, country, gender, password) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(1, fullName);
                statement.setString(2, username);
                statement.setString(3, email);
                statement.setString(4, phone);
                statement.setString(5, dateOfBirth);
                statement.setString(6, country);
                statement.setString(7, gender);
                statement.setString(8, PasswordUtil.hashPassword(password));

                statement.executeUpdate();
            }

            response.setStatus(
                    HttpServletResponse.SC_OK);

            response.getWriter().write(
                    "Registration successful!");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "Registration failed: "
                    + e.getMessage());
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("index.html");
    }

    private String extractJsonValue(
            String json,
            String key) {

        String search =
                "\"" + key + "\"";

        int keyIndex =
                json.indexOf(search);

        if (keyIndex == -1) {
            return null;
        }

        int colonIndex =
                json.indexOf(
                        ":",
                        keyIndex);

        if (colonIndex == -1) {
            return null;
        }

        int startQuote =
                json.indexOf(
                        "\"",
                        colonIndex);

        if (startQuote == -1) {
            return null;
        }

        int endQuote =
                json.indexOf(
                        "\"",
                        startQuote + 1);

        if (endQuote == -1) {
            return null;
        }

        return json.substring(
                startQuote + 1,
                endQuote);
    }
}
package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import db.DatabaseConnection;
import service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = new UserService();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // Read JSON request body
            StringBuilder json = new StringBuilder();

            BufferedReader reader = request.getReader();

            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            String jsonData = json.toString();

            // Extract username and password
            String username =
                    extractJsonValue(jsonData, "username");

            String password =
                    extractJsonValue(jsonData, "password");
System.out.println("RECEIVED USERNAME: [" + username + "]");
System.out.println("PASSWORD RECEIVED: " + (password != null));
            // Server-side validation
            if (username == null || username.trim().isEmpty()
                    || password == null || password.isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "Username and password are required.");

                return;
            }

            username = username.trim();

            // Authenticate user
            boolean authenticated =
                    userService.authenticateUser(
                            username,
                            password);

            if (!authenticated) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.getWriter().write(
                        "Invalid username or password.");

                return;
            }

            // Get complete user information
            String sql =
                    "SELECT full_name, username, email, phone, " +
                    "date_of_birth, country, gender " +
                    "FROM users WHERE username = ?";

            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(1, username);

                ResultSet resultSet =
                        statement.executeQuery();

                if (resultSet.next()) {

                    String fullName =
                            resultSet.getString("full_name");

                    String email =
                            resultSet.getString("email");

                    String phone =
                            resultSet.getString("phone");

                    String dateOfBirth =
                            resultSet.getString("date_of_birth");

                    String country =
                            resultSet.getString("country");

                    String gender =
                            resultSet.getString("gender");

                    // Create HttpSession
                    HttpSession session =
                            request.getSession(true);

                    session.setAttribute(
                            "username",
                            username);

                    session.setAttribute(
                            "fullName",
                            fullName);

                    session.setAttribute(
                            "email",
                            email);

                    // Return user information as JSON
                    String jsonResponse =
                            "{"
                            + "\"fullName\":\""
                            + escapeJson(fullName)
                            + "\","

                            + "\"username\":\""
                            + escapeJson(username)
                            + "\","

                            + "\"email\":\""
                            + escapeJson(email)
                            + "\","

                            + "\"phone\":\""
                            + escapeJson(phone)
                            + "\","

                            + "\"dateOfBirth\":\""
                            + escapeJson(dateOfBirth)
                            + "\","

                            + "\"country\":\""
                            + escapeJson(country)
                            + "\","

                            + "\"gender\":\""
                            + escapeJson(gender)
                            + "\""
                            + "}";

                    response.setStatus(
                            HttpServletResponse.SC_OK);

                    response.getWriter().write(
                            jsonResponse);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "Login server error: "
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

    // Extract value from simple JSON
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

    // Basic JSON escaping
    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
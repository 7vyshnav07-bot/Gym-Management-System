package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import db.DatabaseConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet("/update-profile")
public class UpdateProfileServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {

            // Get logged-in user's session
            HttpSession session =
                    request.getSession(false);

            if (session == null ||
                    session.getAttribute("username") == null) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.getWriter().write(
                        "Please login first.");

                return;
            }

            String currentUsername =
                    (String) session.getAttribute("username");

            // Read JSON request
            StringBuilder json =
                    new StringBuilder();

            BufferedReader reader =
                    request.getReader();

            String line;

            while ((line = reader.readLine()) != null) {
                json.append(line);
            }

            String jsonData =
                    json.toString();

            // Extract profile information
            String fullName =
                    extractJsonValue(
                            jsonData,
                            "fullName");

            String email =
                    extractJsonValue(
                            jsonData,
                            "email");

            String phone =
                    extractJsonValue(
                            jsonData,
                            "phone");

            String dateOfBirth =
                    extractJsonValue(
                            jsonData,
                            "dateOfBirth");

            String country =
                    extractJsonValue(
                            jsonData,
                            "country");

            String gender =
                    extractJsonValue(
                            jsonData,
                            "gender");

            String password =
                    extractJsonValue(
                            jsonData,
                            "password");

            // Basic validation
            if (fullName == null ||
                    fullName.trim().isEmpty() ||
                    email == null ||
                    email.trim().isEmpty() ||
                    phone == null ||
                    phone.trim().isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "Required profile fields are missing.");

                return;
            }

            String sql;

            boolean changePassword =
                    password != null &&
                    !password.trim().isEmpty();

            if (changePassword) {

                sql =
                    "UPDATE users SET " +
                    "full_name = ?, " +
                    "email = ?, " +
                    "phone = ?, " +
                    "date_of_birth = ?, " +
                    "country = ?, " +
                    "gender = ?, " +
                    "password = ? " +
                    "WHERE username = ?";

            } else {

                sql =
                    "UPDATE users SET " +
                    "full_name = ?, " +
                    "email = ?, " +
                    "phone = ?, " +
                    "date_of_birth = ?, " +
                    "country = ?, " +
                    "gender = ? " +
                    "WHERE username = ?";
            }

            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        fullName.trim());

                statement.setString(
                        2,
                        email.trim());

                statement.setString(
                        3,
                        phone.trim());

                statement.setString(
                        4,
                        dateOfBirth);

                statement.setString(
                        5,
                        country);

                statement.setString(
                        6,
                        gender);

                if (changePassword) {

                    statement.setString(
                            7,
                            password);

                    statement.setString(
                            8,
                            currentUsername);

                } else {

                    statement.setString(
                            7,
                            currentUsername);
                }

                int rows =
                        statement.executeUpdate();

                if (rows == 0) {

                    response.setStatus(
                            HttpServletResponse.SC_NOT_FOUND);

                    response.getWriter().write(
                            "User profile not found.");

                    return;
                }
            }

            // Update session information
            session.setAttribute(
                    "fullName",
                    fullName.trim());

            session.setAttribute(
                    "email",
                    email.trim());

            response.setStatus(
                    HttpServletResponse.SC_OK);

            response.getWriter().write(
                    "Profile updated successfully!");

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "Profile update failed: "
                    + e.getMessage());
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect("profile.html");
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
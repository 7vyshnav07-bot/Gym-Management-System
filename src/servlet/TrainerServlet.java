package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import db.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/trainers")
public class TrainerServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            HttpSession session =
                    request.getSession(false);

            if (session == null ||
                    session.getAttribute("username") == null) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Please login first.\"}"
                );

                return;
            }

            String sql =
                    "SELECT id, trainer_name, specialization, " +
                    "experience_years, phone, availability, description " +
                    "FROM trainers " +
                    "ORDER BY id ASC";

            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
            ) {

                StringBuilder json =
                        new StringBuilder();

                json.append(
                        "{\"success\":true,\"trainers\":["
                );

                boolean first = true;

                while (resultSet.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;

                    json.append("{");

                    json.append("\"id\":")
                            .append(
                                    resultSet.getInt("id")
                            )
                            .append(",");

                    json.append("\"trainerName\":\"")
                            .append(
                                    escapeJson(
                                            resultSet.getString(
                                                    "trainer_name"
                                            )
                                    )
                            )
                            .append("\",");

                    json.append("\"specialization\":\"")
                            .append(
                                    escapeJson(
                                            resultSet.getString(
                                                    "specialization"
                                            )
                                    )
                            )
                            .append("\",");

                    json.append("\"experienceYears\":")
                            .append(
                                    resultSet.getInt(
                                            "experience_years"
                                    )
                            )
                            .append(",");

                    json.append("\"phone\":\"")
                            .append(
                                    escapeJson(
                                            resultSet.getString(
                                                    "phone"
                                            )
                                    )
                            )
                            .append("\",");

                    json.append("\"availability\":\"")
                            .append(
                                    escapeJson(
                                            resultSet.getString(
                                                    "availability"
                                            )
                                    )
                            )
                            .append("\",");

                    json.append("\"description\":\"")
                            .append(
                                    escapeJson(
                                            resultSet.getString(
                                                    "description"
                                            )
                                    )
                            )
                            .append("\"");

                    json.append("}");
                }

                json.append("]}");

                response.getWriter().write(
                        json.toString()
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\"Failed to load trainers: "
                    + escapeJson(
                            e.getMessage()
                    )
                    + "\""
                    + "}"
            );
        }
    }


    // ==========================================
    // REQUEST TRAINER
    // ==========================================

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            HttpSession session =
                    request.getSession(false);

            if (session == null ||
                    session.getAttribute("username") == null) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Please login first.\"}"
                );

                return;
            }

            String username =
                    (String) session.getAttribute(
                            "username"
                    );


            // ------------------------------------------
            // READ TRAINER ID
            // ------------------------------------------

            String trainerIdText =
                    request.getParameter(
                            "trainerId"
                    );

            if (trainerIdText == null ||
                    trainerIdText.trim().isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Trainer ID is required.\"}"
                );

                return;
            }

            int trainerId;

            try {

                trainerId =
                        Integer.parseInt(
                                trainerIdText
                        );

            } catch (NumberFormatException e) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid trainer ID.\"}"
                );

                return;
            }


            // ------------------------------------------
            // INSERT REQUEST
            // ------------------------------------------

            String sql =
                    "INSERT INTO trainer_requests " +
                    "(username, trainer_id, request_date, status) " +
                    "VALUES (?, ?, CURDATE(), 'Pending')";


            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
            ) {

                statement.setString(
                        1,
                        username
                );

                statement.setInt(
                        2,
                        trainerId
                );

                statement.executeUpdate();
            }


            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    "{\"success\":true,\"message\":\"Trainer request submitted successfully!\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\"Trainer request failed: "
                    + escapeJson(
                            e.getMessage()
                    )
                    + "\""
                    + "}"
            );
        }
    }


    // ==========================================
    // ESCAPE JSON
    // ==========================================

    private String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        "\\",
                        "\\\\"
                )
                .replace(
                        "\"",
                        "\\\""
                );
    }
}
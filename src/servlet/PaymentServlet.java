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
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;

@WebServlet("/payment")
public class PaymentServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {

            // ==========================================
            // CHECK LOGIN SESSION
            // ==========================================

            HttpSession session =
                    request.getSession(false);

            if (session == null ||
                    session.getAttribute("username") == null) {

                response.setStatus(
                        HttpServletResponse.SC_UNAUTHORIZED);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Please login first.\"}"
                );

                return;
            }


            String username =
                    (String) session.getAttribute("username");


            // ==========================================
            // READ JSON
            // ==========================================

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


            // ==========================================
            // EXTRACT VALUES
            // ==========================================

            String planName =
                    extractJsonValue(
                            jsonData,
                            "planName"
                    );

            String duration =
                    extractJsonValue(
                            jsonData,
                            "duration"
                    );

            String priceText =
                    extractJsonValue(
                            jsonData,
                            "price"
                    );

            String paymentMethod =
                    extractJsonValue(
                            jsonData,
                            "paymentMethod"
                    );

            String transactionId =
                    extractJsonValue(
                            jsonData,
                            "transactionId"
                    );


            // ==========================================
            // VALIDATION
            // ==========================================

            if (planName == null ||
                    duration == null ||
                    priceText == null ||
                    paymentMethod == null ||
                    transactionId == null ||
                    planName.trim().isEmpty() ||
                    duration.trim().isEmpty() ||
                    priceText.trim().isEmpty() ||
                    paymentMethod.trim().isEmpty() ||
                    transactionId.trim().isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Payment details are incomplete.\"}"
                );

                return;
            }


            double price;

            try {

                price =
                        Double.parseDouble(
                                priceText
                        );

            } catch (NumberFormatException e) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "{\"success\":false,\"message\":\"Invalid payment amount.\"}"
                );

                return;
            }


            // ==========================================
            // FIND MEMBERSHIP PLAN
            // ==========================================

            String findPlanSql =
                    "SELECT id, duration_months " +
                    "FROM membership_plans " +
                    "WHERE plan_name = ?";


            int planId = 0;
            int durationMonths = 0;


            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                findPlanSql
                        )
            ) {

                statement.setString(
                        1,
                        planName
                );

                ResultSet resultSet =
                        statement.executeQuery();


                if (resultSet.next()) {

                    planId =
                            resultSet.getInt(
                                    "id"
                            );

                    durationMonths =
                            resultSet.getInt(
                                    "duration_months"
                            );

                } else {

                    response.setStatus(
                            HttpServletResponse.SC_BAD_REQUEST);

                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"Membership plan not found.\"}"
                    );

                    return;
                }

            }


            // ==========================================
            // PAYMENT DATE
            // ==========================================

            LocalDate startDate =
                    LocalDate.now();


            LocalDate endDate =
                    startDate.plusMonths(
                            durationMonths
                    );


            // ==========================================
            // INSERT PAYMENT
            // ==========================================

            String paymentSql =
                    "INSERT INTO payments " +
                    "(username, amount, payment_date, " +
                    "payment_method, payment_status, transaction_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";


            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                paymentSql
                        )
            ) {

                statement.setString(
                        1,
                        username
                );

                statement.setDouble(
                        2,
                        price
                );

                statement.setDate(
                        3,
                        Date.valueOf(
                                startDate
                        )
                );

                statement.setString(
                        4,
                        paymentMethod
                );

                statement.setString(
                        5,
                        "Completed"
                );

                statement.setString(
                        6,
                        transactionId
                );

                statement.executeUpdate();

            }


            // ==========================================
            // INSERT USER MEMBERSHIP
            // ==========================================

            String membershipSql =
                    "INSERT INTO user_memberships " +
                    "(username, plan_id, start_date, " +
                    "end_date, status) " +
                    "VALUES (?, ?, ?, ?, ?)";


            try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(
                                membershipSql
                        )
            ) {

                statement.setString(
                        1,
                        username
                );

                statement.setInt(
                        2,
                        planId
                );

                statement.setDate(
                        3,
                        Date.valueOf(
                                startDate
                        )
                );

                statement.setDate(
                        4,
                        Date.valueOf(
                                endDate
                        )
                );

                statement.setString(
                        5,
                        "Active"
                );

                statement.executeUpdate();

            }


            // ==========================================
            // SUCCESS
            // ==========================================

            response.setStatus(
                    HttpServletResponse.SC_OK
            );

            response.getWriter().write(
                    "{\"success\":true," +
                    "\"message\":\"Payment successful! Membership activated.\"," +
                    "\"plan\":\"" +
                    planName +
                    "\"," +
                    "\"amount\":" +
                    price +
                    "}"
            );


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "{\"success\":false," +
                    "\"message\":\"Payment failed: " +
                    escapeJson(
                            e.getMessage()
                    ) +
                    "\"}"
            );
        }
    }


    // ==========================================
    // GET
    // ==========================================

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendRedirect(
                "payment.html"
        );
    }


    // ==========================================
    // SIMPLE JSON VALUE EXTRACTOR
    // ==========================================

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
                        keyIndex
                );

        if (colonIndex == -1) {
            return null;
        }

        int startQuote =
                json.indexOf(
                        "\"",
                        colonIndex
                );

        if (startQuote == -1) {
            return null;
        }

        int endQuote =
                json.indexOf(
                        "\"",
                        startQuote + 1
                );

        if (endQuote == -1) {
            return null;
        }

        return json.substring(
                startQuote + 1,
                endQuote
        );
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
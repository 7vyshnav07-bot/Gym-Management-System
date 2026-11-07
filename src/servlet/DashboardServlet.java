package servlet;

import db.DatabaseConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null ||
                session.getAttribute("username") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            PrintWriter out = response.getWriter();

            out.print("""
                {
                    "success": false,
                    "message": "Please login first."
                }
                """);

            return;
        }

        String username =
                (String) session.getAttribute("username");

        String fullName = "";
        String email = "";
        String phone = "";

        String membershipPlan = null;
        String membershipStatus = "Inactive";
        String membershipStart = null;
        String membershipEnd = null;

        int attendanceDays = 0;
        int workoutPlans = 0;

        String paymentStatus = "Pending";
        String paymentDate = null;
        double paymentAmount = 0;

        Connection connection = null;

        try {

            connection =
                    DatabaseConnection.getConnection();


            /*
             * ==========================================
             * USER INFORMATION
             * ==========================================
             */

            String userSql = """
                SELECT
                    full_name,
                    username,
                    email,
                    phone
                FROM users
                WHERE username = ?
                """;

            try (PreparedStatement ps =
                         connection.prepareStatement(userSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        fullName =
                                rs.getString("full_name");

                        email =
                                rs.getString("email");

                        phone =
                                rs.getString("phone");
                    }
                }
            }


            /*
             * ==========================================
             * MEMBERSHIP
             * ==========================================
             */

            String membershipSql = """
                SELECT
                    mp.plan_name,
                    um.status,
                    um.start_date,
                    um.end_date
                FROM user_memberships um
                JOIN membership_plans mp
                    ON um.plan_id = mp.id
                WHERE um.username = ?
                AND um.status = 'Active'
                ORDER BY um.id DESC
                LIMIT 1
                """;

            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 membershipSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        membershipPlan =
                                rs.getString("plan_name");

                        membershipStatus =
                                rs.getString("status");

                        membershipStart =
                                rs.getString("start_date");

                        membershipEnd =
                                rs.getString("end_date");
                    }
                }
            }


            /*
             * ==========================================
             * ATTENDANCE
             * ==========================================
             */

            String attendanceSql = """
                SELECT COUNT(*) AS total_days
                FROM attendance
                WHERE username = ?
                AND status = 'Present'
                """;

            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 attendanceSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        attendanceDays =
                                rs.getInt("total_days");
                    }
                }
            }


            /*
             * ==========================================
             * WORKOUT PLANS
             * ==========================================
             */

            String workoutSql = """
                SELECT COUNT(*) AS total_plans
                FROM workout_plans
                WHERE username = ?
                AND status = 'Active'
                """;

            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 workoutSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        workoutPlans =
                                rs.getInt("total_plans");
                    }
                }
            }


            /*
             * ==========================================
             * LATEST PAYMENT
             * ==========================================
             */

            String paymentSql = """
                SELECT
                    amount,
                    payment_date,
                    payment_status
                FROM payments
                WHERE username = ?
                ORDER BY id DESC
                LIMIT 1
                """;

            try (PreparedStatement ps =
                         connection.prepareStatement(
                                 paymentSql)) {

                ps.setString(1, username);

                try (ResultSet rs = ps.executeQuery()) {

                    if (rs.next()) {

                        paymentAmount =
                                rs.getDouble("amount");

                        paymentDate =
                                rs.getString("payment_date");

                        paymentStatus =
                                rs.getString("payment_status");
                    }
                }
            }


            /*
             * ==========================================
             * JSON RESPONSE
             * ==========================================
             */

            PrintWriter out = response.getWriter();

            String json = "{"
                    + "\"success\":true,"

                    + "\"user\":{"
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
                    + "\""
                    + "},"

                    + "\"membership\":{"
                    + "\"plan\":\""
                    + escapeJson(
                            membershipPlan == null
                                    ? "No Membership"
                                    : membershipPlan)
                    + "\","

                    + "\"status\":\""
                    + escapeJson(membershipStatus)
                    + "\","

                    + "\"startDate\":"
                    + jsonString(membershipStart)
                    + ","

                    + "\"endDate\":"
                    + jsonString(membershipEnd)
                    + "},"

                    + "\"attendance\":{"
                    + "\"days\":"
                    + attendanceDays
                    + "},"

                    + "\"workoutPlans\":{"
                    + "\"count\":"
                    + workoutPlans
                    + "},"

                    + "\"payment\":{"
                    + "\"amount\":"
                    + paymentAmount
                    + ","

                    + "\"status\":\""
                    + escapeJson(paymentStatus)
                    + "\","

                    + "\"date\":"
                    + jsonString(paymentDate)
                    + "}"

                    + "}";

            out.print(json);


        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            PrintWriter out = response.getWriter();

            out.print(
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\""
                    + escapeJson(e.getMessage())
                    + "\""
                    + "}"
            );


        } finally {

            if (connection != null) {

                try {
                    connection.close();
                } catch (Exception ignored) {
                }
            }
        }
    }


    /*
     * ==========================================
     * JSON HELPERS
     * ==========================================
     */

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }


    private String jsonString(String value) {

        if (value == null) {
            return "null";
        }

        return "\""
                + escapeJson(value)
                + "\"";
    }
}
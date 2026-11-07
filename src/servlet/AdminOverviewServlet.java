package servlet;

import db.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

@WebServlet("/admin/overview")
public class AdminOverviewServlet extends HttpServlet {

    private int getCount(Connection conn, String sql) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void appendRows(StringBuilder json, Connection conn,
                            String key, String sql) throws Exception {
        json.append(",\"").append(key).append("\":[");
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            boolean firstRow = true;
            while (rs.next()) {
                if (!firstRow) json.append(',');
                firstRow = false;
                json.append('{');
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    if (i > 1) json.append(',');
                    appendString(json, meta.getColumnLabel(i));
                    json.append(':');
                    Object value = rs.getObject(i);
                    if (value == null) json.append("null");
                    else appendString(json, String.valueOf(value));
                }
                json.append('}');
            }
        }
        json.append(']');
    }

    private void appendString(StringBuilder json, String value) {
        json.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"': json.append("\\\""); break;
                case '\\': json.append("\\\\"); break;
                case '\n': json.append("\\n"); break;
                case '\r': json.append("\\r"); break;
                case '\t': json.append("\\t"); break;
                default:
                    if (ch < 0x20) json.append(String.format("\\u%04x", (int) ch));
                    else json.append(ch);
            }
        }
        json.append('"');
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("adminUsername") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = DatabaseConnection.getConnection()) {
            int users = getCount(conn, "SELECT COUNT(*) FROM users");
            int memberships = getCount(conn,
                    "SELECT COUNT(*) FROM user_memberships WHERE status = 'Active'");
            int trainers = getCount(conn, "SELECT COUNT(*) FROM trainers");
            int plans = getCount(conn, "SELECT COUNT(*) FROM workout_plans");
            int payments = getCount(conn, "SELECT COUNT(*) FROM payments");
            int attendance = getCount(conn, "SELECT COUNT(*) FROM attendance");

            StringBuilder json = new StringBuilder("{\"adminUsername\":");
            appendString(json, String.valueOf(session.getAttribute("adminUsername")));
            json.append(String.format(
                ",\"users\":%d,\"activeMemberships\":%d," +
                "\"trainers\":%d,\"workoutPlans\":%d," +
                "\"payments\":%d,\"attendance\":%d",
                users, memberships, trainers, plans, payments, attendance));
            appendRows(json, conn, "members",
                    "SELECT full_name, username, email, phone FROM users ORDER BY username LIMIT 100");
            appendRows(json, conn, "memberships",
                    "SELECT um.id, um.plan_id, um.username, mp.plan_name, um.start_date, um.end_date, um.status " +
                    "FROM user_memberships um JOIN membership_plans mp ON mp.id = um.plan_id " +
                    "ORDER BY um.id DESC LIMIT 100");
            appendRows(json, conn, "trainerRecords",
                    "SELECT id, trainer_name, specialization, experience_years, phone, availability, description " +
                    "FROM trainers ORDER BY id LIMIT 100");
            appendRows(json, conn, "workouts",
                    "SELECT id, username, fitness_goal, experience_level, days_per_week, created_date, status " +
                    "FROM workout_plans ORDER BY id DESC LIMIT 100");
            appendRows(json, conn, "plans",
                    "SELECT id, plan_name, duration_months, price, description, features " +
                    "FROM membership_plans ORDER BY id LIMIT 100");
            appendRows(json, conn, "trainerRequests",
                    "SELECT tr.id, tr.username, t.trainer_name, tr.request_date, tr.status " +
                    "FROM trainer_requests tr JOIN trainers t ON t.id = tr.trainer_id " +
                    "ORDER BY tr.id DESC LIMIT 100");
            appendRows(json, conn, "paymentRecords",
                    "SELECT username, amount, payment_date, payment_method, payment_status " +
                    "FROM payments ORDER BY id DESC LIMIT 100");
            appendRows(json, conn, "attendanceRecords",
                    "SELECT username, attendance_date, status FROM attendance " +
                    "ORDER BY attendance_date DESC LIMIT 100");
            json.append('}');

            response.getWriter().write(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(
                "{\"error\":\"Unable to load dashboard data\"}"
            );
            e.printStackTrace();
        }
    }
}

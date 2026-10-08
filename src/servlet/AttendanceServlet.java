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

@WebServlet("/attendance")
public class AttendanceServlet extends HttpServlet {

    // GET - Load attendance history
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("username") == null) {

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            out.print(
                "{\"success\":false,\"message\":\"Please login first.\"}"
            );

            return;
        }

        String username =
                (String) session.getAttribute("username");

        String sql =
                "SELECT id, attendance_date, status " +
                "FROM attendance " +
                "WHERE username = ? " +
                "ORDER BY attendance_date DESC";

        try (
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            ResultSet resultSet =
                    statement.executeQuery();

            StringBuilder json =
                    new StringBuilder();

            json.append("{\"success\":true,\"attendance\":[");

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                json.append("{");

                json.append("\"id\":")
                        .append(resultSet.getInt("id"))
                        .append(",");

                json.append("\"date\":\"")
                        .append(resultSet.getDate("attendance_date"))
                        .append("\",");

                json.append("\"status\":\"")
                        .append(resultSet.getString("status"))
                        .append("\"");

                json.append("}");

                first = false;
            }

            json.append("]}");

            out.print(json.toString());

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                "{\"success\":false,\"message\":\"Unable to load attendance.\"}"
            );
        }
    }


    // POST - Mark today's attendance
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);

        if (session == null ||
            session.getAttribute("username") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            out.print(
                "{\"success\":false,\"message\":\"Please login first.\"}"
            );

            return;
        }

        String username =
                (String) session.getAttribute("username");

        String status = request.getParameter("status");

        if (status == null || status.trim().isEmpty()) {
            status = "Present";
        }

        String sql =
                "INSERT INTO attendance " +
                "(username, attendance_date, status) " +
                "VALUES (?, CURDATE(), ?) " +
                "ON DUPLICATE KEY UPDATE status = ?";

        try (
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);
            statement.setString(2, status);
            statement.setString(3, status);

            statement.executeUpdate();

            out.print(
                "{\"success\":true," +
                "\"message\":\"Attendance marked successfully!\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print(
                "{\"success\":false," +
                "\"message\":\"Unable to mark attendance.\"}"
            );
        }
    }
}
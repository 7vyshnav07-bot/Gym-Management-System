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

@WebServlet("/membership")
public class MembershipServlet extends HttpServlet {

    @Override
    protected void doGet(
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


            // ==========================================
            // FIND ACTIVE MEMBERSHIP
            // ==========================================

            String sql =
                    "SELECT " +
                    "um.id, " +
                    "um.start_date, " +
                    "um.end_date, " +
                    "um.status, " +
                    "mp.plan_name, " +
                    "mp.duration_months, " +
                    "mp.price " +
                    "FROM user_memberships um " +
                    "JOIN membership_plans mp " +
                    "ON um.plan_id = mp.id " +
                    "WHERE um.username = ? " +
                    "AND um.status = 'Active' " +
                    "ORDER BY um.id DESC " +
                    "LIMIT 1";


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


                ResultSet resultSet =
                        statement.executeQuery();


                // ==========================================
                // NO MEMBERSHIP
                // ==========================================

                if (!resultSet.next()) {

                    response.setStatus(
                            HttpServletResponse.SC_OK
                    );

                    response.getWriter().write(
                            "{\"success\":true,\"membership\":null}"
                    );

                    return;
                }


                // ==========================================
                // GET MEMBERSHIP DATA
                // ==========================================

                int id =
                        resultSet.getInt(
                                "id"
                        );

                String planName =
                        resultSet.getString(
                                "plan_name"
                        );

                int durationMonths =
                        resultSet.getInt(
                                "duration_months"
                        );

                double price =
                        resultSet.getDouble(
                                "price"
                        );

                String startDate =
                        resultSet.getDate(
                                "start_date"
                        ).toString();

                String endDate =
                        resultSet.getDate(
                                "end_date"
                        ).toString();

                String status =
                        resultSet.getString(
                                "status"
                        );


                // ==========================================
                // SEND JSON
                // ==========================================

                String json =
                        "{"
                        + "\"success\":true,"
                        + "\"membership\":{"
                        + "\"id\":" + id + ","
                        + "\"planName\":\""
                        + escapeJson(planName)
                        + "\","
                        + "\"durationMonths\":"
                        + durationMonths + ","
                        + "\"price\":"
                        + price + ","
                        + "\"startDate\":\""
                        + startDate
                        + "\","
                        + "\"endDate\":\""
                        + endDate
                        + "\","
                        + "\"status\":\""
                        + escapeJson(status)
                        + "\""
                        + "}"
                        + "}";


                response.getWriter().write(
                        json
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
                    + "\"message\":\"Membership loading failed: "
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
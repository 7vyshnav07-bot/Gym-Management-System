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

@WebServlet("/workout")
public class WorkoutServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("username") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please login first.\"}"
            );

            return;
        }

        String username =
                (String) session.getAttribute("username");

        String sql = """
            SELECT
                id,
                fitness_goal,
                experience_level,
                days_per_week,
                plan_data,
                created_date,
                status
            FROM workout_plans
            WHERE username = ?
            AND status = 'Active'
            ORDER BY id DESC
            """;

        try (
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, username);

            ResultSet result =
                    statement.executeQuery();

            StringBuilder plans =
                    new StringBuilder();

            plans.append("[");

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    plans.append(",");
                }

                first = false;

                plans.append("{");

                plans.append("\"id\":")
                        .append(result.getInt("id"))
                        .append(",");

                plans.append("\"fitnessGoal\":\"")
                        .append(
                            escapeJson(
                                result.getString(
                                    "fitness_goal"
                                )
                            )
                        )
                        .append("\",");

                plans.append("\"experienceLevel\":\"")
                        .append(
                            escapeJson(
                                result.getString(
                                    "experience_level"
                                )
                            )
                        )
                        .append("\",");

                plans.append("\"daysPerWeek\":")
                        .append(
                            result.getInt(
                                "days_per_week"
                            )
                        )
                        .append(",");

                plans.append("\"planData\":")
                        .append(
                            result.getString(
                                "plan_data"
                            )
                        )
                        .append(",");

                plans.append("\"createdDate\":\"")
                        .append(
                            result.getString(
                                "created_date"
                            )
                        )
                        .append("\",");

                plans.append("\"status\":\"")
                        .append(
                            escapeJson(
                                result.getString(
                                    "status"
                                )
                            )
                        )
                        .append("\"");

                plans.append("}");
            }

            plans.append("]");

            PrintWriter out =
                    response.getWriter();

            out.print(
                "{"
                + "\"success\":true,"
                + "\"plans\":"
                + plans
                + "}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(e.getMessage())
                + "\""
                + "}"
            );
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
            session.getAttribute("username") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print(
                "{\"success\":false,\"message\":\"Please login first.\"}"
            );

            return;
        }

        String username =
                (String) session.getAttribute("username");


        /*
         * ==========================================
         * READ FORM DATA
         * ==========================================
         */

        String fitnessGoal =
                request.getParameter("fitnessGoal");

        String experienceLevel =
                request.getParameter("experienceLevel");

        String daysValue =
                request.getParameter("daysPerWeek");

        String planData =
                request.getParameter("planData");


        /*
         * ==========================================
         * VALIDATION
         * ==========================================
         */

        if (fitnessGoal == null ||
            experienceLevel == null ||
            daysValue == null ||
            planData == null ||
            fitnessGoal.isBlank() ||
            experienceLevel.isBlank() ||
            daysValue.isBlank() ||
            planData.isBlank()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                "{"
                + "\"success\":false,"
                + "\"message\":\"Missing workout plan information.\""
                + "}"
            );

            return;
        }


        int daysPerWeek;

        try {

            daysPerWeek =
                    Integer.parseInt(daysValue);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                "{"
                + "\"success\":false,"
                + "\"message\":\"Invalid training days.\""
                + "}"
            );

            return;
        }


        /*
         * ==========================================
         * SAVE WORKOUT PLAN
         * ==========================================
         */

        String sql = """
            INSERT INTO workout_plans
            (
                username,
                fitness_goal,
                experience_level,
                days_per_week,
                plan_data,
                created_date,
                status
            )
            VALUES
            (?, ?, ?, ?, ?, CURDATE(), 'Active')
            """;


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

            statement.setString(
                    2,
                    fitnessGoal
            );

            statement.setString(
                    3,
                    experienceLevel
            );

            statement.setInt(
                    4,
                    daysPerWeek
            );

            statement.setString(
                    5,
                    planData
            );


            int rows =
                    statement.executeUpdate();


            if (rows > 0) {

                response.getWriter().print(
                    "{"
                    + "\"success\":true,"
                    + "\"message\":\"Workout plan saved successfully!\""
                    + "}"
                );

            } else {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                response.getWriter().print(
                    "{"
                    + "\"success\":false,"
                    + "\"message\":\"Workout plan could not be saved.\""
                    + "}"
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                "{"
                + "\"success\":false,"
                + "\"message\":\""
                + escapeJson(e.getMessage())
                + "\""
                + "}"
            );
        }
    }


    /*
     * ==========================================
     * JSON ESCAPE
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
}
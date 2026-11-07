
package servlet;

import service.JsonAttendanceStore;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/mark-attendance")
public class AttendanceMarkServlet extends HttpServlet {

    private String getJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile(
            "\"" + Pattern.quote(key) +
            "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            return "";
        }

        return matcher.group(1)
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
            .replace("\\n", "\n")
            .replace("\\r", "\r");
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String body = new String(
            request.getInputStream().readAllBytes(),
            StandardCharsets.UTF_8
        );

        String username = getJsonValue(body, "username").trim();
        String status = getJsonValue(body, "status").trim();

        if (username.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Username is required\"}"
            );
            return;
        }

        if (status.isBlank()) {
            status = "Present";
        }

        try {
            boolean saved =
                JsonAttendanceStore.markAttendance(username, status);

            if (!saved) {
                response.setStatus(
                    HttpServletResponse.SC_CONFLICT
                );
                response.getWriter().write(
                    "{\"success\":false,\"message\":\"Attendance already marked for today\"}"
                );
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"success\":true,\"message\":\"Attendance marked successfully\"}"
            );

        } catch (IOException e) {
            getServletContext().log(
                "Unable to save attendance JSON", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to save attendance\"}"
            );
        }
    }
}

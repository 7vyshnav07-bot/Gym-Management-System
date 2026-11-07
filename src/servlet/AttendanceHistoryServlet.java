
package servlet;
import service.JsonAttendanceStore;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/get-attendance")
public class AttendanceHistoryServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");

        if (username == null || username.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Username is required\"}"
            );
            return;
        }

        try {
            String attendance =
                JsonAttendanceStore.getAttendanceJson(username);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"success\":true,\"attendance\":" + attendance + "}"
            );

        } catch (IOException e) {
            getServletContext().log(
                "Unable to load attendance JSON", e
            );

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Unable to load attendance\"}"
            );
        }
    }
}

package servlet;

import db.DatabaseConnection;
import security.PasswordUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.sql.*;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {
    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        String username=req.getParameter("username");
        String password=req.getParameter("password");
        if(username==null || password==null || username.isBlank() || password.isEmpty()) {
            resp.setStatus(400); resp.getWriter().write("{\"success\":false,\"message\":\"Username and password are required.\"}"); return;
        }
        String sql="SELECT username, password_hash FROM admin_users WHERE username = ? AND active = 1";
        try(Connection c=DatabaseConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,username.trim());
            try(ResultSet rs=ps.executeQuery()) {
                if(!rs.next() || !PasswordUtil.verifyPassword(password,rs.getString("password_hash"))) {
                    resp.setStatus(401); resp.getWriter().write("{\"success\":false,\"message\":\"Invalid admin credentials.\"}"); return;
                }
                HttpSession old=req.getSession(false); if(old!=null) old.invalidate();
                HttpSession session=req.getSession(true);
                session.setAttribute("adminUsername",rs.getString("username"));
                session.setMaxInactiveInterval(30*60);
                resp.getWriter().write("{\"success\":true}");
            }
        } catch(SQLException e) { throw new ServletException("Admin login failed.",e); }
    }
}

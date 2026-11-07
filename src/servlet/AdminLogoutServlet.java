package servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import java.io.IOException;
@WebServlet("/admin/logout")
public class AdminLogoutServlet extends HttpServlet {
 @Override protected void doPost(HttpServletRequest req,HttpServletResponse resp) throws ServletException,IOException {
   HttpSession s=req.getSession(false);
   if(s!=null){s.removeAttribute("adminUsername"); if(s.getAttribute("username")==null)s.invalidate();}
   resp.setContentType("application/json;charset=UTF-8"); resp.getWriter().write("{\"success\":true}");
 }
}

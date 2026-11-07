package servlet;

import db.DatabaseConnection;
import security.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

@WebServlet("/admin/manage/*")
public class AdminManageServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUsername") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            write(resp, false, "Admin sign-in required.");
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String entity = req.getPathInfo() == null ? "" : req.getPathInfo().replace("/", "");
        String action = value(req, "action");
        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (entity) {
                case "members": saveMember(conn, req, action); break;
                case "trainers": saveTrainer(conn, req, action); break;
                case "plans": savePlan(conn, req, action); break;
                case "memberships": saveMembership(conn, req, action); break;
                case "workouts": updateWorkout(conn, req, action); break;
                case "trainerRequests": updateTrainerRequest(conn, req, action); break;
                default: throw new BadRequest("Unknown admin module.");
            }
            write(resp, true, "Changes saved.");
        } catch (BadRequest e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            write(resp, false, e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Admin data change failed for module " + entity, e);
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            write(resp, false, "The change could not be saved. Check for duplicate or linked records.");
        } catch (Exception e) {
            getServletContext().log("Admin data change failed for module " + entity, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            write(resp, false, "Unable to save this change.");
        }
    }

    private void saveMember(Connection c, HttpServletRequest r, String action) throws Exception {
        if ("delete".equals(action)) {
            String target = required(r, "original_username");
            String[] related = {"user_memberships", "payments", "attendance", "workout_plans", "trainer_requests"};
            for (String table : related) {
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + table + " WHERE username=?")) {
                    ps.setString(1, target);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0)
                            throw new BadRequest("This member has linked history and cannot be removed. Edit the member profile instead.");
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE username=?")) {
                ps.setString(1, target);
                if (ps.executeUpdate() == 0) throw new BadRequest("Member not found.");
            }
            return;
        }
        String fullName = required(r, "full_name");
        String username = required(r, "username");
        String email = required(r, "email");
        String phone = required(r, "phone");
        String dob = optional(r, "date_of_birth");
        String country = optional(r, "country");
        String gender = optional(r, "gender");
        String password = optional(r, "password");
        if ("create".equals(action)) {
            if (password.length() < 8) throw new BadRequest("New member passwords must be at least 8 characters.");
            String sql = "INSERT INTO users (full_name, username, email, phone, date_of_birth, country, gender, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, fullName); ps.setString(2, username); ps.setString(3, email);
                ps.setString(4, phone); ps.setString(5, dob); ps.setString(6, country);
                ps.setString(7, gender); ps.setString(8, PasswordUtil.hashPassword(password));
                requireUpdated(ps.executeUpdate());
            }
        } else if ("update".equals(action)) {
            String target = required(r, "original_username");
            String sql = "UPDATE users SET full_name=?, email=?, phone=?, date_of_birth=?, country=?, gender=? WHERE username=?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, fullName); ps.setString(2, email); ps.setString(3, phone);
                ps.setString(4, dob); ps.setString(5, country); ps.setString(6, gender);
                ps.setString(7, target); ps.executeUpdate();
            }
            if (!password.isEmpty()) {
                if (password.length() < 8) throw new BadRequest("Passwords must be at least 8 characters.");
                try (PreparedStatement ps = c.prepareStatement("UPDATE users SET password=? WHERE username=?")) {
                    ps.setString(1, PasswordUtil.hashPassword(password)); ps.setString(2, target);
                    ps.executeUpdate();
                }
            }
        } else throw new BadRequest("Unsupported member action.");
    }

    private void saveTrainer(Connection c, HttpServletRequest r, String action) throws Exception {
        if ("delete".equals(action)) {
            int id = integer(r, "id", 1, Integer.MAX_VALUE);
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM trainer_requests WHERE trainer_id=?")) {
                ps.setInt(1, id);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0)
                        throw new BadRequest("This trainer has linked member requests and cannot be removed. Change availability instead.");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM trainers WHERE id=?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) throw new BadRequest("Trainer not found.");
            }
            return;
        }
        String name = required(r, "trainer_name");
        String specialization = required(r, "specialization");
        int experience = integer(r, "experience_years", 0, 80);
        String phone = optional(r, "phone");
        String availability = required(r, "availability");
        String description = optional(r, "description");
        if ("create".equals(action)) {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO trainers (trainer_name, specialization, experience_years, phone, availability, description) VALUES (?, ?, ?, ?, ?, ?)")) {
                bindTrainer(ps, name, specialization, experience, phone, availability, description);
                requireUpdated(ps.executeUpdate());
            }
        } else if ("update".equals(action)) {
            int id = integer(r, "id", 1, Integer.MAX_VALUE);
            try (PreparedStatement ps = c.prepareStatement("UPDATE trainers SET trainer_name=?, specialization=?, experience_years=?, phone=?, availability=?, description=? WHERE id=?")) {
                bindTrainer(ps, name, specialization, experience, phone, availability, description);
                ps.setInt(7, id); ps.executeUpdate();
            }
        } else throw new BadRequest("Unsupported trainer action.");
    }

    private void bindTrainer(PreparedStatement ps, String name, String spec, int exp,
                             String phone, String availability, String description) throws SQLException {
        ps.setString(1, name); ps.setString(2, spec); ps.setInt(3, exp);
        ps.setString(4, phone); ps.setString(5, availability); ps.setString(6, description);
    }

    private void savePlan(Connection c, HttpServletRequest r, String action) throws Exception {
        if ("delete".equals(action)) {
            int id = integer(r, "id", 1, Integer.MAX_VALUE);
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM user_memberships WHERE plan_id=?")) {
                ps.setInt(1, id);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0)
                        throw new BadRequest("This plan has member assignments and cannot be removed. Edit its details instead.");
                }
            }
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM membership_plans WHERE id=?")) {
                ps.setInt(1, id);
                if (ps.executeUpdate() == 0) throw new BadRequest("Membership plan not found.");
            }
            return;
        }
        String name = required(r, "plan_name");
        int months = integer(r, "duration_months", 1, 120);
        double price;
        try { price = Double.parseDouble(required(r, "price")); }
        catch (NumberFormatException e) { throw new BadRequest("Enter a valid plan price."); }
        if (!Double.isFinite(price) || price < 0) throw new BadRequest("Plan price must be zero or more.");
        String description = optional(r, "description");
        String features = optional(r, "features");
        String sql;
        if ("create".equals(action)) {
            sql = "INSERT INTO membership_plans (plan_name, duration_months, price, description, features) VALUES (?, ?, ?, ?, ?)";
        } else if ("update".equals(action)) {
            sql = "UPDATE membership_plans SET plan_name=?, duration_months=?, price=?, description=?, features=? WHERE id=?";
        } else throw new BadRequest("Unsupported membership plan action.");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name); ps.setInt(2, months); ps.setDouble(3, price);
            ps.setString(4, description); ps.setString(5, features);
            if ("update".equals(action)) ps.setInt(6, integer(r, "id", 1, Integer.MAX_VALUE));
            ps.executeUpdate();
        }
    }

    private void saveMembership(Connection c, HttpServletRequest r, String action) throws Exception {
        String username = required(r, "username");
        int planId = integer(r, "plan_id", 1, Integer.MAX_VALUE);
        LocalDate start = date(r, "start_date");
        LocalDate end = date(r, "end_date");
        if (end.isBefore(start)) throw new BadRequest("Membership end date must be on or after its start date.");
        String status = oneOf(r, "status", "Active", "Pending", "Expired", "Cancelled");
        String sql;
        if ("create".equals(action)) {
            sql = "INSERT INTO user_memberships (username, plan_id, start_date, end_date, status) VALUES (?, ?, ?, ?, ?)";
        } else if ("update".equals(action)) {
            sql = "UPDATE user_memberships SET username=?, plan_id=?, start_date=?, end_date=?, status=? WHERE id=?";
        } else throw new BadRequest("Unsupported membership action.");
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setInt(2, planId); ps.setDate(3, java.sql.Date.valueOf(start));
            ps.setDate(4, java.sql.Date.valueOf(end)); ps.setString(5, status);
            if ("update".equals(action)) ps.setInt(6, integer(r, "id", 1, Integer.MAX_VALUE));
            ps.executeUpdate();
        }
    }

    private void updateWorkout(Connection c, HttpServletRequest r, String action) throws Exception {
        if ("delete".equals(action)) {
            try (PreparedStatement ps = c.prepareStatement("DELETE FROM workout_plans WHERE id=?")) {
                ps.setInt(1, integer(r, "id", 1, Integer.MAX_VALUE));
                if (ps.executeUpdate() == 0) throw new BadRequest("Workout plan not found.");
            }
            return;
        }
        if (!"update".equals(action)) throw new BadRequest("Workout plans can only be updated.");
        String status = oneOf(r, "status", "Active", "Completed", "Paused", "Cancelled");
        try (PreparedStatement ps = c.prepareStatement("UPDATE workout_plans SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, integer(r, "id", 1, Integer.MAX_VALUE));
            ps.executeUpdate();
        }
    }

    private void updateTrainerRequest(Connection c, HttpServletRequest r, String action) throws Exception {
        if (!"update".equals(action)) throw new BadRequest("Trainer requests can only be reviewed.");
        String status = oneOf(r, "status", "Pending", "Approved", "Rejected");
        try (PreparedStatement ps = c.prepareStatement("UPDATE trainer_requests SET status=? WHERE id=?")) {
            ps.setString(1, status); ps.setInt(2, integer(r, "id", 1, Integer.MAX_VALUE));
            ps.executeUpdate();
        }
    }

    private String value(HttpServletRequest r, String name) {
        String value = r.getParameter(name);
        return value == null ? "" : value.trim();
    }
    private String required(HttpServletRequest r, String name) throws BadRequest {
        String v = value(r, name);
        if (v.isEmpty()) throw new BadRequest("Complete all required fields.");
        return v;
    }
    private String optional(HttpServletRequest r, String name) { return value(r, name); }
    private int integer(HttpServletRequest r, String name, int min, int max) throws BadRequest {
        try { int v = Integer.parseInt(required(r, name)); if (v < min || v > max) throw new NumberFormatException(); return v; }
        catch (NumberFormatException e) { throw new BadRequest("Enter a valid value for " + name.replace('_', ' ') + "."); }
    }
    private LocalDate date(HttpServletRequest r, String name) throws BadRequest {
        try { return LocalDate.parse(required(r, name)); }
        catch (Exception e) { throw new BadRequest("Enter a valid " + name.replace('_', ' ') + "."); }
    }
    private String oneOf(HttpServletRequest r, String name, String... choices) throws BadRequest {
        String v = required(r, name);
        for (String choice : choices) if (choice.equals(v)) return v;
        throw new BadRequest("Choose a valid " + name.replace('_', ' ') + ".");
    }
    private void requireUpdated(int count) throws BadRequest {
        if (count < 1) throw new BadRequest("The record was not found or no change was made.");
    }
    private void write(HttpServletResponse resp, boolean success, String message) throws IOException {
        resp.getWriter().write("{\"success\":" + success + ",\"message\":\"" + escape(message) + "\"}");
    }
    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
    private static class BadRequest extends Exception {
        BadRequest(String message) { super(message); }
    }
}

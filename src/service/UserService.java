package service;
import security.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import db.DatabaseConnection;
import model.User;

public class UserService implements UserOperations {

    private User registeredUser;

    @Override
    public void registerUser(User user) {

        if (!validate(user)) {
            System.out.println("Registration failed.");
            return;
        }

        String sql = """
                INSERT INTO users
                (full_name, username, email, phone,
                 date_of_birth, country, gender, password)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setString(1, user.getFullName());
            statement.setString(2, user.getUsername());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getDateOfBirth());
            statement.setString(6, user.getCountry());
            statement.setString(7, user.getGender());
           statement.setString(
        8,
        security.PasswordUtil.hashPassword(user.getPassword())
);

            statement.executeUpdate();

            registeredUser = user;

            System.out.println("\nRegistration successful!");
            System.out.println("Welcome, " + user.getFullName());

        } catch (Exception e) {

            System.out.println(
                    "Registration failed: "
                    + e.getMessage()
            );
        }
    }

   

@Override
public boolean authenticateUser(
        String username,
        String password) {

    String sql =
            "SELECT id, password FROM users WHERE username = ?";

    try (
        Connection connection =
                DatabaseConnection.getConnection();

        PreparedStatement statement =
                connection.prepareStatement(sql)
    ) {

        statement.setString(1, username);

        try (ResultSet resultSet = statement.executeQuery()) {

            if (!resultSet.next()) {
                return false;
            }

            int userId = resultSet.getInt("id");
            String storedPassword =
                    resultSet.getString("password");

            // Already hashed password
            if (storedPassword != null
                    && storedPassword.startsWith("pbkdf2$")) {

                return security.PasswordUtil.verifyPassword(
                        password,
                        storedPassword
                );
            }

            // Legacy plaintext password: verify, then upgrade it
            if (storedPassword != null
                    && storedPassword.equals(password)) {

                String hashedPassword =
                        security.PasswordUtil.hashPassword(password);

                String updateSql =
                        "UPDATE users SET password = ? WHERE id = ?";

                try (PreparedStatement updateStatement =
                        connection.prepareStatement(updateSql)) {

                    updateStatement.setString(1, hashedPassword);
                    updateStatement.setInt(2, userId);

                    return updateStatement.executeUpdate() == 1;
                }
            }
        }

    } catch (Exception e) {
        System.out.println("Login error: " + e.getMessage());
    }

    return false;
}


    @Override
    public boolean validate(User user) {

        if (user == null) {
            System.out.println("Invalid user.");
            return false;
        }

        if (user.getFullName() == null ||
                user.getFullName().trim().isEmpty()) {

            System.out.println("Full name cannot be empty.");
            return false;
        }

        if (user.getUsername() == null ||
                user.getUsername().length() < 3) {

            System.out.println(
                    "Username must contain at least 3 characters."
            );
            return false;
        }

        if (user.getEmail() == null ||
                !user.getEmail().contains("@") ||
                !user.getEmail().contains(".")) {

            System.out.println(
                    "Enter a valid email address."
            );
            return false;
        }

        if (user.getPhone() == null ||
                !user.getPhone().matches("\\d{7,15}")) {

            System.out.println(
                    "Phone number must contain 7 to 15 digits."
            );
            return false;
        }

        if (user.getPassword() == null ||
                user.getPassword().length() < 6) {

            System.out.println(
                    "Password must contain at least 6 characters."
            );
            return false;
        }

        return true;
    }

    public User getRegisteredUser() {
        return registeredUser;
    }
}
public class User {

    private String username;
    private String password;

    // Constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Validate username
    public boolean isValidUsername() {

        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        if (username.length() < 3) {
            return false;
        }

        return true;
    }

    // Validate password
    public boolean isValidPassword() {

        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        if (password.length() < 6) {
            return false;
        }

        return true;
    }

    // Validate complete user
    public boolean validateUser() {

        return isValidUsername() && isValidPassword();
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }
}
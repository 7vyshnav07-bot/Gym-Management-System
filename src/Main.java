import java.util.Scanner;
import model.User;
import service.UserService;
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();
        // ==============================
        // REGISTRATION PAGE
        // ==============================
        System.out.println("=================================");
        System.out.println("       GYM MANAGEMENT SYSTEM");
        System.out.println("=================================");
        System.out.println("\n--------- REGISTRATION ---------");
        System.out.print("Full Name: ");
        String fullName = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Phone: ");
        String phone = scanner.nextLine();
        System.out.print("Date of Birth: ");
        String dateOfBirth = scanner.nextLine();
        System.out.print("Country: ");
        String country = scanner.nextLine();
        System.out.print("Gender: ");
        String gender = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        User user = new User(
                fullName,
                username,
                email,
                phone,
                dateOfBirth,
                country,
                gender,
                password
        );
        // ==============================
        // VALIDATION
        // ==============================
        if (!userService.validate(user)) {
            System.out.println("\nRegistration failed.");
            System.out.println("Please correct the details and try again.");
            scanner.close();
            return;
        }
        // ==============================
        // REGISTRATION
        // ==============================
        userService.registerUser(user);
        System.out.println("\n=================================");
        System.out.println("    REGISTRATION SUCCESSFUL!");
        System.out.println("=================================");
        System.out.println("Account created successfully.");
        // ==============================
        // LOGIN PAGE
        // ==============================
        System.out.println("\n");
        System.out.println("=================================");
        System.out.println("           LOGIN PAGE");
        System.out.println("=================================");
        System.out.print("Username: ");
        String loginUsername = scanner.nextLine();
        System.out.print("Password: ");
        String loginPassword = scanner.nextLine();
        // ==============================
        // LOGIN VALIDATION
        // ==============================
        if (userService.authenticateUser(loginUsername, loginPassword)) {
            System.out.println("\n=================================");
            System.out.println("       LOGIN SUCCESSFUL!");
            System.out.println("=================================");
            System.out.println("Welcome back, "
                    + user.getFullName() + "!");
            // ==============================
            // DASHBOARD
            // ==============================
            System.out.println("\n=================================");
            System.out.println("      GYM MANAGEMENT DASHBOARD");
            System.out.println("=================================");
            System.out.println("1. Member Profile");
            System.out.println("2. Membership Plans");
            System.out.println("3. Trainer");
            System.out.println("4. Workout Plans");
            System.out.println("5. Attendance");
            System.out.println("6. Payment");
            System.out.println("7. Logout");
            System.out.println("\nDashboard loaded successfully.");
        } else {
            System.out.println("\n=================================");
            System.out.println("          LOGIN FAILED");
            System.out.println("=================================");
            System.out.println("Invalid username or password.");
        }
        scanner.close();
    }
}
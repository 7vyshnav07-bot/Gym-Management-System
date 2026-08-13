import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("================================");
        System.out.println("     GYM MANAGEMENT SYSTEM");
        System.out.println("================================");

        System.out.println("\n--- User Registration ---");

        System.out.print("Enter username: ");
        String username = sc.nextLine();

        System.out.print("Enter password: ");
        String password = sc.nextLine();

        // Create User object
        User user = new User(username, password);

        // Check username first
        if (!user.isValidUsername()) {

            System.out.println("\nRegistration Failed!");
            System.out.println("- Username must contain at least 3 characters.");

        }
        // Check password only if username is valid
        else if (!user.isValidPassword()) {

            System.out.println("\nRegistration Failed!");
            System.out.println("- Password must contain at least 6 characters.");

        }
        // Both are valid
        else {

            System.out.println("\nRegistration Successful!");
            System.out.println("Welcome " + user.getUsername() + "!");
        }

        sc.close();
    }
}
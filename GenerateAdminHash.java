import security.PasswordUtil;
import java.util.Scanner;

public class GenerateAdminHash {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a new admin password: ");
        String password = scanner.nextLine();

        System.out.println("Password hash:");
        System.out.println(PasswordUtil.hashPassword(password));
    }
}
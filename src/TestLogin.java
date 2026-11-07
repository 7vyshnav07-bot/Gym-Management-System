import service.UserService;

public class TestLogin {

    public static void main(String[] args) {

        UserService userService = new UserService();

        boolean result =
                userService.authenticateUser(
                        "vyshnav0107",
                        "Hello@123"
                );

        System.out.println("LOGIN RESULT: " + result);
    }
}
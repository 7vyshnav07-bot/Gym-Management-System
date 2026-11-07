package service;

import model.User;

public interface UserOperations {

    void registerUser(User user);

    boolean authenticateUser(String username, String password);

    boolean validate(User user);
}
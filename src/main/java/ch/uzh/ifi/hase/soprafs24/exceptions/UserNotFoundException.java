package ch.uzh.ifi.hase.soprafs24.exceptions;

//custom exception, is used in UserService when creating a User

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}

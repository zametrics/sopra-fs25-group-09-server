package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.mindrot.jbcrypt.BCrypt; //instead of spring security
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;

import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;




/* User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.*/

@Service
@Transactional
public class UserService {

  private final Logger log = LoggerFactory.getLogger(UserService.class);
  private final UserRepository userRepository;

  @Autowired
  public UserService(@Qualifier("userRepository") UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  // Get all users
  public List<User> getUsers() {
    return this.userRepository.findAll();
  }

  // Create a new user
  //In the User Controller --> the newUser is a User Entity (see create user function in user controller)
  //thats also why we specified newUser to be a User object
  public User createUser(User newUser) {
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    String hashedPassword = BCrypt.hashpw(newUser.getPassword(), BCrypt.gensalt(12));
    newUser.setPassword(hashedPassword);
    checkIfUserExists(newUser);
    // Saves the given entity but data is only persisted in the database once
    // flush() is called
    newUser = userRepository.save(newUser);
    userRepository.flush();

    log.debug("Created Information for User: {}", newUser);
    return newUser;
  }

  // Find a user by username
  public User findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  public String getTokenForUser(String username) {
    // Find user by username from the database
    User user = userRepository.findByUsername(username);

    // If the user exists, return the token
    if (user != null) {
        return user.getToken();
    } else {
        // Return null or throw an exception if user is not found
        throw new UserNotFoundException("User not found for username: " + username);
    }
}

  // Simple login method
  public boolean login(String username, String password) {
    // Find the user by username
    User user = userRepository.findByUsername(username);

    // If the user does not exist, login fails
    if (user == null) {
      return false;
    }

    // If the user exists, check if the password matches
    if (BCrypt.checkpw(password, user.getPassword())) {
      return true; // Login successful
    }

    return false; // Login failed
  }


  // In UserService.java

  public User getUserById(Long id) {
    Optional<User> userOptional = userRepository.findById(id);
    if (userOptional.isPresent()) {
        return userOptional.get(); // Retrieve and return the User
    } else {
        throw new UserNotFoundException("User not found with id: " + id); // Handle the case where the user is not found
    }
  }

    public void setUserStatus(String token, UserStatus status) {
        // Find the user by username
        User user = userRepository.findByToken(token);
        if (user != null) {
            // Set the status of the user to the given status
            user.setStatus(status);
            // Save the user with the updated status
            userRepository.save(user);
        }
    }


    // Update the Date of Birth for a user identified by their username
    public void setDateOfBirth(Long id, String dateOfBirth) {
        // Find the user by username
        User user = userRepository.findById(id).orElse(null);

        // Update the Date of Birth
    // Check if the user exists before updating
    if (user != null) {
      user.setDateOfBirth(dateOfBirth);
      userRepository.save(user); // Save the updated user
    } else {
      throw new EntityNotFoundException("User not found with id: " + id);
  }
        
        // Save the updated user
        userRepository.save(user);
    }


    public User updateUsername(long id , String newUsername) {
      User user = userRepository.findById(id).orElse(null);
      
      // Check if the username already exists
      if (userRepository.findByUsername(newUsername) != null) {
          throw new IllegalArgumentException("Username is already taken.");
      }

      user.setUsername(newUsername);
      return userRepository.save(user);
  }


  public boolean usernameExists(String username) {
    User userByUsername = userRepository.findByUsername(username);
    return userByUsername != null;
  }

  public void deleteUser(Long id) {
      if(!userRepository.existsById(id)) {
          throw new UserNotFoundException("User with id was not found: " + id);  //use Java persistence API from dependencies
      }
      userRepository.deleteById(id);
  }

  /* This is a helper method that will check the uniqueness criteria of the
   * username and the name
   * defined in the User entity. The method will do nothing if the input is unique
   * and throw an error otherwise.
   *
   * @param userToBeCreated
   * @throws org.springframework.web.server.ResponseStatusException
   * @see User 
   *  */

  
  private void checkIfUserExists(User userToBeCreated) {
    User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());

    String baseErrorMessage = "The %s provided %s not unique. Therefore, the user could not be created!";
    if (userByUsername != null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format(baseErrorMessage, "username", "is"));
    } 
  }

  // Find a user by token
public User findByToken(String token) {
  User user = userRepository.findByToken(token);
  if (user == null) {
      throw new UserNotFoundException("User not found with the provided token: " + token);
  }
  return user;
}

public User updateAvatarUrl(Long userId, String avatarUrl) {
  User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
  
  // Update avatarUrl
  user.setAvatarUrl(avatarUrl);
  
  // Save the updated user entity
  return userRepository.save(user);
}

}

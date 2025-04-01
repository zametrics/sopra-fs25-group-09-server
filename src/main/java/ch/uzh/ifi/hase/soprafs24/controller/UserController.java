package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;





/* This class is responsible for handling all REST request that are related to the user. 
 * The controller will receive the request and delegate the execution to the. UserService and finally return the result.

 /* Basically this is a Spring MVC controller that handles HTTP request
  * it will receive HTTP requests from the frontend and passes it to UserService for processing
  the code will convert the response into an API-friendly format DTO, Entity etc. */

@RestController
public class UserController {

  //private because it can only be acccessed in UserController
  // final because it cant be changed
  // UserService refers to the type of class "UserService" with the
  // Object name being UserService

  // Instance variable stores passed object
  private final UserService userService;

  //constructor like in python
  UserController(UserService userService) {

    // assign instance variable the object
    this.userService = userService;
  }

  // when opening URL/users we make GET request that wants to see all users
  @GetMapping("/users")
  //This tells the frontend that the request was successful with status 200 OK.
  @ResponseStatus(HttpStatus.OK)
  //This makes sure the method returns JSON data, not just text.
  @ResponseBody

/*Explanations for code below 
  Spring Boot matches the request with @GetMapping("/users").

  It calls the getAllUsers() method.
  UserService.getUsers() fetches all users from the database.
  Users are converted into JSON format and sent back */

  //this is a functions that return a List of Object type UserGet DTO
  public List<UserGetDTO> getAllUsers() {

    // fetch all users in the internal representation --> getUsers() is a method defined in userService
    List<User> users = userService.getUsers();

    //Creates an empty list where we will store the users.
    List<UserGetDTO> userGetDTOs = new ArrayList<>();

    // convert each user to the API representation
    for (User user : users) {

      //loop through all users entitis and convert them to a UserGetDTO(data transfer object)
      userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user)); //--> we convert the entity to a UserGetDTO where we e.g remove sensitive info like passwords
    }

    //Sends the list of users as JSON to the frontend.
    return userGetDTOs;
  }

  // When we are in the registration page and we want to send something this will handle it (POST request sent by sending username + password)
  @PostMapping("/users")

  //When this method executes successfully, it returns an HTTP status code 201 Created to the client.
  @ResponseStatus(HttpStatus.CREATED)

  //This means that the return value of this method will be automatically converted to JSON (or another format, based on configuration) and sent as the HTTP response.
  @ResponseBody
  //the public function createUser is of object UserGetDTO. The input is a Java Object UserPostDTO @RequestBody will transform whatever comes in into this exact java object
  
  //It is important to understand, that the requestbody in "@RequestBody UserPostDTO userPostDTO" is important
  //if the registration page makes a post request we receive a JSON file, then we want to convert it to a UserPostDTO to process it further
  public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
    // convert API user to internal representation  
      // Convert DTO to entity
      User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
      
      
      // Check if the username already exists using the new usernameExists method
      if (userService.usernameExists(userInput.getUsername())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Error: Username already exists");
      }
    
    // create user
    User createdUser = userService.createUser(userInput);
    // convert internal representation of user back to API
    //We convert it to a UserGetDTO since maybe the frontend wants to confirm or access to the username BUT we need to remove sensitive information 
    userService.setUserStatus(createdUser.getToken(), UserStatus.ONLINE);  
    // Convert the created user entity to a DTO and return it
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
  }

  @PostMapping("/login")
  public Map<String, Object> login(@RequestBody Map<String, String> loginData) {
      // Validate input
      String username = loginData.get("username");
      String password = loginData.get("password");
      if (username == null || password == null) {
          Map<String, Object> response = new HashMap<>();
          response.put("success", false);
          response.put("message", "Username or password missing");
          return response;
      }
  
      // Attempt login
      boolean loginSuccessful = userService.login(username, password);
      if (!loginSuccessful) {
          Map<String, Object> response = new HashMap<>();
          response.put("success", false);
          response.put("message", "Invalid username or password");
          return response;
      }
  
      // Login succeeded, proceed with token and user details
      String token = userService.getTokenForUser(username);
      User user = userService.findByUsername(username);
      userService.setUserStatus(token, UserStatus.ONLINE);
  
      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("token", token);
      response.put("userId", user.getId().toString());
  
      return response;
  }

// Get a specific user by their ID
@GetMapping("/users/{userId}")
@ResponseStatus(HttpStatus.OK)
@ResponseBody
public UserGetDTO getUserById(@PathVariable("userId") Long userId) {
    // Fetch the user by ID using the service layer
    User user = userService.getUserById(userId);

    // Check if the user exists
    if (user == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error: User with ID " + userId + " not found");
    }

    // Convert the User entity to a UserGetDTO and return it
    return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
}


  @PostMapping("/logout")
  public ResponseEntity<Map<String, Object>> logout(@RequestBody Map<String, String> logoutData) {
    String token = logoutData.get("token");
    
    System.out.println("Received token: " + token);

    if (token == null || token.isEmpty()) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Token is required"));
    }

    // Set user status to OFFLINE
    userService.setUserStatus(token, UserStatus.OFFLINE);

    // Prepare response
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    response.put("message", "User logged out and status set to OFFLINE");

    return ResponseEntity.ok(response);
}

@PutMapping("/users/{userId}")
public ResponseEntity<?> updateUser(@PathVariable("userId") Long userId, @RequestBody Map<String, String> requestBody) {
    String newUsername = requestBody.get("newUsername");
    String dateOfBirth = requestBody.get("dateOfBirth");

    // Check that only one of newUsername or dateOfBirth is provided
    if ((newUsername != null && !newUsername.trim().isEmpty()) && (dateOfBirth != null && !dateOfBirth.trim().isEmpty())) {
        return ResponseEntity.badRequest().body("Please update only one field (either username or date of birth).");
    }

    if (newUsername != null && !newUsername.trim().isEmpty()) {
        // Update the username
        if (newUsername.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty.");
        }

        try {
            userService.updateUsername(userId, newUsername);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Username update failed: " + e.getMessage());
        }
    }

    if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
        // Update the date of birth
        try {
            userService.setDateOfBirth(userId, dateOfBirth);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Date of birth update failed: " + e.getMessage());
        }
    }

    // Return success response
    Map<String, Object> response = new HashMap<>();
    response.put("success", true);
    return ResponseEntity.noContent().build();  // This will return 204 No Content
}

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> deleteUser(
            @PathVariable("userId") Long userId,
            @RequestHeader(value = "Authorization", required = false) String token) {  //false in case that not in header(we check manually) because if token is missing
      if (token == null || token.isEmpty()){                                           // it will be null, and we check for null ir .isEmpty() here immediately
          return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not authorized to delete user")); //if no token -> return 403 Forbidden
    }
      try {
          User authenticatedUser = userService.findByToken(token); //
          if (!authenticatedUser.getId().equals(userId)) {
              return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You can only delete your own account"));
          }         //if id from request != UserId -> return 403 Forbidden

          userService.deleteUser(authenticatedUser.getId());  //actual deletion
          return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null); //no response body(rest specs (204))
      }
          catch(UserNotFoundException e){
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage())); //404 if not found
          }
      }

}

    









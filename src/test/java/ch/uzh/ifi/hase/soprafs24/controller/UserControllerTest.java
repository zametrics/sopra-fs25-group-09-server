package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UserControllerTest
 * This is a WebMvcTest which allows to test the UserController i.e. GET/POST
 * request without actually sending them over the network.
 * This tests if the UserController works.
 */
@WebMvcTest(UserController.class)
public class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private UserService userService;

  //Test for GET /users
  @Test
  public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
    // given
    User user = new User();
    user.setName("Firstname Lastname");
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);

    List<User> allUsers = Collections.singletonList(user);

    // this mocks the UserService -> we define above what the userService should
    // return when getUsers() is called
    given(userService.getUsers()).willReturn(allUsers);

    // when
    MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

    // then
    mockMvc.perform(getRequest).andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].name", is(user.getName())))
        .andExpect(jsonPath("$[0].username", is(user.getUsername())))
        .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
  }


  //Test for post /users  --> 201 Created status
  @Test
  public void createUser_validInput_userCreated() throws Exception {
    // given
    User user = new User();
    user.setId(1L);
    user.setName("Test User");
    user.setUsername("testUsername");
    user.setToken("1");
    user.setStatus(UserStatus.ONLINE);

    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setName("Test User");
    userPostDTO.setUsername("testUsername");

    given(userService.createUser(Mockito.any())).willReturn(user);

    // when/then -> do the request + validate the result
    MockHttpServletRequestBuilder postRequest = post("/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(userPostDTO));

    // then
    mockMvc.perform(postRequest)
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id", is(user.getId().intValue())))
        .andExpect(jsonPath("$.name", is(user.getName())))
        .andExpect(jsonPath("$.username", is(user.getUsername())))
        .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
  }

  // THE OTHER 4 TESTS

  //Test for post /users --> 409 Conflict 
  @Test
  public void createUser_usernameAlreadyExists_error() throws Exception {
      // Given
      UserPostDTO userPostDTO = new UserPostDTO();
      userPostDTO.setUsername("testUsername");
      userPostDTO.setName("Test User");
      userPostDTO.setPassword("password");
  
      // Mock the userService to return that the username already exists
      given(userService.usernameExists("testUsername")).willReturn(true);
  
      // Perform the POST request to /users and check the response
      mockMvc.perform(post("/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content(asJsonString(userPostDTO)))
              .andExpect(status().isConflict())  // Expect a 409 Conflict status
              .andExpect(jsonPath("$.message").value("Error: Username already exists"));  // Expect the specific error message
  }


  //Test for GET /users{userId} --> code 200 OK
  @Test
  public void getUserProfile_success() throws Exception {
      // Given
      User user = new User();
      user.setId(1L); //1L is not int 1 but like long 1....
      user.setName("Test User");
      user.setUsername("testUsername");
      user.setStatus(UserStatus.ONLINE);  // Assuming status is set to ONLINE
  
      // Mock the UserService to return the user
      given(userService.getUserById(1L)).willReturn(user);
  
      // When/Then - perform the GET request and validate the response
      mockMvc.perform(get("/users/1")
              .contentType(MediaType.APPLICATION_JSON))
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id", is(user.getId().intValue())))
              .andExpect(jsonPath("$.name", is(user.getName())))
              .andExpect(jsonPath("$.username", is(user.getUsername())))
              .andExpect(jsonPath("$.status", is(user.getStatus().toString())))
              .andExpect(jsonPath("$.token").value(is(user.getToken())))  // Assumes token is null
              .andExpect(jsonPath("$.createdAt").value(is(user.getCreatedAt())))  // Assumes createdAt is null
              .andExpect(jsonPath("$.dateOfBirth").value(is(user.getDateOfBirth())));  // Assumes dateOfBirth is null
  }

//Test for GET /users/{id} --> code 404
@Test
public void getUserProfile_userNotFound() throws Exception {
    // Given
    long Id = 999L;  // Assuming this user doesn't exist in the database

    // Mocking the user service to return null when queried (user doesn't exist)
    given(userService.getUserById(Id)).willReturn(null);

    // Perform the GET request and check the response
    mockMvc.perform(get("/users/{Id}", Id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())  // Expect 404 Not Found
            .andExpect(jsonPath("$.message").value("Error: User with ID 999 not found"));
}


// Test for PUT /users/{id} --> 204 No Content (Updating username)
@Test
public void updateUser_usernameValid_success() throws Exception {
    // Given
    long userId = 1L;
    String newUsername = "newUsername";

    // Creating a user with the original username
    User existingUser = new User();
    existingUser.setId(userId);
    existingUser.setUsername("oldUsername");

    // Creating a UserPostDTO with the new username
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername(newUsername);

    // Mock the service to return the updated user
    given(userService.updateUsername(userId, newUsername)).willReturn(existingUser);

    // When and Then
    mockMvc.perform(put("/users/{id}", userId)  // Correct URL formatting
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(userPostDTO)))
        .andExpect(status().isNoContent());  // Expect 204 No Content
}

// Test for PUT /users/{id} --> 404 User not found (Updating username)
@Test
public void updateUser_userNotFound() throws Exception {
    // Given
    Long userId = 999L; // Assume this user does not exist
    String newUsername = "newUsername"; // The new username

    // Mock the user service to throw an exception (user not found)
    given(userService.updateUsername(userId, newUsername)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User with ID " + userId + " not found"));

    // Create the request body (the new username)
    Map<String, String> requestBody = new HashMap<>();
    requestBody.put("newUsername", newUsername);

    // When/Then - perform the PUT request and validate the response
    mockMvc.perform(put("/users/{userId}", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(requestBody)))
            .andExpect(status().isNotFound()) // Expecting 404 Not Found
            .andExpect(jsonPath("$.message").value("User with ID " + userId + " not found")); // Expecting the correct error message
}

  /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
  private String asJsonString(final Object object) {
    try {
      return new ObjectMapper().writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          String.format("The request body could not be created.%s", e.toString()));
    }
  }
}
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
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;





import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


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

    private User testUser;
    private UserGetDTO testUserGetDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setToken("token123");
        testUser.setStatus(ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE);

        testUserGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(testUser);

        objectMapper = new ObjectMapper();
    }

    @Test
    public void getAllUsers_success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        List<UserGetDTO> userGetDTOs = Arrays.asList(testUserGetDTO);

        given(userService.getUsers()).willReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUserGetDTO.getId()))
                .andExpect(jsonPath("$[0].username").value(testUserGetDTO.getUsername()));

        verify(userService, times(1)).getUsers();
    }

    @Test
    public void createUser_success() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("password");

        given(userService.usernameExists("testUser")).willReturn(false);
        given(userService.createUser(any(User.class))).willReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPostDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testUserGetDTO.getId()))
                .andExpect(jsonPath("$.username").value(testUserGetDTO.getUsername()));

        verify(userService, times(1)).usernameExists("testUser");
        verify(userService, times(1)).createUser(any(User.class));
        verify(userService, times(1)).setUserStatus("token123", ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE);
    }

    @Test
    public void createUser_usernameExists_throwsConflict() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUser");
        userPostDTO.setPassword("password");

        given(userService.usernameExists("testUser")).willReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userPostDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Error: Username already exists"));

        verify(userService, times(1)).usernameExists("testUser");
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    public void login_success() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testUser");
        loginData.put("password", "password");

        given(userService.login("testUser", "password")).willReturn(true);
        given(userService.getTokenForUser("testUser")).willReturn("token123");
        given(userService.findByUsername("testUser")).willReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));

        verify(userService, times(1)).login("testUser", "password");
        verify(userService, times(1)).getTokenForUser("testUser");
        verify(userService, times(1)).findByUsername("testUser");
        verify(userService, times(1)).setUserStatus("token123", ch.uzh.ifi.hase.soprafs24.constant.UserStatus.ONLINE);
    }

    @Test
    public void login_missingCredentials_throwsBadRequest() throws Exception {
        Map<String, String> loginData = new HashMap<>();
        loginData.put("username", "testUser");

        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username or password missing"));

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    public void getUserById_success() throws Exception {
        given(userService.getUserById(1L)).willReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserGetDTO.getId()))
                .andExpect(jsonPath("$.username").value(testUserGetDTO.getUsername()));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    public void getUserById_notFound_throwsNotFound() throws Exception {
        given(userService.getUserById(1L)).willReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Error: User with ID 1 not found"));

        verify(userService, times(1)).getUserById(1L);
    }


    @Test
    public void logout_missingToken_throwsBadRequest() throws Exception {
        Map<String, String> logoutData = new HashMap<>();
        logoutData.put("token", "");

        mockMvc.perform(MockMvcRequestBuilders.post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token is required"));

        verify(userService, never()).setUserStatus(anyString(), any());
    }

    @Test
    public void updateUser_bothFieldsProvided_throwsBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newUsername", "newUser");
        requestBody.put("dateOfBirth", "2000-01-01");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Please update only one field (either username or date of birth)."));

        verify(userService, never()).updateUsername(anyLong(), anyString());
        verify(userService, never()).setDateOfBirth(anyLong(), anyString());
    }

    @Test
    public void updateUser_validUsername_success() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newUsername", "newUser");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully."));

        verify(userService, times(1)).updateUsername(1L, "newUser");
        verify(userService, never()).setDateOfBirth(anyLong(), anyString());
    }

    @Test
    public void updateUser_validDateOfBirth_success() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("dateOfBirth", "2000-01-01");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully."));

        verify(userService, times(1)).setDateOfBirth(1L, "2000-01-01");
        verify(userService, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    public void updateUser_invalidUsername_throwsBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("newUsername", "invalidUser");

        doThrow(new IllegalArgumentException("Invalid username")).when(userService).updateUsername(1L, "invalidUser");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username update failed: Invalid username"));

        verify(userService, times(1)).updateUsername(1L, "invalidUser");
        verify(userService, never()).setDateOfBirth(anyLong(), anyString());
    }

    @Test
    public void updateUser_invalidDateOfBirth_throwsBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("dateOfBirth", "invalidDate");

        doThrow(new IllegalArgumentException("Invalid date format")).when(userService).setDateOfBirth(1L, "invalidDate");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Date of birth update failed: Invalid date format"));

        verify(userService, times(1)).setDateOfBirth(1L, "invalidDate");
        verify(userService, never()).updateUsername(anyLong(), anyString());
    }

    @Test
    public void updateAvatarUrl_emptyUrl_throwsBadRequest() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("avatarUrl", "   ");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Avatar URL cannot be empty."));

        verify(userService, never()).updateAvatarUrl(anyLong(), anyString());
    }


    @Test
    public void updateAvatarUrl_userNotFound_throwsNotFound() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("avatarUrl", "http://example.com/avatar.jpg");

        doThrow(new UserNotFoundException("User not found")).when(userService).updateAvatarUrl(1L, "http://example.com/avatar.jpg");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found."));

        verify(userService, times(1)).updateAvatarUrl(1L, "http://example.com/avatar.jpg");
    }

    @Test
    public void updateAvatarUrl_internalError_throwsInternalServerError() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("avatarUrl", "http://example.com/avatar.jpg");

        doThrow(new RuntimeException("Internal error")).when(userService).updateAvatarUrl(1L, "http://example.com/avatar.jpg");

        mockMvc.perform(MockMvcRequestBuilders.put("/users/1/avatar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating avatar URL."));

        verify(userService, times(1)).updateAvatarUrl(1L, "http://example.com/avatar.jpg");
    }

    @Test
    public void deleteUser_success() throws Exception {
        given(userService.findByToken("token123")).willReturn(testUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1")
                .header("Authorization", "token123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).findByToken("token123");
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    public void deleteUser_missingToken_throwsForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Not authorized to delete user"));

        verify(userService, never()).findByToken(anyString());
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    public void deleteUser_unauthorizedUser_throwsForbidden() throws Exception {
        User differentUser = new User();
        differentUser.setId(2L);
        given(userService.findByToken("token123")).willReturn(differentUser);

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1")
                .header("Authorization", "token123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only delete your own account"));

        verify(userService, times(1)).findByToken("token123");
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    public void deleteUser_userNotFound_throwsNotFound() throws Exception {
        given(userService.findByToken("token123")).willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(MockMvcRequestBuilders.delete("/users/1")
                .header("Authorization", "token123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService, times(1)).findByToken("token123");
        verify(userService, never()).deleteUser(anyLong());
    }

    //Test for GET /users
    @Test
    public void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        // given
        User user = new User();
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
            .andExpect(jsonPath("$[0].username", is(user.getUsername())))
            .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }


    //Test for post /users  --> 201 Created status
    @Test
    public void createUser_validInput_userCreated() throws Exception {
        // given
        User user = new User();
        user.setId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.ONLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
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
        user.setUsername("testUsername");
        user.setStatus(UserStatus.ONLINE);  // Assuming status is set to ONLINE
    
        // Mock the UserService to return the user
        given(userService.getUserById(1L)).willReturn(user);
    
        // When/Then - perform the GET request and validate the response
        mockMvc.perform(get("/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
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

    /* 
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
    */

    // Test for PUT /users/{id} --> 404 User not found (Updating username)
    /** 
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
    */
    //Test for deleting account with valid information -> successful deletion
    @Test
    public void deleteUser_usernameValid_success() throws Exception {
        //given
        long userId = 1L;
        String token = "validToken";
        //create user
        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setToken(token);

        given(userService.findByToken(token)).willReturn(existingUser); //simulate valid token scenario
        Mockito.doNothing().when(userService).deleteUser(userId); //does not necessarily need this but clarifies intention

        mockMvc.perform(delete("/users/{userId}", userId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent()); //204
    }
    //Test for trying to delete with wrong id -> return 403 / forbidden
    @Test
    public void deleteUser_unauthorizedUser_fails() throws Exception {
        // given
        long userIdToDelete = 1L;
        long authenticatedUserId = 2L;
        String token = "valid-token";

        // create the authenticated user (different ID)
        User authenticatedUser = new User();
        authenticatedUser.setId(authenticatedUserId);
        authenticatedUser.setToken(token);

        // mock the service to return the authenticated user when finding by token (same as in other test)
        given(userService.findByToken(token)).willReturn(authenticatedUser);

        // When and Then
        mockMvc.perform(delete("/users/{id}", userIdToDelete)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()) // Expect 403 Forbidden
                .andExpect(jsonPath("$.message").value("You can only delete your own account"));
    }


    @Test
        public void deleteUser_missingToken_forbidden() throws Exception {
            // Given
            Long userId = 1L;

            // When/Then
            mockMvc.perform(delete("/users/{userId}", userId)
                    .contentType(MediaType.APPLICATION_JSON)) // No Authorization header
                    .andExpect(status().isForbidden()) // Expect 403 Forbidden
                    .andExpect(jsonPath("$.message").value("Not authorized to delete user"));
        }

        @Test
        public void deleteUser_tokenNotFound_notFound() throws Exception {
            // Given
            Long userId = 1L;
            String token = "invalidToken";

            // Mock the service to throw UserNotFoundException
            given(userService.findByToken(token)).willThrow(new UserNotFoundException("User not found with the provided token: " + token));

            // When/Then
            mockMvc.perform(delete("/users/{userId}", userId)
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound()) // Expect 404 Not Found
                    .andExpect(jsonPath("$.message").value("User not found with the provided token: " + token));
        }



    @Test
        public void login_validCredentials_success() throws Exception {
            // Given
            String username = "testUsername";
            String password = "validPassword";
            String token = "validToken";
            Long userId = 1L;

            User user = new User();
            user.setUsername(username);
            user.setPassword(password); // Assume hashed in practice
            user.setToken(token);
            user.setStatus(UserStatus.ONLINE);
            user.setId(userId);

            // Mock the login service calls
            given(userService.login(username, password)).willReturn(true);
            given(userService.getTokenForUser(username)).willReturn(token);
            given(userService.findByUsername(username)).willReturn(user);

    // Create the request body as a Map
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);

        // when/then -> do the request + validate the result
        MockHttpServletRequestBuilder postRequest = post("/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(loginData));


        mockMvc.perform(postRequest)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.token").value(token))
                    .andExpect(jsonPath("$.userId").value(userId.toString()));
        }

        @Test
        public void login_invalidCredentials_fail() throws Exception {
            // Given
            String username = "testUsername";
            String password = "invalidPassword"; // Invalid password
        
            // No need to set up a full User object since login will fail
            given(userService.login(username, password)).willReturn(false);
            // Optionally mock these if they’re still called despite failure
            given(userService.getTokenForUser(username)).willReturn(null); // Token might not be generated
            given(userService.findByUsername(username)).willReturn(null);  // User might not be found
        
            // Create the request body as a Map
            Map<String, String> loginData = new HashMap<>();
            loginData.put("username", username);
            loginData.put("password", password);
        
            // When/Then -> do the request + validate the result
            MockHttpServletRequestBuilder postRequest = post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(loginData));
        
            mockMvc.perform(postRequest)
                    .andExpect(status().isOk()) // Still 200 OK since the endpoint returns a response
                    .andExpect(jsonPath("$.success").value(false)); // Expect success: false
        }

        @Test
        public void logout_success() throws Exception {
            String token = "validToken";

            Map<String, String> logoutData = new HashMap<>();
            logoutData.put("token", token);

            mockMvc.perform(post("/logout")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(asJsonString(logoutData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User logged out and status set to OFFLINE"));
        }

    @Test
    public void logout_invalidToken_fail() throws Exception {
        // Given an invalid token
        String invalidToken = null;

        Map<String, String> logoutData = new HashMap<>();
        logoutData.put("token", invalidToken);

        // Perform the logout request as a POST with a JSON body
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(logoutData))) // Send token in request body
            .andExpect(status().isBadRequest())  // Expecting 400 Bad Request for invalid token
            .andExpect(jsonPath("$.message").value("Token is required"));  // Assert error message
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

    @Test
    public void updateAvatarUrl_success() throws Exception {
        // given
        Long userId = 1L;
        String newAvatarUrl = "https://example.com/new-avatar.jpg";
        
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setUsername("testUser");
        updatedUser.setAvatarUrl(newAvatarUrl);
        
        // Create request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("avatarUrl", newAvatarUrl);
        
        given(userService.updateAvatarUrl(userId, newAvatarUrl)).willReturn(updatedUser);
        
        // when/then
        mockMvc.perform(put("/users/{userId}/avatar", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(updatedUser.getUsername())))
                .andExpect(jsonPath("$.avatarUrl", is(updatedUser.getAvatarUrl())));
    }
}

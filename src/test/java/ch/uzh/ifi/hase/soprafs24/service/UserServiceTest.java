package ch.uzh.ifi.hase.soprafs24.service;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.exceptions.UserNotFoundException;
import ch.uzh.ifi.hase.soprafs24.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("plainpw");
        when(userRepository.save(any())).thenReturn(testUser);
    }

    @Test
    public void createUser_validInputs_success() {
        // given any save returns testUser
        // when createUser is called
        User createdUser = userService.createUser(testUser);
        // then it returns user with id, username, token, offline status
        verify(userRepository, times(1)).save(any());
        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateInputs_throwsException() {
        // given username already exists
        when(userRepository.findByUsername(any())).thenReturn(testUser);
        // when / then createUser throws bad request
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test
    public void setUserStatus_validToken_updatesStatus() {
        // given user found by token
        testUser.setToken("token1");
        testUser.setStatus(UserStatus.OFFLINE);
        when(userRepository.findByToken("token1")).thenReturn(testUser);
        // when setUserStatus is called
        userService.setUserStatus("token1", UserStatus.ONLINE);
        // then status is updated and saved
        assertEquals(UserStatus.ONLINE, testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void getUsers_returnsAllUsers() {
        // given repository returns two users
        User other = new User();
        other.setId(2L);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, other));
        // when getUsers is called
        List<User> users = userService.getUsers();
        // then list contains both
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void findByUsername_existingUser_returnsUser() {
        // given repository finds user by name
        when(userRepository.findByUsername("foo")).thenReturn(testUser);
        // when findByUsername is called
        User u = userService.findByUsername("foo");
        // then returned user matches
        assertEquals(testUser, u);
    }

    @Test
    public void getTokenForUser_nonExisting_throwsException() {
        // given no user for name
        when(userRepository.findByUsername("nope")).thenReturn(null);
        // when / then throws not found
        assertThrows(UserNotFoundException.class, () -> userService.getTokenForUser("nope"));
    }

    @Test
    public void getTokenForUser_existing_returnsToken() {
        // given user with token
        testUser.setToken("tok");
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when getTokenForUser is called
        String tok = userService.getTokenForUser("testUsername");
        // then returns that token
        assertEquals("tok", tok);
    }

    @Test
    public void login_successfulPassword_returnsTrue() {
        // given user with hashed password
        String hashed = BCrypt.hashpw("pw", BCrypt.gensalt());
        testUser.setPassword(hashed);
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when login with correct pw
        boolean ok = userService.login("testUsername", "pw");
        // then login succeeds
        assertTrue(ok);
    }

    @Test
    public void login_wrongPassword_returnsFalse() {
        // given user with hashed pw
        String hashed = BCrypt.hashpw("pw", BCrypt.gensalt());
        testUser.setPassword(hashed);
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when login with bad pw
        boolean ok = userService.login("testUsername", "bad");
        // then login fails
        assertFalse(ok);
    }

    @Test
    public void login_nonExistingUser_returnsFalse() {
        // given no user in repo
        when(userRepository.findByUsername("none")).thenReturn(null);
        // when login attempted
        boolean ok = userService.login("none", "any");
        // then login fails
        assertFalse(ok);
    }

    @Test
    public void getUserById_existing_returnsUser() {
        // given user found by id
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // when getUserById is called
        User u = userService.getUserById(1L);
        // then returns that user
        assertEquals(testUser, u);
    }

    @Test
    public void getUserById_nonExisting_throwsException() {
        // given no user for id
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        // when / then throws not found
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(2L));
    }


    @Test
    public void setDateOfBirth_nonExisting_throwsException() {
        // given no user for id
        when(userRepository.findById(3L)).thenReturn(Optional.empty());
        // when / then throws entity not found
        assertThrows(EntityNotFoundException.class, () -> userService.setDateOfBirth(3L, "2001-02-02"));
    }

    @Test
    public void updateUsername_successfulChange_returnsUser() {
        // given user exists and new name unused
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("newName")).thenReturn(null);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // when updateUsername is called
        User updated = userService.updateUsername(1L, "newName");
        // then username changed and saved
        assertEquals("newName", updated.getUsername());
    }

    @Test
    public void updateUsername_taken_throwsException() {
        // given another user has that name
        User other = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("dup")).thenReturn(other);
        // when / then throws illegal argument
        assertThrows(IllegalArgumentException.class, () -> userService.updateUsername(1L, "dup"));
    }

    @Test
    public void usernameExists_trueIfFound() {
        // given repository finds user
        when(userRepository.findByUsername("exists")).thenReturn(testUser);
        // when usernameExists is called
        boolean exists = userService.usernameExists("exists");
        // then returns true
        assertTrue(exists);
    }

    @Test
    public void usernameExists_falseIfNotFound() {
        // given no user found
        when(userRepository.findByUsername("nope")).thenReturn(null);
        // when usernameExists is called
        boolean exists = userService.usernameExists("nope");
        // then returns false
        assertFalse(exists);
    }

    @Test
    public void deleteUser_existing_deletesUser() {
        // given user exists
        when(userRepository.existsById(1L)).thenReturn(true);
        // when deleteUser is called
        userService.deleteUser(1L);
        // then deleteById is invoked
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void deleteUser_nonExisting_throwsException() {
        // given no user for id
        when(userRepository.existsById(2L)).thenReturn(false);
        // when / then throws not found
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(2L));
    }

    @Test
    public void findByToken_existing_returnsUser() {
        // given repository finds user by token
        when(userRepository.findByToken("t1")).thenReturn(testUser);
        // when findByToken is called
        User u = userService.findByToken("t1");
        // then returns that user
        assertEquals(testUser, u);
    }

    @Test
    public void findByToken_nonExisting_throwsException() {
        // given no user for token
        when(userRepository.findByToken("bad")).thenReturn(null);
        // when / then throws not found
        assertThrows(UserNotFoundException.class, () -> userService.findByToken("bad"));
    }

    @Test
    public void updateAvatarUrl_existing_updatesAndReturnsUser() {
        // given user found by id
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // when updateAvatarUrl is called
        User u = userService.updateAvatarUrl(1L, "url.png");
        // then avatarUrl set and saved
        assertEquals("url.png", u.getAvatarUrl());
    }

    @Test
    public void updateAvatarUrl_nonExisting_throwsException() {
        // given no user for id
        when(userRepository.findById(4L)).thenReturn(Optional.empty());
        // when / then throws not found
        assertThrows(UserNotFoundException.class, () -> userService.updateAvatarUrl(4L, "u"));
    }

    @Test
    public void getUsers_emptyList_returnsEmpty() {
        // given no users in repo
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        // when getUsers is called
        List<User> users = userService.getUsers();
        // then it returns empty list
        assertTrue(users.isEmpty());
    }

    @Test
    public void findByUsername_nonExisting_returnsNull() {
        // given repo returns null
        when(userRepository.findByUsername("nope")).thenReturn(null);
        // when findByUsername is called
        User u = userService.findByUsername("nope");
        // then it returns null
        assertNull(u);
    }

    @Test
    public void setUserStatus_invalidToken_doesNotSave() {
        // given no user for token
        when(userRepository.findByToken("bad")).thenReturn(null);
        // when setUserStatus is called
        userService.setUserStatus("bad", UserStatus.ONLINE);
        // then save is never invoked
        verify(userRepository, never()).save(any());
    }

    @Test
    public void createUser_passwordIsHashed() {
        // given new user with plain password
        User newUser = new User();
        newUser.setUsername("u1");
        newUser.setPassword("pw");
        when(userRepository.findByUsername("u1")).thenReturn(null);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // when createUser is called
        User created = userService.createUser(newUser);
        // then password is hashed and matches original
        assertNotEquals("pw", created.getPassword());
        assertTrue(BCrypt.checkpw("pw", created.getPassword()));
    }

    @Test
    public void createUser_callsFlushOnce() {
        // given new user
        User newUser = new User();
        newUser.setUsername("u2");
        newUser.setPassword("pw2");
        when(userRepository.findByUsername("u2")).thenReturn(null);
        when(userRepository.save(any())).thenReturn(newUser);
        // when createUser is called
        userService.createUser(newUser);
        // then flush is invoked once
        verify(userRepository, times(1)).flush();
    }
    @Test
    public void updateUsername_nonExistingId_throwsException() {
        // given no user for id
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        // when / then updateUsername throws ResponseStatusException
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
            userService.updateUsername(5L, "newName")
        );

    }

}

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class UserServiceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        // base user for most tests
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("plainpw");

        // default save behaviour – can be overridden per test
        when(userRepository.save(any())).thenReturn(testUser);
    }


    @Test
    public void createUser_validInputs_success() {
        // given any save returns testUser
        // when createUser is called
        User createdUser = userService.createUser(testUser);
        // then one save, props set, status offline
        verify(userRepository, times(1)).save(any());
        assertEquals(testUser.getId(), createdUser.getId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.OFFLINE, createdUser.getStatus());
    }

    @Test
    public void createUser_duplicateInputs_throwsException() {
        // given username already taken
        when(userRepository.findByUsername(any())).thenReturn(testUser);
        // when / then
        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }



    @Test
    public void login_validCredentials_returnsTrue() {
        // given hashed pw stored in user
        String hash = BCrypt.hashpw("plainpw", BCrypt.gensalt());
        testUser.setPassword(hash);
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when
        boolean ok = userService.login("testUsername", "plainpw");
        // then
        assertTrue(ok);
    }

    @Test
    public void login_wrongPassword_returnsFalse() {
        // given hashed pw but wrong attempt
        String hash = BCrypt.hashpw("plainpw", BCrypt.gensalt());
        testUser.setPassword(hash);
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when
        boolean ok = userService.login("testUsername", "wrong");
        // then
        assertFalse(ok);
    }

    @Test
    public void login_nonExistingUser_returnsFalse() {
        // given none found
        when(userRepository.findByUsername("ghost")).thenReturn(null);
        // when
        boolean ok = userService.login("ghost", "pw");
        // then
        assertFalse(ok);
    }


    @Test
    public void setUserStatus_validToken_updatesStatus() {
        // given user found by token
        testUser.setToken("token1");
        testUser.setStatus(UserStatus.OFFLINE);
        when(userRepository.findByToken("token1")).thenReturn(testUser);
        // when
        userService.setUserStatus("token1", UserStatus.ONLINE);
        // then value changed and saved
        assertEquals(UserStatus.ONLINE, testUser.getStatus());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void getUsers_returnsAllUsers() {
        // given repo returns two users
        User other = new User();
        other.setId(2L);
        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, other));
        // when
        List<User> users = userService.getUsers();
        // then both present
        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }


    @Test
    public void findByUsername_existingUser_returnsUser() {
        // given
        when(userRepository.findByUsername("foo")).thenReturn(testUser);
        // when
        User u = userService.findByUsername("foo");
        // then
        assertEquals(testUser, u);
    }

    @Test
    public void getTokenForUser_nonExisting_throwsException() {
        // given none found
        when(userRepository.findByUsername("nope")).thenReturn(null);
        // when / then
        assertThrows(UserNotFoundException.class, () -> userService.getTokenForUser("nope"));
    }

    @Test
    public void getTokenForUser_existing_returnsToken() {
        // given token set
        testUser.setToken("tok");
        when(userRepository.findByUsername("testUsername")).thenReturn(testUser);
        // when
        String tok = userService.getTokenForUser("testUsername");
        // then
        assertEquals("tok", tok);
    }



    @Test
    public void updateUsername_success() {
        // given id resolves, new username free
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("newName")).thenReturn(null);
        // when
        User updated = userService.updateUsername(1L, "newName");
        // then
        assertEquals("newName", updated.getUsername());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    public void updateUsername_duplicate_throwsException() {
        // given id resolves, new username taken
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("dup")).thenReturn(new User());
        // when / then
        assertThrows(IllegalArgumentException.class, () -> userService.updateUsername(1L, "dup"));
        verify(userRepository, never()).save(any());
    }


    @Test
    public void setDateOfBirth_updatesField() {
        // given id resolves
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // when
        userService.setDateOfBirth(1L, "1990-01-01");
        // then
        assertEquals("1990-01-01", testUser.getDateOfBirth());
        verify(userRepository, atLeastOnce()).save(testUser);
    }

    @Test
    public void setDateOfBirth_noUser_throwsException() {
        // given none found
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        // when / then
        assertThrows(EntityNotFoundException.class, () -> userService.setDateOfBirth(99L, "2000-01-01"));
    }


    @Test
    public void deleteUser_existing_deletes() {
        // given user exists
        when(userRepository.existsById(1L)).thenReturn(true);
        // when
        userService.deleteUser(1L);
        // then delete called
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void deleteUser_nonExisting_throwsException() {
        // given user not there
        when(userRepository.existsById(1L)).thenReturn(false);
        // when / then
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(anyLong());
    }


    @Test
    public void findByToken_valid_returnsUser() {
        // given
        when(userRepository.findByToken("tok")).thenReturn(testUser);
        // when
        User u = userService.findByToken("tok");
        // then
        assertEquals(testUser, u);
    }

    @Test
    public void findByToken_invalid_throwsException() {
        // given none
        when(userRepository.findByToken("bad")).thenReturn(null);
        // when / then
        assertThrows(UserNotFoundException.class, () -> userService.findByToken("bad"));
    }
}

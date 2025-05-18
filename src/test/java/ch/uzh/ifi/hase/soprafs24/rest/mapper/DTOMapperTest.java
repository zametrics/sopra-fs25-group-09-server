package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * DTOMapperTest
 * Tests if the mapping between the internal and the external/API representation
 * works.
 */
public class DTOMapperTest {
  @Test
  public void testCreateUser_fromUserPostDTO_toUser_success() {
    // create UserPostDTO
    UserPostDTO userPostDTO = new UserPostDTO();
    userPostDTO.setUsername("username");

    // MAP -> Create user
    User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

    // check content
    assertEquals(userPostDTO.getUsername(), user.getUsername());
  }

  @Test
  public void testGetUser_fromUser_toUserGetDTO_success() {
    // create User
    User user = new User();
    user.setUsername("firstname@lastname");
    user.setStatus(UserStatus.OFFLINE);
    user.setToken("1");

    // MAP -> Create UserGetDTO
    UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

    // check content
    assertEquals(user.getId(), userGetDTO.getId());
    assertEquals(user.getUsername(), userGetDTO.getUsername());
    assertEquals(user.getStatus(), userGetDTO.getStatus());
  }

    @Test
    public void testCreateUser_fromUserPostDTO_passwordAndDateOfBirth_success() {
        // create UserPostDTO with username, password and dateOfBirth
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("alice");
        userPostDTO.setPassword("secret");
        userPostDTO.setDateOfBirth("1990-05-20");

        // MAP -> Create User entity
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // check that username, password and dateOfBirth are copied
        assertEquals("alice", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("1990-05-20", user.getDateOfBirth());

        // check that ignored fields remain null
        assertEquals(null, user.getId());
        assertEquals(null, user.getToken());
        assertEquals(null, user.getStatus());
        assertEquals(null, user.getCreatedAt());
    }

    @Test
    public void testGetUser_fullEntity_toUserGetDTO_success() {
        // create User with all fields
        User user = new User();
        user.setId(42L);
        user.setUsername("bob");
        user.setStatus(UserStatus.ONLINE);
        user.setToken("tok-123");
        user.setDateOfBirth("1985-12-31");
        user.setAvatarUrl("http://avatar.url/img.png");

        // MAP -> Create UserGetDTO
        UserGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        // check that all mapped fields match
        assertEquals(42L, dto.getId());
        assertEquals("bob", dto.getUsername());
        assertEquals(UserStatus.ONLINE, dto.getStatus());
        assertEquals("tok-123", dto.getToken());
        assertEquals("1985-12-31", dto.getDateOfBirth());
        assertEquals("http://avatar.url/img.png", dto.getAvatarUrl());
    }

    @Test
    public void testCreateUser_passwordMapping_success() {
        UserPostDTO dto = new UserPostDTO();
        dto.setUsername("user1");
        dto.setPassword("mypw");
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);
        assertEquals("mypw", user.getPassword());
    }

    @Test
    public void testCreateUser_dateOfBirthNull_success() {
        UserPostDTO dto = new UserPostDTO();
        dto.setUsername("user2");
        // dateOfBirth not set
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);
        assertEquals(null, user.getDateOfBirth());
    }

    @Test
    public void testCreateUser_ignoredFields_areNull() {
        UserPostDTO dto = new UserPostDTO();
        dto.setUsername("user3");
        dto.setPassword("pw3");
        dto.setDateOfBirth("1999-12-31");
        User user = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(dto);
        assertEquals(null, user.getId());
        assertEquals(null, user.getToken());
        assertEquals(null, user.getStatus());
        assertEquals(null, user.getCreatedAt());
    }

    @Test
    public void testGetUser_tokenAndAvatarMapping_success() {
        User user = new User();
        user.setToken("tok123");
        user.setAvatarUrl("http://img");
        UserGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        assertEquals("tok123", dto.getToken());
        assertEquals("http://img", dto.getAvatarUrl());
    }

    @Test
    public void testGetUser_dateOfBirthMapping_success() {
        User user = new User();
        user.setDateOfBirth("2000-01-01");
        UserGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        assertEquals("2000-01-01", dto.getDateOfBirth());
    }

    @Test
    public void testGetUser_unsetFields_areNull() {
        User user = new User();
        UserGetDTO dto = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
        assertEquals(null, dto.getToken());
        assertEquals(null, dto.getDateOfBirth());
        assertEquals(null, dto.getAvatarUrl());
    }


}

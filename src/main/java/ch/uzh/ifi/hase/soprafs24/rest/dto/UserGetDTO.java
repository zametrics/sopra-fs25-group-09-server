package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;
import java.time.LocalDateTime;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

@Data
public class UserGetDTO {
    private Long id;
    private String username;
    private UserStatus status;
    private String token;
    private LocalDateTime createdAt;
    private String dateOfBirth;  // Date of Birth as a string
    private String avatarUrl;
}

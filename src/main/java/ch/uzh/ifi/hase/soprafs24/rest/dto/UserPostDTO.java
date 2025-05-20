package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserPostDTO {
    private String name;
    private String username;
    private String password;
    private String token;
    private LocalDateTime createdAt;
    private String dateOfBirth;  // Date of Birth as a string
    private String avatarUrl;
}

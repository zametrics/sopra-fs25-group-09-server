package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.time.LocalDateTime;
import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;

/*When converting to a UserGetDTO through the mapper, we will return this recipe, in contrast to POSTDTO, we will send back infromation
 * without sensitive data, as you can see there is no password here.
*/

public class UserGetDTO {

  private Long id;
  private String name;
  private String username;
  private UserStatus status;
  private String token;
  private LocalDateTime createdAt;

  private String dateOfBirth;  // Date of Birth as a string

  // Getter and Setter for dateOfBirth
  public String getDateOfBirth() {
      return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
  }
  
  // Getters and Setters
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
  }
  

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
  

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }
}

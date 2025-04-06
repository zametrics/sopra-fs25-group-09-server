package ch.uzh.ifi.hase.soprafs24.rest.dto;
import java.time.LocalDateTime;

 //userPostDTO object is neede to convert e.g to an entity
public class UserPostDTO {

  private String name;

  private String username;

  private String password;  

  private String token;

  private LocalDateTime createdAt;

  
  private String dateOfBirth;  // Date of Birth as a string

  private String avatarUrl;  // Add this field

  // Getters and setters
  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  // Getter and Setter for dateOfBirth
  public String getDateOfBirth() {
      return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
      this.dateOfBirth = dateOfBirth;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
  
  // Getters and Setters
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
  }
  
  // Getter for the password field
    public String getPassword() {
      return password;
  }
  
  // Setter for the password field
    public void setPassword(String password) {
      this.password = password;
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
}

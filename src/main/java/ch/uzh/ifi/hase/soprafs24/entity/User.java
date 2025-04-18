package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.time.LocalDateTime;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unqiue across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "USER")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String token;
  
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserStatus status;
  
  //@Column(nullable = false)
  private String password;

  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "date_of_birth")
  private String dateOfBirth;  // Date of Birth as a string


  @Column(name = "Avatar_URL")
  private String avatarUrl;  


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

  // Automatically set createdAt before inserting into DB
  @PrePersist
  protected void onCreate() {
      this.createdAt = LocalDateTime.now();
      this.avatarUrl = "https://i.pinimg.com/originals/0f/68/94/0f6894e539589a50809e45833c8bb6c4.jpg";
    }
    
  // Getters and Setters
  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
      this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public UserStatus getStatus() {
    return status;
  }

  public void setStatus(UserStatus status) {
    this.status = status;
  }

  public String getPassword() { 
    return password; 
  }

  public void setPassword(String password) { 
    this.password = password; 
  }


}

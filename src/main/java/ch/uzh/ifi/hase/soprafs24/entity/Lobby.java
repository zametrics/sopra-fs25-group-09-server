package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
=======
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "LOBBY")
public class Lobby implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
<<<<<<< HEAD
  @Column(nullable = false)
  private Long id;
=======
  @GeneratedValue
  private Long id; // maybe this would be better with a hashed value
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5

  @Column(nullable = false)
  private Long numOfMaxPlayers;

  @Column(nullable = false)
<<<<<<< HEAD
  private String playerIds = "";  // Store as comma-separated values
  
  @Column(nullable = false)
  private Long lobbyOwner;  // New field to store the lobby owner's ID
=======
  //private List<User> listOfPlayers = new ArrayList<User>();
  private String listOfPlayers;
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5

  @Column(nullable = false)
  private String wordset;
  
  @Column(nullable = false)
  private Long numOfRounds;
<<<<<<< HEAD
  
  @Column(nullable = false)
  private int drawTime;

  // Constructor with random ID generation
  public Lobby() {
    // Generate a random 6-digit ID (100000-999999)
    Random random = new Random();
    this.id = 100000L + random.nextInt(900000);
  }

=======

  /*
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

<<<<<<< HEAD
  // Convert string to list when getting
  public List<Long> getPlayerIds() {
    if (playerIds == null || playerIds.isEmpty()) {
      return new ArrayList<>();
    }
    return Arrays.stream(playerIds.split(","))
           .map(Long::parseLong)
           .collect(Collectors.toList());
  }

  // Convert list to string when setting
  public void setPlayerIds(List<Long> playerIdList) {
    this.playerIds = playerIdList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
  }
  
  public void addPlayerId(Long playerId) {
    List<Long> currentIds = getPlayerIds();
    currentIds.add(playerId);
    setPlayerIds(currentIds);
  }
  
  public void removePlayerId(Long playerId) {
    List<Long> currentIds = getPlayerIds();
    currentIds.remove(playerId);
    setPlayerIds(currentIds);
  }

  // Getter and setter for the lobby owner
  public Long getLobbyOwner() {
    return lobbyOwner;
  }

  public void setLobbyOwner(Long lobbyOwner) {
    this.lobbyOwner = lobbyOwner;
=======
  public List<User> getListOfPlayers() {
    return listOfPlayers;
  }

  public void setListOfPlayer(List<User> listOfPlayers) {
    this.listOfPlayers = listOfPlayers;
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5
  }

  public String getWordset() {
    return wordset;
  }

  public void setWordset(String wordset) {
    this.wordset = wordset;
  }

  public Long getNumOfMaxPlayers() {
    return numOfMaxPlayers;
  }

  public void setNumOfMaxPlayers(Long numOfMaxPlayers) {
    this.numOfMaxPlayers = numOfMaxPlayers;
  }

  public Long getNumOfRounds() {
    return numOfRounds;
  }

  public void setNumOfRounds(Long numOfRounds) {
    this.numOfRounds = numOfRounds;
  }
<<<<<<< HEAD
  
  public int getDrawTime() {
    return drawTime;
  }
  
  public void setDrawTime(int drawTime) {
    this.drawTime = drawTime;
  }
}
=======

  */
}
>>>>>>> f8dd06d022d06ce364cc1260fbee7bbb8a05dbf5

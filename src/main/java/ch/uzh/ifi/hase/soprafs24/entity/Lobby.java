package ch.uzh.ifi.hase.soprafs24.entity;

import ch.uzh.ifi.hase.soprafs24.constant.UserStatus;
import java.time.LocalDateTime;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "LOBBY")
public class Lobby implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long id; // maybe this would be better with a hashed value

  @Column(nullable = false)
  private Long numOfMaxPlayers;

  @Column(nullable = false)
  //private List<User> listOfPlayers = new ArrayList<User>();
  private String listOfPlayers;

  @Column(nullable = false)
  private String wordset;
  
  @Column(nullable = false)
  private Long numOfRounds;

  /*
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<User> getListOfPlayers() {
    return listOfPlayers;
  }

  public void setListOfPlayer(List<User> listOfPlayers) {
    this.listOfPlayers = listOfPlayers;
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

  */
}

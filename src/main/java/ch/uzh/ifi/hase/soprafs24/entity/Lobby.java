package ch.uzh.ifi.hase.soprafs24.entity;

import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "LOBBY")
public class Lobby implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(nullable = false)
  private Long id;

  @Column(nullable = false)
  private Long numOfMaxPlayers;

  @Column(nullable = false)
  private String playerIds = "";  // Store as comma-separated values
  
  @Column(nullable = false)
  private Long lobbyOwner;  // New field to store the lobby owner's ID

  @Column(nullable = false)
  private String language;
  
  @Column(nullable = false)
  private Long numOfRounds;
  
  @Column(nullable = false)
  private int drawTime;

  @Column(nullable = false)
  private String type = "anything";  // Default value

  @Column(nullable = true)
  private String currentPainterToken;

  @Column(nullable = false)
  private String painterHistoryTokens = "";

  @Column(nullable = false)
  private String CurrentWord = "default_word";


  // Constructor with random ID generation
  public Lobby() {
    // Generate a random 6-digit ID (100000-999999)
    Random random = new Random();
    this.id = 100000L + random.nextInt(900000);
  }

  public String getCurrentWord() {
    return CurrentWord;
}

  public void setCurrentWord(String word) {
    this.CurrentWord = word;
  }


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

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


    // Use a Set internally for easier checking, but store as comma-separated string
    public Set<String> getPainterHistoryTokens() {
        if (painterHistoryTokens == null || painterHistoryTokens.isEmpty()) {
            return new HashSet<>(); // Return an empty Set
        }
        // Split the string and collect into a Set
        return new HashSet<>(Arrays.asList(painterHistoryTokens.split(",")));
    }

    // Accept a Set and convert to string for storage
    public void setPainterHistoryTokens(Set<String> painterHistoryTokenSet) {
        if (painterHistoryTokenSet == null || painterHistoryTokenSet.isEmpty()) {
            this.painterHistoryTokens = "";
        } else {
            this.painterHistoryTokens = String.join(",", painterHistoryTokenSet);
        }
    }


    // Method to add a token to the history
    public void addTokenToPainterHistory(String playerToken) {
        // Get the Set of current history tokens
        Set<String> currentTokens = getPainterHistoryTokens(); // Variable is 'currentTokens' (plural)
        if (currentTokens.add(playerToken)) { // Check if adding the token modified the set
            setPainterHistoryTokens(currentTokens); // Update the stored string using the modified set
        }
    }

    // Method to clear the history (start of new cycle)
    public void clearPainterHistory() {
        setPainterHistoryTokens(Collections.emptySet()); // Use helper method
    }

  // Getter and setter for the lobby owner
  public Long getLobbyOwner() {
    return lobbyOwner;
  }

  public void setLobbyOwner(Long lobbyOwner) {
    this.lobbyOwner = lobbyOwner;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
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
  
  public int getDrawTime() {
    return drawTime;
  }
  
  public void setDrawTime(int drawTime) {
    this.drawTime = drawTime;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

    public String getCurrentPainterToken() {
        return this.currentPainterToken;
    }

    // Setter
    public void setCurrentPainterToken(String currentPainterToken) {
        this.currentPainterToken = currentPainterToken;
    }

}

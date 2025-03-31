package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class LobbyPutDTO {

    private Long id;
    private Long lobbyOwner;
    private Long numOfMaxPlayers;
    private List<Long> playerIds;
    private String wordset;
    private Long numOfRounds;
    private int drawTime;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLobbyOwner() {
        return lobbyOwner;
    }

    public void setLobbyOwner(Long lobbyOwner) {
        this.lobbyOwner = lobbyOwner;
    }

    public Long getNumOfMaxPlayers() {
        return numOfMaxPlayers;
    }

    public void setNumOfMaxPlayers(Long numOfMaxPlayers) {
        this.numOfMaxPlayers = numOfMaxPlayers;
    }

    public List<Long> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<Long> playerIds) {
        this.playerIds = playerIds;
    }

    public String getWordset() {
        return wordset;
    }

    public void setWordset(String wordset) {
        this.wordset = wordset;
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
}
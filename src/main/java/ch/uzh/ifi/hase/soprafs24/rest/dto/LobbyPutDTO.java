package ch.uzh.ifi.hase.soprafs24.rest.dto;

import java.util.List;

public class LobbyPutDTO {

    private Long id;
    private Long lobbyOwner;
    private Long numOfMaxPlayers;
    private List<Long> playerIds;
    private String language;
    private Long numOfRounds;
    private int drawTime;
    private String type;
    private int status;

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

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class LobbyGetDTO {
    private Long id;
    private Long lobbyOwner;
    private Long numOfMaxPlayers;
    private List<Long> playerIds;
    private String language;
    private Long numOfRounds;
    private int drawTime;
    private String type;
    private String currentPainterToken;
    private List<String> painterHistoryTokens;
    private String currentWord; 
    private int status;
}

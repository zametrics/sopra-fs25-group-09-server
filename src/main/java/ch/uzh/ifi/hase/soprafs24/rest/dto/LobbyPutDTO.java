package ch.uzh.ifi.hase.soprafs24.rest.dto;

import lombok.Data;
import java.util.List;

@Data
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
}

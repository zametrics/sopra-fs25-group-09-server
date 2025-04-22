package ch.uzh.ifi.hase.soprafs24.rest.mapper;

import ch.uzh.ifi.hase.soprafs24.entity.Lobby;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.LobbyPutDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LobbyDTOMapper {

    LobbyDTOMapper INSTANCE = Mappers.getMapper(LobbyDTOMapper.class);
    
    @Mapping(source = "lobbyOwner", target = "lobbyOwner")
    @Mapping(source = "numOfMaxPlayers", target = "numOfMaxPlayers")
    @Mapping(source = "playerIds", target = "playerIds")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "type", target = "type")
    @Mapping(target = "currentPainterToken", ignore = true)
    @Mapping(target = "painterHistoryTokens", ignore = true)
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyOwner", target = "lobbyOwner")
    @Mapping(source = "numOfMaxPlayers", target = "numOfMaxPlayers")
    @Mapping(source = "playerIds", target = "playerIds")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "currentPainterToken", target = "currentPainterToken")
    @Mapping(source = "painterHistoryTokens", target = "painterHistoryTokens")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyOwner", target = "lobbyOwner")
    @Mapping(source = "numOfMaxPlayers", target = "numOfMaxPlayers")
    @Mapping(source = "playerIds", target = "playerIds")
    @Mapping(source = "language", target = "language")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    @Mapping(source = "type", target = "type")
    @Mapping(target = "currentPainterToken", ignore = true)
    @Mapping(target = "painterHistoryTokens", ignore = true)
    Lobby convertLobbyPutDTOtoEntity(LobbyPutDTO lobbyPutDTO);
}
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
    @Mapping(source = "wordset", target = "wordset")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    @Mapping(target = "id", ignore = true)
    Lobby convertLobbyPostDTOtoEntity(LobbyPostDTO lobbyPostDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyOwner", target = "lobbyOwner")
    @Mapping(source = "numOfMaxPlayers", target = "numOfMaxPlayers")
    @Mapping(source = "playerIds", target = "playerIds")
    @Mapping(source = "wordset", target = "wordset")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    LobbyGetDTO convertEntityToLobbyGetDTO(Lobby lobby);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "lobbyOwner", target = "lobbyOwner")
    @Mapping(source = "numOfMaxPlayers", target = "numOfMaxPlayers")
    @Mapping(source = "playerIds", target = "playerIds")
    @Mapping(source = "wordset", target = "wordset")
    @Mapping(source = "numOfRounds", target = "numOfRounds")
    @Mapping(source = "drawTime", target = "drawTime")
    Lobby convertLobbyPutDTOtoEntity(LobbyPutDTO lobbyPutDTO);
}
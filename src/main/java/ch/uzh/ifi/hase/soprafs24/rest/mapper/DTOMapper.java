package ch.uzh.ifi.hase.soprafs24.rest.mapper;
import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

  DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

  @Mapping(source = "username", target = "username")
  @Mapping(source = "password", target = "password") // Added this mapping for the password field
  @Mapping(target = "id", ignore = true) 
  @Mapping(target = "token", ignore = true) 
  @Mapping(target = "status", ignore = true) 
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(source = "dateOfBirth", target = "dateOfBirth") 
  User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "username", target = "username")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "token", target = "token")
  @Mapping(source = "createdAt", target = "createdAt")
  @Mapping(source = "dateOfBirth", target = "dateOfBirth")
  @Mapping(source = "avatarUrl", target = "avatarUrl")
  UserGetDTO convertEntityToUserGetDTO(User user);
}

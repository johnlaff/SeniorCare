package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.web.dto.request.UserRequest;
import br.com.uniube.seniorcare.web.dto.response.UserResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    User toEntity(UserRequest dto);

    @Mapping(target = "organizationId", source = "organization.id")
    UserResponse toDto(User entity);

    List<UserResponse> toDtoList(List<User> entities);

    @Named("organizationFromId")
    default Organization organizationFromId(UUID id) {
        if (id == null) return null;
        Organization org = new Organization();
        org.setId(id);
        return org;
    }
}
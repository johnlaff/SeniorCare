package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.web.dto.request.CaregiverRequest;
import br.com.uniube.seniorcare.web.dto.response.CaregiverResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CaregiverMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    Caregiver toEntity(CaregiverRequest dto);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "userId", source = "user.id")
    CaregiverResponse toDto(Caregiver entity);

    List<CaregiverResponse> toDtoList(List<Caregiver> entities);

    @Named("organizationFromId")
    default Organization organizationFromId(UUID id) {
        if (id == null) return null;
        Organization org = new Organization();
        org.setId(id);
        return org;
    }

    @Named("userFromId")
    default User userFromId(UUID id) {
        if (id == null) return null;
        User user = new User();
        user.setId(id);
        return user;
    }
}


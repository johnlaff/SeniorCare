package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.web.dto.request.CaregiverRequest;
import br.com.uniube.seniorcare.web.dto.response.CaregiverResponse;
import br.com.uniube.seniorcare.web.dto.response.OrganizationSummary;
import br.com.uniube.seniorcare.web.dto.response.UserSummary;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CaregiverMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    Caregiver toEntity(CaregiverRequest dto);

    @Mapping(target = "organization", expression = "java(toOrganizationSummary(entity.getOrganization()))")
    @Mapping(target = "user", expression = "java(toUserSummary(entity.getUser()))")
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

    default OrganizationSummary toOrganizationSummary(Organization org) {
        if (org == null) return null;
        OrganizationSummary summary = new OrganizationSummary();
        summary.setId(org.getId());
        summary.setName(org.getName());
        summary.setDomain(org.getDomain());
        return summary;
    }

    default UserSummary toUserSummary(User user) {
        if (user == null) return null;
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setName(user.getName());
        summary.setEmail(user.getEmail());
        return summary;
    }
}

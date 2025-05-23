package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.web.dto.request.FamilyMemberRequest;
import br.com.uniube.seniorcare.web.dto.response.ElderlySummary;
import br.com.uniube.seniorcare.web.dto.response.FamilyMemberResponse;
import br.com.uniube.seniorcare.web.dto.response.OrganizationSummary;
import br.com.uniube.seniorcare.web.dto.response.UserSummary;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FamilyMemberMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    @Mapping(target = "elderly", source = "elderlyId", qualifiedByName = "elderlyFromId")
    FamilyMember toEntity(FamilyMemberRequest dto);

    @Mapping(target = "organization", expression = "java(toOrganizationSummary(entity.getOrganization()))")
    @Mapping(target = "user", expression = "java(toUserSummary(entity.getUser()))")
    @Mapping(target = "elderly", expression = "java(toElderlySummary(entity.getElderly()))")
    FamilyMemberResponse toDto(FamilyMember entity);

    List<FamilyMemberResponse> toDtoList(List<FamilyMember> entities);

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

    @Named("elderlyFromId")
    default Elderly elderlyFromId(UUID id) {
        if (id == null) return null;
        Elderly elderly = new Elderly();
        elderly.setId(id);
        return elderly;
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

    default ElderlySummary toElderlySummary(Elderly elderly) {
        if (elderly == null) return null;
        ElderlySummary summary = new ElderlySummary();
        summary.setId(elderly.getId());
        summary.setName(elderly.getName());
        summary.setBirthDate(elderly.getBirthDate());
        return summary;
    }
}

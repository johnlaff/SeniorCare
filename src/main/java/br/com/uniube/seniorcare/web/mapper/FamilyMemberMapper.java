package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.web.dto.request.FamilyMemberRequest;
import br.com.uniube.seniorcare.web.dto.response.FamilyMemberResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FamilyMemberMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    @Mapping(target = "user", source = "userId", qualifiedByName = "userFromId")
    @Mapping(target = "elderly", source = "elderlyId", qualifiedByName = "elderlyFromId")
    FamilyMember toEntity(FamilyMemberRequest dto);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "elderlyId", source = "elderly.id")
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
}


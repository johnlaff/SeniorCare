package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.web.dto.request.ElderlyRequest;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.web.dto.response.CaregiverSummary;
import br.com.uniube.seniorcare.web.dto.response.FamilyMemberSummary;
import br.com.uniube.seniorcare.web.dto.response.UserSummary;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElderlyMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    Elderly toEntity(ElderlyRequest dto);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "caregivers", ignore = true)
    @Mapping(target = "familyMembers", ignore = true)
    ElderlyResponse toDto(Elderly entity);

    List<ElderlyResponse> toDtoList(List<Elderly> entities);

    @Named("organizationFromId")
    default Organization organizationFromId(UUID id) {
        if (id == null) return null;
        Organization org = new Organization();
        org.setId(id);
        return org;
    }

    // Métodos auxiliares para popular os vínculos
    default List<CaregiverSummary> toCaregiverSummaryList(List<Caregiver> caregivers) {
        if (caregivers == null) return null;
        return caregivers.stream().map(this::toCaregiverSummary).collect(Collectors.toList());
    }

    default CaregiverSummary toCaregiverSummary(Caregiver caregiver) {
        if (caregiver == null) return null;
        CaregiverSummary summary = new CaregiverSummary();
        summary.setId(caregiver.getId());
        summary.setSpecialty(caregiver.getSpecialty());
        summary.setUser(toUserSummary(caregiver.getUser()));
        return summary;
    }

    default List<FamilyMemberSummary> toFamilyMemberSummaryList(List<FamilyMember> familyMembers) {
        if (familyMembers == null) return null;
        return familyMembers.stream().map(this::toFamilyMemberSummary).collect(Collectors.toList());
    }

    default FamilyMemberSummary toFamilyMemberSummary(FamilyMember familyMember) {
        if (familyMember == null) return null;
        FamilyMemberSummary summary = new FamilyMemberSummary();
        summary.setId(familyMember.getId());
        summary.setRelationship(familyMember.getRelationship());
        summary.setUser(toUserSummary(familyMember.getUser()));
        return summary;
    }

    default UserSummary toUserSummary(br.com.uniube.seniorcare.domain.entity.User user) {
        if (user == null) return null;
        UserSummary summary = new UserSummary();
        summary.setId(user.getId());
        summary.setName(user.getName());
        summary.setEmail(user.getEmail());
        return summary;
    }

    // Método utilitário para montar ElderlyResponse enriquecido sem recursividade
    default ElderlyResponse toEnrichedElderlyResponse(Elderly elderly, List<Caregiver> caregivers, List<FamilyMember> familyMembers) {
        ElderlyResponse response = toDto(elderly);
        response.setCaregivers(toCaregiverSummaryList(caregivers));
        response.setFamilyMembers(toFamilyMemberSummaryList(familyMembers));
        return response;
    }
}

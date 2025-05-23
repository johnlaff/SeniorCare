package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.web.dto.request.ElderlyRequest;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ElderlyMapper {
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    Elderly toEntity(ElderlyRequest dto);

    @Mapping(target = "organizationId", source = "organization.id")
    ElderlyResponse toDto(Elderly entity);

    List<ElderlyResponse> toDtoList(List<Elderly> entities);

    @Named("organizationFromId")
    default Organization organizationFromId(UUID id) {
        if (id == null) return null;
        Organization org = new Organization();
        org.setId(id);
        return org;
    }
}


package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.web.dto.request.OrganizationRequest;
import br.com.uniube.seniorcare.web.dto.response.OrganizationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization toEntity(OrganizationRequest dto);

    OrganizationResponse toDto(Organization entity);

    List<OrganizationResponse> toDtoList(List<Organization> entities);
}
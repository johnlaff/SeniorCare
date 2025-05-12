package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User; // Necessário para o nome do cuidador
import br.com.uniube.seniorcare.web.dto.request.AppointmentRequest;
import br.com.uniube.seniorcare.web.dto.response.AppointmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppointmentMapper {

    @Mapping(target = "id", ignore = true) // O ID será gerado pelo banco ou definido no serviço
    @Mapping(target = "organization", source = "organizationId", qualifiedByName = "organizationFromId")
    @Mapping(target = "elderly", source = "elderlyId", qualifiedByName = "elderlyFromId")
    @Mapping(target = "caregiver", source = "caregiverId", qualifiedByName = "caregiverFromId")
    @Mapping(target = "status", ignore = true) // Status será definido no serviço
    @Mapping(target = "createdAt", ignore = true) // createdAt será definido pelo @CreationTimestamp
    Appointment toEntity(AppointmentRequest dto);

    @Mapping(target = "organizationId", source = "entity.organization.id")
    @Mapping(target = "elderly.id", source = "entity.elderly.id")
    @Mapping(target = "elderly.name", source = "entity.elderly.name")
    @Mapping(target = "caregiver.id", source = "entity.caregiver.id")
    @Mapping(target = "caregiver.name", source = "entity.caregiver.user.name") // Assumindo que o Caregiver tem um User com nome
    @Mapping(target = "caregiver.specialty", source = "entity.caregiver.specialty")
    AppointmentResponse toDto(Appointment entity);

    List<AppointmentResponse> toDtoList(List<Appointment> entities);

    // --- Métodos Qualificadores para IDs ---

    @Named("organizationFromId")
    default Organization organizationFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    @Named("elderlyFromId")
    default Elderly elderlyFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Elderly elderly = new Elderly();
        elderly.setId(id);
        return elderly;
    }

    @Named("caregiverFromId")
    default Caregiver caregiverFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Caregiver caregiver = new Caregiver();
        caregiver.setId(id);
        return caregiver;
    }
}
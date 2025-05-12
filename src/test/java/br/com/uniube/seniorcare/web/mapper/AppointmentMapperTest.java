package br.com.uniube.seniorcare.web.mapper;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import br.com.uniube.seniorcare.web.dto.request.AppointmentRequest;
import br.com.uniube.seniorcare.web.dto.response.AppointmentResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentMapperTest {

    private AppointmentMapper appointmentMapper;

    private UUID organizationId;
    private UUID elderlyId;
    private UUID caregiverId;
    private UUID userId; // Para o User dentro do Caregiver

    @BeforeEach
    void setUp() {
        appointmentMapper = Mappers.getMapper(AppointmentMapper.class);
        organizationId = UUID.randomUUID();
        elderlyId = UUID.randomUUID();
        caregiverId = UUID.randomUUID();
        userId = UUID.randomUUID();
    }

    @Test
    void toEntity_shouldMapCorrectly_whenGivenValidDto() {
        // Arrange
        LocalDateTime futureDateTime = LocalDateTime.now().plusDays(1);
        AppointmentRequest dto = AppointmentRequest.builder()
                .organizationId(organizationId)
                .elderlyId(elderlyId)
                .caregiverId(caregiverId)
                .dateTime(futureDateTime)
                .description("Check-up routine")
                .build();

        // Act
        Appointment entity = appointmentMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals(futureDateTime, entity.getDateTime());
        assertEquals("Check-up routine", entity.getDescription());

        assertNotNull(entity.getOrganization());
        assertEquals(organizationId, entity.getOrganization().getId());

        assertNotNull(entity.getElderly());
        assertEquals(elderlyId, entity.getElderly().getId());

        assertNotNull(entity.getCaregiver());
        assertEquals(caregiverId, entity.getCaregiver().getId());

        // Campos ignorados no mapeamento de DTO para entidade
        assertNull(entity.getId());
        assertNull(entity.getStatus()); // Status é definido pelo serviço
        assertNull(entity.getCreatedAt()); // Definido pelo @CreationTimestamp
    }

    @Test
    void toDto_shouldMapCorrectly_whenGivenValidEntity() {
        // Arrange
        Organization org = new Organization();
        org.setId(organizationId);

        Elderly elderly = new Elderly();
        elderly.setId(elderlyId);
        elderly.setName("João Silva");

        User caregiverUser = new User();
        caregiverUser.setId(userId);
        caregiverUser.setName("Maria Cuidadora");

        Caregiver caregiver = new Caregiver();
        caregiver.setId(caregiverId);
        caregiver.setUser(caregiverUser);
        caregiver.setSpecialty("Geriatria");

        Appointment entity = Appointment.builder()
                .id(UUID.randomUUID())
                .organization(org)
                .elderly(elderly)
                .caregiver(caregiver)
                .dateTime(LocalDateTime.now().plusHours(2))
                .description("Acompanhamento")
                .status(AppointmentStatus.AGENDADO)
                .createdAt(LocalDateTime.now())
                .build();

        // Act
        AppointmentResponse dto = appointmentMapper.toDto(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(organizationId, dto.getOrganizationId());
        assertEquals(entity.getDateTime(), dto.getDateTime());
        assertEquals("Acompanhamento", dto.getDescription());
        assertEquals(AppointmentStatus.AGENDADO, dto.getStatus());
        assertEquals(entity.getCreatedAt(), dto.getCreatedAt());

        assertNotNull(dto.getElderly());
        assertEquals(elderlyId, dto.getElderly().getId());
        assertEquals("João Silva", dto.getElderly().getName());

        assertNotNull(dto.getCaregiver());
        assertEquals(caregiverId, dto.getCaregiver().getId());
        assertEquals("Maria Cuidadora", dto.getCaregiver().getName());
        assertEquals("Geriatria", dto.getCaregiver().getSpecialty());
    }

    @Test
    void toDtoList_shouldMapListOfEntities_toListOfDtos() {
        // Arrange
        Organization org = new Organization();
        org.setId(organizationId);

        Elderly elderly1 = new Elderly();
        elderly1.setId(UUID.randomUUID());
        elderly1.setName("Elderly One");

        User caregiverUser1 = new User();
        caregiverUser1.setId(UUID.randomUUID());
        caregiverUser1.setName("Caregiver User One");

        Caregiver caregiver1 = new Caregiver();
        caregiver1.setId(UUID.randomUUID());
        caregiver1.setUser(caregiverUser1);
        caregiver1.setSpecialty("Specialty One");

        Appointment entity1 = Appointment.builder().id(UUID.randomUUID()).organization(org).elderly(elderly1).caregiver(caregiver1).status(AppointmentStatus.AGENDADO).build();
        Appointment entity2 = Appointment.builder().id(UUID.randomUUID()).organization(org).elderly(elderly1).caregiver(caregiver1).status(AppointmentStatus.CONCLUIDO).build();
        List<Appointment> entities = List.of(entity1, entity2);

        // Act
        List<AppointmentResponse> dtoList = appointmentMapper.toDtoList(entities);

        // Assert
        assertNotNull(dtoList);
        assertEquals(2, dtoList.size());
        assertEquals(entity1.getId(), dtoList.get(0).getId());
        assertEquals(AppointmentStatus.AGENDADO, dtoList.get(0).getStatus());
        assertEquals(entity2.getId(), dtoList.get(1).getId());
        assertEquals(AppointmentStatus.CONCLUIDO, dtoList.get(1).getStatus());
    }
}
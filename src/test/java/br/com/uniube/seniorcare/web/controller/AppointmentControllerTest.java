package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.service.AppointmentService;
import br.com.uniube.seniorcare.service.CaregiverService; // Para simular a busca do Caregiver ID
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import br.com.uniube.seniorcare.web.dto.request.AppointmentObservationRequest;
import br.com.uniube.seniorcare.web.dto.request.AppointmentRequest;
import br.com.uniube.seniorcare.web.dto.request.AppointmentStatusUpdateRequest;
import br.com.uniube.seniorcare.web.dto.response.AppointmentResponse;
import br.com.uniube.seniorcare.web.mapper.AppointmentMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private AppointmentMapper appointmentMapper;

    @Mock
    private SecurityUtils securityUtils;

    // Adicione este mock se for usá-lo para resolver Caregiver.id a partir de User.id
    // @Mock
    // private CaregiverService caregiverService; // Ou CaregiverRepository

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID organizationId;
    private UUID elderlyId;
    private UUID caregiverId;
    private UUID appointmentId;
    private UUID currentUserId; // Para simular o usuário logado

    @BeforeEach
    void setUp() {
        // Configuração do MockMvc para simular requisições HTTP
        // O GlobalExceptionHandler não será registrado automaticamente aqui.
        // Para testar o GlobalExceptionHandler, você precisaria de um @WebMvcTest ou carregar o contexto.
        // Estes testes focam na lógica do controller.
        mockMvc = MockMvcBuilders.standaloneSetup(appointmentController)
                // Você pode adicionar seu GlobalExceptionHandler aqui se quiser testar o fluxo de exceção
                // .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para serializar/desserializar LocalDateTime

        organizationId = UUID.randomUUID();
        elderlyId = UUID.randomUUID();
        caregiverId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        currentUserId = UUID.randomUUID();
    }

    private AppointmentRequest createAppointmentRequest() {
        return AppointmentRequest.builder()
                .organizationId(organizationId)
                .elderlyId(elderlyId)
                .caregiverId(caregiverId)
                .dateTime(LocalDateTime.now().plusDays(1))
                .description("Test appointment")
                .build();
    }

    private Appointment createAppointmentEntity() {
        Organization org = new Organization();
        org.setId(organizationId);
        Elderly elderly = new Elderly();
        elderly.setId(elderlyId);
        User user = new User(); user.setId(UUID.randomUUID()); user.setName("Caregiver User");
        Caregiver caregiver = new Caregiver();
        caregiver.setId(caregiverId);
        caregiver.setUser(user);

        return Appointment.builder()
                .id(appointmentId)
                .organization(org)
                .elderly(elderly)
                .caregiver(caregiver)
                .dateTime(LocalDateTime.now().plusDays(1))
                .description("Test appointment")
                .status(AppointmentStatus.AGENDADO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private AppointmentResponse createAppointmentResponse() {
        return AppointmentResponse.builder()
                .id(appointmentId)
                .organizationId(organizationId)
                .elderly(AppointmentResponse.ElderlyBasicResponse.builder().id(elderlyId).name("Elderly Test").build())
                .caregiver(AppointmentResponse.CaregiverBasicResponse.builder().id(caregiverId).name("Caregiver Test").specialty("Test").build())
                .dateTime(LocalDateTime.now().plusDays(1))
                .description("Test appointment")
                .status(AppointmentStatus.AGENDADO)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAppointment_shouldReturnCreated_whenAdminAndDataIsValid() throws Exception {
        // Arrange
        AppointmentRequest requestDto = createAppointmentRequest();
        Appointment entity = createAppointmentEntity(); // Mapper converteria o DTO para isto (sem ID, status, createdAt)
        Appointment createdEntity = createAppointmentEntity(); // Serviço retornaria isto (com ID, status, createdAt)
        AppointmentResponse responseDto = createAppointmentResponse();

        when(appointmentMapper.toEntity(any(AppointmentRequest.class))).thenReturn(entity);
        when(appointmentService.createAppointment(any(Appointment.class))).thenReturn(createdEntity);
        when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()))
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()));

        verify(appointmentService).createAppointment(any(Appointment.class));
    }

    @Test
    void getAllAppointments_shouldReturnAppointments_whenAdmin() throws Exception {
        // Arrange
        List<Appointment> entities = List.of(createAppointmentEntity());
        List<AppointmentResponse> responses = List.of(createAppointmentResponse());

        when(appointmentService.findAll()).thenReturn(entities); // Simplificado, sem filtros por agora
        when(appointmentMapper.toDtoList(entities)).thenReturn(responses);

        // Act & Assert
        mockMvc.perform(get("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(responses.get(0).getId().toString()));

        verify(appointmentService).findAll();
    }


    @Test
    void getAppointmentById_shouldReturnAppointment_whenExists() throws Exception {
        // Arrange
        Appointment entity = createAppointmentEntity();
        AppointmentResponse responseDto = createAppointmentResponse();

        when(appointmentService.findById(appointmentId)).thenReturn(entity);
        when(appointmentMapper.toDto(entity)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get("/api/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()));

        verify(appointmentService).findById(appointmentId);
    }

    @Test
    void updateAppointment_shouldReturnUpdated_whenAdminAndDataIsValid() throws Exception {
        // Arrange
        AppointmentRequest requestDto = createAppointmentRequest();
        Appointment entityToUpdate = createAppointmentEntity(); // Mapper converteria
        Appointment updatedEntity = createAppointmentEntity(); // Serviço retornaria
        updatedEntity.setDescription("Updated Description");
        AppointmentResponse responseDto = createAppointmentResponse();
        responseDto.setDescription("Updated Description");


        when(appointmentMapper.toEntity(any(AppointmentRequest.class))).thenReturn(entityToUpdate);
        when(appointmentService.updateAppointment(eq(appointmentId), any(Appointment.class))).thenReturn(updatedEntity);
        when(appointmentMapper.toDto(any(Appointment.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseDto.getId().toString()))
                .andExpect(jsonPath("$.description").value("Updated Description"));

        verify(appointmentService).updateAppointment(eq(appointmentId), any(Appointment.class));
    }

    @Test
    void updateAppointmentStatusByAdmin_shouldReturnUpdated_whenAdminAndStatusIsValid() throws Exception {
        // Arrange
        AppointmentStatusUpdateRequest statusRequest = new AppointmentStatusUpdateRequest(AppointmentStatus.EM_ANDAMENTO);
        Appointment updatedEntity = createAppointmentEntity();
        updatedEntity.setStatus(AppointmentStatus.EM_ANDAMENTO);
        AppointmentResponse responseDto = createAppointmentResponse();
        responseDto.setStatus(AppointmentStatus.EM_ANDAMENTO);

        when(appointmentService.updateStatus(appointmentId, AppointmentStatus.EM_ANDAMENTO)).thenReturn(updatedEntity);
        when(appointmentMapper.toDto(updatedEntity)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(patch("/api/appointments/{id}/status", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AppointmentStatus.EM_ANDAMENTO.toString()));

        verify(appointmentService).updateStatus(appointmentId, AppointmentStatus.EM_ANDAMENTO);
    }

    @Test
    void cancelAppointment_shouldReturnCancelledAppointment_whenAdmin() throws Exception {
        // Arrange
        Appointment cancelledEntity = createAppointmentEntity();
        cancelledEntity.setStatus(AppointmentStatus.CANCELADO);
        AppointmentResponse responseDto = createAppointmentResponse();
        responseDto.setStatus(AppointmentStatus.CANCELADO);

        when(appointmentService.cancelAppointment(appointmentId)).thenReturn(cancelledEntity);
        when(appointmentMapper.toDto(cancelledEntity)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(delete("/api/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Seu controller retorna 200 OK com o objeto cancelado
                .andExpect(jsonPath("$.status").value(AppointmentStatus.CANCELADO.toString()));

        verify(appointmentService).cancelAppointment(appointmentId);
    }

    @Test
    void addObservation_shouldReturnUpdatedAppointment_whenAuthorized() throws Exception {
        // Arrange
        AppointmentObservationRequest observationRequest = new AppointmentObservationRequest("Nova observação.");
        Appointment updatedEntity = createAppointmentEntity();
        updatedEntity.setDescription(updatedEntity.getDescription() + "\n\n--- " + LocalDateTime.now() + " ---\n" + "Nova observação.");
        AppointmentResponse responseDto = createAppointmentResponse(); // Ajuste a descrição conforme a lógica do serviço
        responseDto.setDescription(updatedEntity.getDescription());


        when(appointmentService.addObservation(appointmentId, "Nova observação.")).thenReturn(updatedEntity);
        when(appointmentMapper.toDto(updatedEntity)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/{id}/observations", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(observationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(responseDto.getDescription()));

        verify(appointmentService).addObservation(appointmentId, "Nova observação.");
    }


    // --- Testes para Endpoints de Cuidador ---

    @Test
    void getMySchedule_shouldReturnCaregiverAppointments_whenCaregiver() throws Exception {
        // Arrange
        when(securityUtils.getCurrentUserId()).thenReturn(currentUserId);

        // ---- ATENÇÃO: PONTO DE AJUSTE CRÍTICO NO SEU CÓDIGO E NESTE TESTE ----
        // Você precisa de uma forma de mockar a conversão de currentUserId (User.id) para Caregiver.id
        // Supondo que você injetou CaregiverRepository ou CaregiverService e tem um método.
        // Exemplo:
        // Caregiver mockCaregiver = new Caregiver(); mockCaregiver.setId(caregiverId);
        // when(caregiverServiceOuRepository.findByUser_Id(currentUserId)).thenReturn(Optional.of(mockCaregiver));
        // Este teste vai falhar ou dar erro se a lógica no controller não for ajustada.
        // Por agora, vamos mockar o appointmentService.findByCaregiver diretamente se a lógica fosse ajustada.

        // Para este teste funcionar como está, o controller precisaria já ter o caregiverId.
        // Vamos assumir, para fins de demonstração do teste, que a lógica de busca do caregiverId
        // está implementada e o service é chamado com o ID correto do Caregiver.
        // **VOCÊ DEVERÁ AJUSTAR ESTE TESTE APÓS CORRIGIR O CONTROLLER**

        List<Appointment> entities = List.of(createAppointmentEntity());
        List<AppointmentResponse> responses = List.of(createAppointmentResponse());

        // Simulação de que o caregiverId foi obtido e passado para o serviço
        // Este when() simula que o controller já obteve 'caregiverId' corretamente
        when(appointmentService.findByCaregiver(any(UUID.class))).thenReturn(entities); // Alterado para any() para este exemplo
        when(appointmentMapper.toDtoList(entities)).thenReturn(responses);

        // Act & Assert
        // Se o controller não for ajustado, o UnsupportedOperationException será lançado.
        // Este teste passará se você ajustar o controller para chamar appointmentService.findByCaregiver(caregiverIdResolvido)
        // e o caregiverIdResolvido for o que foi usado no when() acima.

        // Se você comentar a exceção no controller e mockar a obtenção do caregiverId:
        mockMvc.perform(get("/api/appointments/my-schedule")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Espera-se OK se a lógica for corrigida
                .andExpect(jsonPath("$[0].id").value(responses.get(0).getId().toString()));

        verify(securityUtils).getCurrentUserId();
        // verify(caregiverServiceOuRepository).findByUser_Id(currentUserId); // Você verificaria isso
        verify(appointmentService).findByCaregiver(any(UUID.class)); // Verifique com o caregiverId correto
    }

    @Test
    void startService_shouldUpdateStatusToEmAndamento_whenCaregiverOwnsAppointment() throws Exception {
        // Arrange
        Appointment appointment = createAppointmentEntity(); // Caregiver está associado aqui
        Appointment updatedEntity = createAppointmentEntity();
        updatedEntity.setStatus(AppointmentStatus.EM_ANDAMENTO);
        AppointmentResponse responseDto = createAppointmentResponse();
        responseDto.setStatus(AppointmentStatus.EM_ANDAMENTO);

        // Supondo que a validação de propriedade do agendamento é feita no serviço
        when(appointmentService.findById(appointmentId)).thenReturn(appointment); // Chamado no controller
        when(appointmentService.updateStatus(appointmentId, AppointmentStatus.EM_ANDAMENTO)).thenReturn(updatedEntity);
        when(appointmentMapper.toDto(updatedEntity)).thenReturn(responseDto);
        // quando securityUtils.getCurrentUserId() for chamado, retornará currentUserId
        // when(securityUtils.getCurrentUserId()).thenReturn(appointment.getCaregiver().getUser().getId()); // Para simular que o usuário logado é o dono

        // Act & Assert
        mockMvc.perform(post("/api/appointments/{id}/start-service", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AppointmentStatus.EM_ANDAMENTO.toString()));

        verify(appointmentService).updateStatus(appointmentId, AppointmentStatus.EM_ANDAMENTO);
    }


    @Test
    void completeService_shouldUpdateStatusToConcluido_whenCaregiverOwnsAppointment() throws Exception {
        // Arrange
        AppointmentObservationRequest observationRequest = new AppointmentObservationRequest("Serviço concluído bem.");
        Appointment appointment = createAppointmentEntity();
        Appointment updatedEntity = createAppointmentEntity();
        updatedEntity.setStatus(AppointmentStatus.CONCLUIDO);
        updatedEntity.setDescription(appointment.getDescription() + " observação adicionada"); // Simulação

        AppointmentResponse responseDto = createAppointmentResponse();
        responseDto.setStatus(AppointmentStatus.CONCLUIDO);
        responseDto.setDescription(updatedEntity.getDescription());


        when(appointmentService.findById(appointmentId)).thenReturn(appointment); // Chamado no controller
        when(appointmentService.updateStatus(appointmentId, AppointmentStatus.CONCLUIDO)).thenReturn(updatedEntity); // Primeiro status é atualizado
        when(appointmentService.addObservation(appointmentId, observationRequest.getObservation())).thenReturn(updatedEntity); // Depois observação
        when(appointmentMapper.toDto(updatedEntity)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/appointments/{id}/complete-service", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(observationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(AppointmentStatus.CONCLUIDO.toString()));

        verify(appointmentService).updateStatus(appointmentId, AppointmentStatus.CONCLUIDO);
        verify(appointmentService).addObservation(appointmentId, observationRequest.getObservation());
    }
}
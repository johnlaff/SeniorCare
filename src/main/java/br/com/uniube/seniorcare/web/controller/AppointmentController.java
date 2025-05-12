package br.com.uniube.seniorcare.web.controller;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import br.com.uniube.seniorcare.service.AppointmentService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import br.com.uniube.seniorcare.web.dto.request.AppointmentObservationRequest;
import br.com.uniube.seniorcare.web.dto.request.AppointmentRequest;
import br.com.uniube.seniorcare.web.dto.request.AppointmentStatusUpdateRequest;
import br.com.uniube.seniorcare.web.dto.response.AppointmentResponse;
import br.com.uniube.seniorcare.web.mapper.AppointmentMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
@Tag(name = "Agendamentos (Escalas)", description = "API para gerenciamento de agendamentos e escalas de cuidado")
@SecurityRequirement(name = "bearer-jwt") // Aplica a exigência de JWT para todos os endpoints deste controller
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentMapper appointmentMapper;
    private final SecurityUtils securityUtils; // Para obter o ID do cuidador logado

    // --- Endpoints para Administradores ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Criar novo agendamento (escala)",
            description = "Permite que um administrador crie um novo agendamento para um idoso com um cuidador.")
    @ApiResponse(responseCode = "201", description = "Agendamento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de negócio (ex: conflito de horário)")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentRequest requestDto) {
        Appointment appointment = appointmentMapper.toEntity(requestDto);
        // A lógica de validação de organização, existência de idoso/cuidador, conflitos, etc.,
        // está no appointmentService.createAppointment()
        Appointment createdAppointment = appointmentService.createAppointment(appointment);
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentMapper.toDto(createdAppointment));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos os agendamentos (Admin)",
            description = "Retorna uma lista de todos os agendamentos, com filtros opcionais.")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments(
            @Parameter(description = "ID do Idoso para filtrar os agendamentos") @RequestParam(required = false) UUID elderlyId,
            @Parameter(description = "ID do Cuidador para filtrar os agendamentos") @RequestParam(required = false) UUID caregiverId,
            @Parameter(description = "Data/Hora inicial do período para filtrar (formato YYYY-MM-DDTHH:MM:SS)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @Parameter(description = "Data/Hora final do período para filtrar (formato YYYY-MM-DDTHH:MM:SS)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime) {

        List<Appointment> appointments;
        if (elderlyId != null) {
            appointments = appointmentService.findByElderly(elderlyId);
        } else if (caregiverId != null) {
            appointments = appointmentService.findByCaregiver(caregiverId);
        } else if (startDateTime != null && endDateTime != null) {
            appointments = appointmentService.findByPeriod(startDateTime, endDateTime);
        } else {
            appointments = appointmentService.findAll(); // Idealmente, este findAll() deveria ser paginado e/ou restrito à organização
        }
        return ResponseEntity.ok(appointmentMapper.toDtoList(appointments));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER')") // Admin pode ver qualquer um, Cuidador pode ver se estiver relacionado (lógica no serviço idealmente)
    @Operation(summary = "Buscar agendamento por ID",
            description = "Retorna os detalhes de um agendamento específico.")
    @ApiResponse(responseCode = "200", description = "Agendamento encontrado")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        Appointment appointment = appointmentService.findById(id);
        // Adicionar verificação se o cuidador logado tem permissão para ver este appointment específico,
        // caso não seja admin. O serviço pode ter essa lógica.
        return ResponseEntity.ok(appointmentMapper.toDto(appointment));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar agendamento",
            description = "Atualiza os dados de um agendamento existente.")
    @ApiResponse(responseCode = "200", description = "Agendamento atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou erro de negócio")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> updateAppointment(@PathVariable UUID id, @Valid @RequestBody AppointmentRequest requestDto) {
        Appointment appointmentToUpdate = appointmentMapper.toEntity(requestDto);
        Appointment updatedAppointment = appointmentService.updateAppointment(id, appointmentToUpdate);
        return ResponseEntity.ok(appointmentMapper.toDto(updatedAppointment));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')") // Admin pode mudar qualquer status, dentro das regras de transição
    @Operation(summary = "Atualizar status de um agendamento (Admin)",
            description = "Permite que um administrador atualize o status de um agendamento.")
    @ApiResponse(responseCode = "200", description = "Status do agendamento atualizado")
    @ApiResponse(responseCode = "400", description = "Status inválido ou transição de status não permitida")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatusByAdmin(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentStatusUpdateRequest statusUpdateRequest) {
        Appointment updatedAppointment = appointmentService.updateStatus(id, statusUpdateRequest.getStatus());
        return ResponseEntity.ok(appointmentMapper.toDto(updatedAppointment));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancelar/Excluir agendamento",
            description = "Cancela um agendamento (muda status para CANCELADO) ou o remove, dependendo da regra de negócio.")
    @ApiResponse(responseCode = "200", description = "Agendamento cancelado com sucesso")
    @ApiResponse(responseCode = "204", description = "Agendamento excluído com sucesso (se aplicável)")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "400", description = "Não é possível cancelar/excluir (ex: já concluído)")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable UUID id) {
        // O método cancelAppointment no serviço já define o status para CANCELADO.
        Appointment cancelledAppointment = appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(appointmentMapper.toDto(cancelledAppointment));
        // Se fosse exclusão física, seria ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/observations")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAREGIVER')")
    @Operation(summary = "Adicionar observação a um agendamento",
            description = "Permite que um administrador ou cuidador adicione uma observação a um agendamento.")
    @ApiResponse(responseCode = "200", description = "Observação adicionada com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: observação vazia)")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> addObservation(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentObservationRequest observationRequest) {
        Appointment updatedAppointment = appointmentService.addObservation(id, observationRequest.getObservation());
        return ResponseEntity.ok(appointmentMapper.toDto(updatedAppointment));
    }


    // --- Endpoints para Cuidadores ---

    @GetMapping("/my-schedule")
    @PreAuthorize("hasRole('CAREGIVER')")
    @Operation(summary = "Visualizar minha escala (Cuidador)",
            description = "Retorna a lista de agendamentos (passados, presentes e futuros) para o cuidador autenticado.")
    @ApiResponse(responseCode = "200", description = "Operação bem-sucedida")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<List<AppointmentResponse>> getMySchedule() {
        UUID caregiverUserId = securityUtils.getCurrentUserId();
        // Precisamos buscar o ID da entidade Caregiver associada a este UserID.
        // Isso pode exigir uma busca no CaregiverRepository pelo userId
        // Ex: Caregiver caregiver = caregiverRepository.findByUserId(caregiverUserId).orElseThrow(...);
        // UUID caregiverEntityId = caregiver.getId();
        // List<Appointment> appointments = appointmentService.findByCaregiver(caregiverEntityId);

        // --- ATENÇÃO: Ajuste necessário aqui ---
        // O `appointmentService.findByCaregiver` espera o ID da entidade `Caregiver`.
        // O `securityUtils.getCurrentUserId()` retorna o ID do `User`.
        // Você precisará de um passo para obter o `Caregiver.id` a partir do `User.id` do cuidador.
        // Exemplo (você precisará injetar CaregiverRepository ou ter um método no CaregiverService):
        // Caregiver currentCaregiver = caregiverRepository.findByUser_Id(caregiverUserId)
        //                            .orElseThrow(() -> new BusinessException("Perfil de cuidador não encontrado para o usuário logado"));
        // List<Appointment> appointments = appointmentService.findByCaregiver(currentCaregiver.getId());
        // Por ora, vou simular que você tem esse ID. Substitua pela lógica real.
        // Este é um ponto IMPORTANTE para você ajustar no seu código real.
        // Vou deixar um placeholder para a lógica de busca do caregiverId a partir do userId.

        // --- INÍCIO DO AJUSTE NECESSÁRIO ---
        // Exemplo conceitual (você precisará implementar isso de forma robusta):
        // Supondo que você adicione um método em CaregiverService ou use diretamente o repo:
        // UUID caregiverEntityId = caregiverService.findCaregiverIdByUserId(caregiverUserId);
        // List<Appointment> appointments = appointmentService.findByCaregiver(caregiverEntityId);
        // --- FIM DO AJUSTE NECESSÁRIO ---

        // Simulação para o código compilar (REMOVA E IMPLEMENTE O CORRETO):
        if (true) { // Esta condição é só para o exemplo, remova-a.
            throw new UnsupportedOperationException("Lógica pendente: buscar Caregiver ID a partir do User ID (" + caregiverUserId + ") e então chamar appointmentService.findByCaregiver()");
        }
        List<Appointment> appointments = List.of(); // Linha de placeholder


        return ResponseEntity.ok(appointmentMapper.toDtoList(appointments));
    }

    @PostMapping("/{id}/start-service")
    @PreAuthorize("hasRole('CAREGIVER')")
    @Operation(summary = "Iniciar atendimento (Cuidador)",
            description = "Permite que o cuidador marque o início de um atendimento, mudando o status para EM_ANDAMENTO.")
    @ApiResponse(responseCode = "200", description = "Atendimento iniciado com sucesso")
    @ApiResponse(responseCode = "400", description = "Transição de status não permitida")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> startService(@PathVariable UUID id) {
        // Adicionar verificação se o cuidador logado é o cuidador do agendamento.
        // Esta lógica pode estar no serviço.
        Appointment appointment = appointmentService.findById(id); // carrega o appointment
        // Validação de permissão (cuidador do agendamento == cuidador logado) aqui ou no serviço
        // UUID caregiverUserId = securityUtils.getCurrentUserId();
        // if (!appointment.getCaregiver().getUser().getId().equals(caregiverUserId)) {
        //    throw new AccessDeniedException("Você não tem permissão para iniciar este atendimento.");
        // }

        Appointment updatedAppointment = appointmentService.updateStatus(id, AppointmentStatus.EM_ANDAMENTO);
        return ResponseEntity.ok(appointmentMapper.toDto(updatedAppointment));
    }

    @PostMapping("/{id}/complete-service")
    @PreAuthorize("hasRole('CAREGIVER')")
    @Operation(summary = "Finalizar atendimento (Cuidador)",
            description = "Permite que o cuidador marque o fim de um atendimento (status para CONCLUIDO) e adicione uma observação.")
    @ApiResponse(responseCode = "200", description = "Atendimento finalizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou transição de status não permitida")
    @ApiResponse(responseCode = "404", description = "Agendamento não encontrado")
    @ApiResponse(responseCode = "401", description = "Não autorizado")
    @ApiResponse(responseCode = "403", description = "Acesso negado")
    public ResponseEntity<AppointmentResponse> completeService(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) AppointmentObservationRequest observationRequest) { // Observação opcional
        // Adicionar verificação se o cuidador logado é o cuidador do agendamento.
        Appointment appointment = appointmentService.findById(id); // carrega o appointment
        // Validação de permissão aqui ou no serviço

        Appointment completedAppointment = appointmentService.updateStatus(id, AppointmentStatus.CONCLUIDO);
        if (observationRequest != null && observationRequest.getObservation() != null && !observationRequest.getObservation().isBlank()) {
            completedAppointment = appointmentService.addObservation(id, observationRequest.getObservation());
        }
        return ResponseEntity.ok(appointmentMapper.toDto(completedAppointment));
    }
}
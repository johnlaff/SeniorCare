package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.AppointmentRepository;
import br.com.uniube.seniorcare.domain.repository.CaregiverRepository;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.service.AppointmentService;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ElderlyRepository elderlyRepository;
    private final CaregiverRepository caregiverRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                 ElderlyRepository elderlyRepository,
                                 CaregiverRepository caregiverRepository,
                                 AuditService auditService,
                                 SecurityUtils securityUtils) {
        this.appointmentRepository = appointmentRepository;
        this.elderlyRepository = elderlyRepository;
        this.caregiverRepository = caregiverRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Override
    public Appointment findById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Agendamento não encontrado com o id: " + id));
    }

    @Override
    public Appointment createAppointment(Appointment appointment) {
        validateAppointmentData(appointment);

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(appointment.getElderly().getId())
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + appointment.getElderly().getId()));

        // Verifica se o cuidador existe
        Caregiver caregiver = caregiverRepository.findById(appointment.getCaregiver().getId())
                .orElseThrow(() -> new BusinessException("Cuidador não encontrado com o id: " + appointment.getCaregiver().getId()));

        // Verifica se a data é futura
        if (appointment.getDateTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("A data e hora do agendamento deve ser futura");
        }

        // Verifica conflito de horário
        if (hasScheduleConflict(caregiver.getId(), appointment.getDateTime(), 60, null)) {
            throw new BusinessException("Existe um conflito de horário para este cuidador no período solicitado");
        }

        // Define o status inicial como AGENDADO
        appointment.setStatus(AppointmentStatus.AGENDADO);

        Appointment createdAppointment = appointmentRepository.save(appointment);

        auditService.recordEvent(
                appointment.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_APPOINTMENT",
                "Agendamento",
                createdAppointment.getId(),
                "Agendamento criado para o idoso: " + elderly.getName()
        );

        return createdAppointment;
    }

    @Override
    public Appointment updateAppointment(UUID id, Appointment updatedAppointment) {
        Appointment appointment = findById(id);

        // Não permite atualizar agendamentos cancelados ou concluídos
        if (appointment.getStatus() == AppointmentStatus.CANCELADO ||
            appointment.getStatus() == AppointmentStatus.CONCLUIDO) {
            throw new BusinessException("Não é possível atualizar agendamentos cancelados ou concluídos");
        }

        // Verificar se a data está sendo alterada
        if (!appointment.getDateTime().equals(updatedAppointment.getDateTime())) {
            // Verifica se a nova data é futura
            if (updatedAppointment.getDateTime().isBefore(LocalDateTime.now())) {
                throw new BusinessException("A data e hora do agendamento deve ser futura");
            }

            // Verifica conflito na nova data
            if (hasScheduleConflict(appointment.getCaregiver().getId(),
                                  updatedAppointment.getDateTime(), 60, id)) {
                throw new BusinessException("Existe um conflito de horário para este cuidador no período solicitado");
            }
        }

        // Atualiza os campos permitidos
        appointment.setDateTime(updatedAppointment.getDateTime());
        appointment.setDescription(updatedAppointment.getDescription());

        Appointment updated = appointmentRepository.save(appointment);

        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_APPOINTMENT",
                "Agendamento",
                updated.getId(),
                "Agendamento atualizado para o idoso: " + updated.getElderly().getName()
        );

        return updated;
    }

    @Override
    public Appointment cancelAppointment(UUID id) {
        Appointment appointment = findById(id);

        if (appointment.getStatus() == AppointmentStatus.CONCLUIDO) {
            throw new BusinessException("Não é possível cancelar agendamentos já concluídos");
        }

        if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
            throw new BusinessException("Este agendamento já está cancelado");
        }

        appointment.setStatus(AppointmentStatus.CANCELADO);
        Appointment cancelled = appointmentRepository.save(appointment);

        auditService.recordEvent(
                cancelled.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CANCEL_APPOINTMENT",
                "Agendamento",
                cancelled.getId(),
                "Agendamento cancelado: " + cancelled.getId()
        );

        return cancelled;
    }

    @Override
    public Appointment updateStatus(UUID id, AppointmentStatus status) {
        Appointment appointment = findById(id);

        // Validação de transição de estados
        validateStatusTransition(appointment.getStatus(), status);

        appointment.setStatus(status);
        Appointment updated = appointmentRepository.save(appointment);

        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_APPOINTMENT_STATUS",
                "Agendamento",
                updated.getId(),
                "Status do agendamento atualizado para: " + status
        );

        return updated;
    }

    @Override
    public List<Appointment> findByElderly(UUID elderlyId) {
        return appointmentRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<Appointment> findByCaregiver(UUID caregiverId) {
        return appointmentRepository.findByCaregiverId(caregiverId);
    }

    @Override
    public List<Appointment> findByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return appointmentRepository.findByDateTimeBetween(startDateTime, endDateTime);
    }

    @Override
    public boolean hasScheduleConflict(UUID caregiverId, LocalDateTime dateTime,
                                    int durationMinutes, UUID excludeAppointmentId) {
        // Calcula o fim do período baseado na duração
        LocalDateTime endDateTime = dateTime.plusMinutes(durationMinutes);

        // Busca agendamentos conflitantes
        List<Appointment> conflictingAppointments =
            appointmentRepository.findConflictingAppointments(caregiverId, dateTime, endDateTime);

        // Se houver um ID a ser excluído da verificação, filtra a lista
        if (excludeAppointmentId != null) {
            conflictingAppointments = conflictingAppointments.stream()
                .filter(a -> !a.getId().equals(excludeAppointmentId))
                .toList();
        }

        return !conflictingAppointments.isEmpty();
    }

    @Override
    public Appointment addObservation(UUID id, String observation) {
        Appointment appointment = findById(id);

        // Não permite adicionar observações em agendamentos cancelados
        if (appointment.getStatus() == AppointmentStatus.CANCELADO) {
            throw new BusinessException("Não é possível adicionar observações em agendamentos cancelados");
        }

        if (observation == null || observation.isBlank()) {
            throw new BusinessException("A observação não pode ser vazia");
        }

        // Concatena a nova observação ao campo description
        String currentDescription = appointment.getDescription();
        String updatedDescription = currentDescription != null && !currentDescription.isBlank()
                                  ? currentDescription + "\n\n--- " + LocalDateTime.now() + " ---\n" + observation
                                  : "--- " + LocalDateTime.now() + " ---\n" + observation;

        appointment.setDescription(updatedDescription);
        Appointment updated = appointmentRepository.save(appointment);

        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "ADD_APPOINTMENT_OBSERVATION",
                "Agendamento",
                updated.getId(),
                "Observação adicionada ao agendamento"
        );

        return updated;
    }

    private void validateAppointmentData(Appointment appointment) {
        if (appointment.getElderly() == null || appointment.getElderly().getId() == null) {
            throw new BusinessException("O idoso do agendamento é obrigatório");
        }

        if (appointment.getCaregiver() == null || appointment.getCaregiver().getId() == null) {
            throw new BusinessException("O cuidador do agendamento é obrigatório");
        }

        if (appointment.getOrganization() == null || appointment.getOrganization().getId() == null) {
            throw new BusinessException("A organização do agendamento é obrigatória");
        }

        if (appointment.getDateTime() == null) {
            throw new BusinessException("A data e hora do agendamento são obrigatórias");
        }
    }

    private void validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        if (currentStatus == newStatus) {
            return; // Não há mudança de status
        }

        switch (currentStatus) {
            case AGENDADO:
                // De AGENDADO pode ir para EM_ANDAMENTO ou CANCELADO
                if (newStatus != AppointmentStatus.EM_ANDAMENTO && newStatus != AppointmentStatus.CANCELADO) {
                    throw new BusinessException(
                            "Transição de status inválida. De AGENDADO só pode ir para EM_ANDAMENTO ou CANCELADO");
                }
                break;
            case EM_ANDAMENTO:
                // De EM_ANDAMENTO pode ir para CONCLUIDO ou CANCELADO
                if (newStatus != AppointmentStatus.CONCLUIDO && newStatus != AppointmentStatus.CANCELADO) {
                    throw new BusinessException(
                            "Transição de status inválida. De EM_ANDAMENTO só pode ir para CONCLUIDO ou CANCELADO");
                }
                break;
            case CONCLUIDO:
                // De CONCLUIDO não pode mudar
                throw new BusinessException("Não é possível alterar o status de um agendamento CONCLUIDO");
            case CANCELADO:
                // De CANCELADO não pode mudar
                throw new BusinessException("Não é possível alterar o status de um agendamento CANCELADO");
        }
    }
}
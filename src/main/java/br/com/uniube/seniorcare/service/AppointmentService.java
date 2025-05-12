package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.enums.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de agendamentos.
 *
 * Regras de negócio:
 * 1. Cada agendamento deve estar vinculado a um idoso, cuidador e organização
 * 2. Não deve haver conflito de horário para o mesmo cuidador
 * 3. A data e hora do agendamento devem ser futuras no momento da criação
 * 4. Atualização de status segue o fluxo: AGENDADO -> EM_ANDAMENTO -> CONCLUIDO (ou CANCELADO a qualquer momento)
 * 5. Registro de eventos de auditoria para todas as operações
 */
public interface AppointmentService {

    /**
     * Retorna todos os agendamentos da organização do usuário atual.
     *
     * @return lista de agendamentos.
     */
    List<Appointment> findAll();

    /**
     * Busca um agendamento pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do agendamento.
     * @return agendamento encontrado.
     */
    Appointment findById(UUID id);

    /**
     * Cria um novo agendamento, aplicando validações de negócio.
     *
     * @param appointment entidade que representa o novo agendamento.
     * @return agendamento criado.
     */
    Appointment createAppointment(Appointment appointment);

    /**
     * Atualiza um agendamento existente.
     *
     * @param id identificador do agendamento a ser atualizado.
     * @param updatedAppointment entidade com os dados atualizados.
     * @return agendamento atualizado.
     */
    Appointment updateAppointment(UUID id, Appointment updatedAppointment);

    /**
     * Cancela um agendamento existente.
     *
     * @param id identificador do agendamento a ser cancelado.
     * @return agendamento cancelado.
     */
    Appointment cancelAppointment(UUID id);

    /**
     * Atualiza o status de um agendamento.
     *
     * @param id identificador do agendamento.
     * @param status novo status para o agendamento.
     * @return agendamento com status atualizado.
     */
    Appointment updateStatus(UUID id, AppointmentStatus status);

    /**
     * Lista agendamentos por idoso.
     *
     * @param elderlyId identificador do idoso.
     * @return lista de agendamentos do idoso.
     */
    List<Appointment> findByElderly(UUID elderlyId);

    /**
     * Lista agendamentos por cuidador.
     *
     * @param caregiverId identificador do cuidador.
     * @return lista de agendamentos do cuidador.
     */
    List<Appointment> findByCaregiver(UUID caregiverId);

    /**
     * Lista agendamentos por período.
     *
     * @param startDateTime data/hora inicial do período.
     * @param endDateTime data/hora final do período.
     * @return lista de agendamentos no período.
     */
    List<Appointment> findByPeriod(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * Verifica se existe conflito de horário para um cuidador.
     *
     * @param caregiverId identificador do cuidador.
     * @param dateTime data/hora a verificar.
     * @param durationMinutes duração em minutos.
     * @param excludeAppointmentId opcional - ID de agendamento a excluir da verificação.
     * @return true se existir conflito, false caso contrário.
     */
    boolean hasScheduleConflict(UUID caregiverId, LocalDateTime dateTime,
                              int durationMinutes, UUID excludeAppointmentId);

    /**
     * Adiciona observação a um agendamento existente.
     *
     * @param id identificador do agendamento.
     * @param observation texto da observação a ser adicionada.
     * @return agendamento com observação atualizada.
     */
    Appointment addObservation(UUID id, String observation);
}
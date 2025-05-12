package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByElderlyId(UUID elderlyId);

    List<Appointment> findByCaregiverId(UUID caregiverId);

    List<Appointment> findByDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    /**
     * Busca agendamentos que possam conflitar com um novo horário para um cuidador.
     *
     * @param caregiverId   ID do cuidador
     * @param startDateTime início do período a verificar
     * @param endDateTime   fim do período a verificar
     * @return lista de agendamentos que conflitam
     */
    @Query("SELECT a FROM Appointment a " +
            "WHERE a.caregiver.id = :caregiverId " +
            "AND a.status != 'CANCELADO' " +
            "AND ((a.dateTime <= :endDateTime AND a.dateTime >= :startDateTime) OR " +
            "     (a.dateTime <= :startDateTime AND FUNCTION('DATE_ADD', a.dateTime, 'INTERVAL 60 MINUTE') >= :startDateTime))")
    List<Appointment> findConflictingAppointments(@Param("caregiverId") UUID caregiverId,
                                                  @Param("startDateTime") LocalDateTime startDateTime,
                                                  @Param("endDateTime") LocalDateTime endDateTime);
}
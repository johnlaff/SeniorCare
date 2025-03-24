package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Appointment;
import br.com.uniube.seniorcare.domain.repository.AppointmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Appointment findById(UUID id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    public Appointment createAppointment(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(UUID id, Appointment updatedAppointment) {
        Appointment appointment = findById(id);
        appointment.setDateTime(updatedAppointment.getDateTime());
        appointment.setDescription(updatedAppointment.getDescription());
        // Atualize referências se necessário (elderly, caregiver)
        return appointmentRepository.save(appointment);
    }

    public void deleteAppointment(UUID id) {
        Appointment appointment = findById(id);
        appointmentRepository.delete(appointment);
    }
}

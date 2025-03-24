package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.Medication;
import br.com.uniube.seniorcare.domain.repository.MedicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;

    public MedicationService(MedicationRepository medicationRepository) {
        this.medicationRepository = medicationRepository;
    }

    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }

    public Medication findById(UUID id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + id));
    }

    public Medication createMedication(Medication medication) {
        return medicationRepository.save(medication);
    }

    public Medication updateMedication(UUID id, Medication updatedMedication) {
        Medication medication = findById(id);
        medication.setName(updatedMedication.getName());
        medication.setDosage(updatedMedication.getDosage());
        medication.setFrequency(updatedMedication.getFrequency());
        return medicationRepository.save(medication);
    }

    public void deleteMedication(UUID id) {
        Medication medication = findById(id);
        medicationRepository.delete(medication);
    }
}

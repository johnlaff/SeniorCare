package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.MedicalHistory;
import br.com.uniube.seniorcare.domain.repository.MedicalHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;

    public MedicalHistoryService(MedicalHistoryRepository medicalHistoryRepository) {
        this.medicalHistoryRepository = medicalHistoryRepository;
    }

    public List<MedicalHistory> findAll() {
        return medicalHistoryRepository.findAll();
    }

    public MedicalHistory findById(UUID id) {
        return medicalHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical history not found with id: " + id));
    }

    public MedicalHistory createMedicalHistory(MedicalHistory history) {
        return medicalHistoryRepository.save(history);
    }

    public MedicalHistory updateMedicalHistory(UUID id, MedicalHistory updatedHistory) {
        MedicalHistory history = findById(id);
        history.setCondition(updatedHistory.getCondition());
        history.setDateRecorded(updatedHistory.getDateRecorded());
        return medicalHistoryRepository.save(history);
    }

    public void deleteMedicalHistory(UUID id) {
        MedicalHistory history = findById(id);
        medicalHistoryRepository.delete(history);
    }
}

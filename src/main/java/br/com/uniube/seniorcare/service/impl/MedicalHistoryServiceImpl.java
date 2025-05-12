package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.MedicalHistory;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.MedicalHistoryRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.MedicalHistoryService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicalHistoryServiceImpl implements MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final ElderlyRepository elderlyRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public MedicalHistoryServiceImpl(MedicalHistoryRepository medicalHistoryRepository,
                                     ElderlyRepository elderlyRepository,
                                     AuditService auditService,
                                     SecurityUtils securityUtils) {
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.elderlyRepository = elderlyRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<MedicalHistory> findAll() {
        return medicalHistoryRepository.findAll();
    }

    @Override
    public MedicalHistory findById(UUID id) {
        return medicalHistoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Registro de histórico médico não encontrado com o id: " + id));
    }

    @Override
    public List<MedicalHistory> findByElderly(UUID elderlyId) {
        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        return medicalHistoryRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<MedicalHistory> findByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("As datas inicial e final são obrigatórias");
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException("A data final não pode ser anterior à data inicial");
        }

        return medicalHistoryRepository.findByDateRecordedBetween(startDate, endDate);
    }

    @Override
    public MedicalHistory createMedicalHistory(MedicalHistory medicalHistory) {
        validateMedicalHistoryData(medicalHistory);

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(medicalHistory.getElderly().getId())
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + medicalHistory.getElderly().getId()));

        // Se a data de registro não for informada, usa a data atual
        if (medicalHistory.getDateRecorded() == null) {
            medicalHistory.setDateRecorded(LocalDateTime.now());
        }

        MedicalHistory createdMedicalHistory = medicalHistoryRepository.save(medicalHistory);

        auditService.recordEvent(
                medicalHistory.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_MEDICAL_HISTORY",
                "HistóricoMédico",
                createdMedicalHistory.getId(),
                "Registro médico adicionado para o idoso: " + elderly.getName()
        );

        return createdMedicalHistory;
    }

    @Override
    public MedicalHistory updateMedicalHistory(UUID id, MedicalHistory updatedMedicalHistory) {
        MedicalHistory medicalHistory = findById(id);

        validateMedicalHistoryData(updatedMedicalHistory);

        // Atualiza apenas os campos permitidos
        medicalHistory.setCondition(updatedMedicalHistory.getCondition());
        medicalHistory.setDateRecorded(updatedMedicalHistory.getDateRecorded());

        MedicalHistory updated = medicalHistoryRepository.save(medicalHistory);

        auditService.recordEvent(
                medicalHistory.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_MEDICAL_HISTORY",
                "HistóricoMédico",
                updated.getId(),
                "Registro médico atualizado para o idoso: " + medicalHistory.getElderly().getName()
        );

        return updated;
    }

    @Override
    public void deleteMedicalHistory(UUID id) {
        MedicalHistory medicalHistory = findById(id);

        medicalHistoryRepository.delete(medicalHistory);

        auditService.recordEvent(
                medicalHistory.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_MEDICAL_HISTORY",
                "HistóricoMédico",
                medicalHistory.getId(),
                "Registro médico excluído para o idoso: " + medicalHistory.getElderly().getName()
        );
    }

    private void validateMedicalHistoryData(MedicalHistory medicalHistory) {
        if (medicalHistory.getElderly() == null || medicalHistory.getElderly().getId() == null) {
            throw new BusinessException("O idoso associado ao registro médico é obrigatório");
        }

        if (medicalHistory.getOrganization() == null || medicalHistory.getOrganization().getId() == null) {
            throw new BusinessException("A organização do registro médico é obrigatória");
        }

        if (medicalHistory.getCondition() == null || medicalHistory.getCondition().isBlank()) {
            throw new BusinessException("A condição médica é obrigatória");
        }

        // Verifica se a data de registro é futura
        if (medicalHistory.getDateRecorded() != null && medicalHistory.getDateRecorded().isAfter(LocalDateTime.now())) {
            throw new BusinessException("A data de registro não pode ser futura");
        }
    }
}
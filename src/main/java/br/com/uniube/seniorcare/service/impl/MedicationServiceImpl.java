package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.Medication;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.MedicationRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.MedicationService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository medicationRepository;
    private final ElderlyRepository elderlyRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public MedicationServiceImpl(MedicationRepository medicationRepository,
                                 ElderlyRepository elderlyRepository,
                                 AuditService auditService,
                                 SecurityUtils securityUtils) {
        this.medicationRepository = medicationRepository;
        this.elderlyRepository = elderlyRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<Medication> findAll() {
        return medicationRepository.findAll();
    }

    @Override
    public Medication findById(UUID id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Medicamento não encontrado com o id: " + id));
    }

    @Override
    public List<Medication> findByElderly(UUID elderlyId) {
        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        return medicationRepository.findByElderlyId(elderlyId);
    }

    @Override
    public Medication createMedication(Medication medication) {
        validateMedicationData(medication);

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(medication.getElderly().getId())
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + medication.getElderly().getId()));

        Medication createdMedication = medicationRepository.save(medication);

        auditService.recordEvent(
                medication.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_MEDICATION",
                "Medicamento",
                createdMedication.getId(),
                "Medicamento cadastrado: " + createdMedication.getName() + " para o idoso: " + elderly.getName()
        );

        return createdMedication;
    }

    @Override
    public Medication updateMedication(UUID id, Medication updatedMedication) {
        Medication medication = findById(id);

        validateMedicationData(updatedMedication);

        // Atualiza dados
        medication.setName(updatedMedication.getName());
        medication.setDosage(updatedMedication.getDosage());
        medication.setFrequency(updatedMedication.getFrequency());

        Medication updated = medicationRepository.save(medication);

        auditService.recordEvent(
                medication.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_MEDICATION",
                "Medicamento",
                updated.getId(),
                "Medicamento atualizado: " + updated.getName() + " para o idoso: " + medication.getElderly().getName()
        );

        return updated;
    }

    @Override
    public void deleteMedication(UUID id) {
        Medication medication = findById(id);

        medicationRepository.delete(medication);

        auditService.recordEvent(
                medication.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_MEDICATION",
                "Medicamento",
                medication.getId(),
                "Medicamento excluído: " + medication.getName() + " para o idoso: " + medication.getElderly().getName()
        );
    }

    private void validateMedicationData(Medication medication) {
        if (medication.getName() == null || medication.getName().isBlank()) {
            throw new BusinessException("O nome do medicamento é obrigatório");
        }

        if (medication.getElderly() == null || medication.getElderly().getId() == null) {
            throw new BusinessException("O idoso associado ao medicamento é obrigatório");
        }

        if (medication.getOrganization() == null || medication.getOrganization().getId() == null) {
            throw new BusinessException("A organização do medicamento é obrigatória");
        }

        // Validações opcionais para dosagem e frequência
        if (medication.getDosage() != null && medication.getDosage().length() > 100) {
            throw new BusinessException("A dosagem não pode exceder 100 caracteres");
        }

        if (medication.getFrequency() != null && medication.getFrequency().length() > 50) {
            throw new BusinessException("A frequência não pode exceder 50 caracteres");
        }
    }
}
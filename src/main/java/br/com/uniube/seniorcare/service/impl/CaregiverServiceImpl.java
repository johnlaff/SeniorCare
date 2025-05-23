package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.CaregiverRepository;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.CaregiverService;
import br.com.uniube.seniorcare.service.ElderlyService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import br.com.uniube.seniorcare.web.dto.response.ElderlyResponse;
import br.com.uniube.seniorcare.web.mapper.ElderlyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CaregiverServiceImpl implements CaregiverService {

    private final CaregiverRepository caregiverRepository;
    private final UserRepository userRepository;
    private final ElderlyRepository elderlyRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final ElderlyMapper elderlyMapper;
    private final ElderlyService elderlyService;

    public CaregiverServiceImpl(CaregiverRepository caregiverRepository,
                               UserRepository userRepository,
                               ElderlyRepository elderlyRepository,
                               AuditService auditService,
                               SecurityUtils securityUtils,
                               ElderlyMapper elderlyMapper,
                               ElderlyService elderlyService) {
        this.caregiverRepository = caregiverRepository;
        this.userRepository = userRepository;
        this.elderlyRepository = elderlyRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.elderlyMapper = elderlyMapper;
        this.elderlyService = elderlyService;
    }

    @Override
    public List<Caregiver> findAll() {
        return caregiverRepository.findAll();
    }

    @Override
    public Caregiver findById(UUID id) {
        return caregiverRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Cuidador não encontrado com o id: " + id));
    }

    @Override
    public Caregiver createCaregiver(Caregiver caregiver) {
        validateCaregiverData(caregiver);

        // Verifica se o usuário existe
        User user = userRepository.findById(caregiver.getUser().getId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + caregiver.getUser().getId()));

        // Verifica se o usuário já é um cuidador
        if (caregiverRepository.existsByUserId(user.getId())) {
            throw new BusinessException("Este usuário já está registrado como cuidador");
        }

        // Verifica se o usuário tem o papel CAREGIVER
        if (user.getRole() != Role.CAREGIVER) {
            throw new BusinessException("O usuário deve ter o papel CAREGIVER para ser registrado como cuidador");
        }

        Caregiver createdCaregiver = caregiverRepository.save(caregiver);

        auditService.recordEvent(
                createdCaregiver.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_CAREGIVER",
                "Cuidador",
                createdCaregiver.getId(),
                "Cuidador cadastrado: " + user.getName()
        );

        return createdCaregiver;
    }

    @Override
    public Caregiver updateCaregiver(UUID id, Caregiver updatedCaregiver) {
        Caregiver caregiver = findById(id);

        // Atualiza apenas a especialidade
        caregiver.setSpecialty(updatedCaregiver.getSpecialty());

        Caregiver updated = caregiverRepository.save(caregiver);

        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_CAREGIVER",
                "Cuidador",
                updated.getId(),
                "Cuidador atualizado: " + updated.getUser().getName()
        );

        return updated;
    }

    @Override
    public void deleteCaregiver(UUID id) {
        Caregiver caregiver = findById(id);

        // Verifica se o cuidador possui vínculos com idosos
        List<Elderly> assignedElderly = getAssignedElderly(id);
        if (!assignedElderly.isEmpty()) {
            throw new BusinessException("Não é possível excluir o cuidador; existem idosos vinculados.");
        }

        caregiverRepository.delete(caregiver);

        auditService.recordEvent(
                caregiver.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_CAREGIVER",
                "Cuidador",
                caregiver.getId(),
                "Cuidador excluído: " + caregiver.getUser().getName()
        );
    }

    @Override
    public List<Caregiver> findBySpecialty(String specialty) {
        return caregiverRepository.findBySpecialtyContainingIgnoreCase(specialty);
    }

    @Override
    public List<Elderly> getAssignedElderly(UUID caregiverId) {
        // Verifica se o cuidador existe
        findById(caregiverId);

        // Retorna a lista de idosos associados ao cuidador
        return elderlyRepository.findByCaregiverId(caregiverId);
    }

    @Override
    public List<ElderlyResponse> getAssignedElderlyEnriched(UUID caregiverId) {
        // Verifica se o cuidador existe
        findById(caregiverId);
        List<Elderly> elderlyList = elderlyRepository.findByCaregiverId(caregiverId);
        return elderlyList.stream()
                .map(elderly -> elderlyMapper.toEnrichedElderlyResponse(
                        elderly,
                        elderlyService.getCaregiversByElderlyId(elderly.getId()),
                        elderlyService.getFamilyMembersByElderlyId(elderly.getId())
                ))
                .toList();
    }

    private void validateCaregiverData(Caregiver caregiver) {
        if (caregiver.getUser() == null || caregiver.getUser().getId() == null) {
            throw new BusinessException("O usuário do cuidador é obrigatório");
        }

        if (caregiver.getOrganization() == null || caregiver.getOrganization().getId() == null) {
            throw new BusinessException("A organização do cuidador é obrigatória");
        }
    }
}


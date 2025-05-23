package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Caregiver;
import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Relationship;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.CaregiverRepository;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.FamilyMemberRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
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
public class ElderlyServiceImpl implements ElderlyService {

    private final ElderlyRepository elderlyRepository;
    private final CaregiverRepository caregiverRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;
    private final ElderlyMapper elderlyMapper;

    public ElderlyServiceImpl(ElderlyRepository elderlyRepository,
                              CaregiverRepository caregiverRepository,
                              FamilyMemberRepository familyMemberRepository,
                              UserRepository userRepository,
                              AuditService auditService,
                              SecurityUtils securityUtils,
                              ElderlyMapper elderlyMapper) {
        this.elderlyRepository = elderlyRepository;
        this.caregiverRepository = caregiverRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
        this.elderlyMapper = elderlyMapper;
    }

    @Override
    public List<Elderly> findAll() {
        return elderlyRepository.findAll();
    }

    @Override
    public Elderly findById(UUID id) {
        return elderlyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + id));
    }

    @Override
    public Elderly createElderly(Elderly elderly) {
        validateElderlyData(elderly);

        Elderly createdElderly = elderlyRepository.save(elderly);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_ELDERLY",
                "Idoso",
                createdElderly.getId(),
                "Idoso cadastrado: " + createdElderly.getName()
        );

        return createdElderly;
    }

    @Override
    public Elderly updateElderly(UUID id, Elderly updatedElderly) {
        Elderly elderly = findById(id);

        validateElderlyData(updatedElderly);

        // Atualiza dados
        elderly.setName(updatedElderly.getName());
        elderly.setBirthDate(updatedElderly.getBirthDate());
        elderly.setEmergencyContact(updatedElderly.getEmergencyContact());
        elderly.setAddress(updatedElderly.getAddress());

        Elderly updated = elderlyRepository.save(elderly);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_ELDERLY",
                "Idoso",
                updated.getId(),
                "Idoso atualizado: " + updated.getName()
        );

        return updated;
    }

    @Override
    public void deleteElderly(UUID id) {
        Elderly elderly = findById(id);

        // Verifica se existem vínculos que impedem a exclusão
        if (elderlyRepository.hasDependentRecords(elderly.getId())) {
            throw new BusinessException("Não é possível excluir o idoso; existem registros dependentes.");
        }

        elderlyRepository.delete(elderly);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_ELDERLY",
                "Idoso",
                elderly.getId(),
                "Idoso excluído: " + elderly.getName()
        );
    }

    @Override
    public Elderly assignCaregiver(UUID elderlyId, UUID caregiverId) {
        Elderly elderly = findById(elderlyId);

        // Verifica se o cuidador existe
        if (!caregiverRepository.existsById(caregiverId)) {
            throw new BusinessException("Cuidador não encontrado com o id: " + caregiverId);
        }

        // Verifica se já existe vínculo
        if (elderlyRepository.hasCaregiver(elderlyId, caregiverId)) {
            throw new BusinessException("Este cuidador já está vinculado ao idoso.");
        }

        elderlyRepository.assignCaregiver(elderlyId, caregiverId);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "ASSIGN_CAREGIVER",
                "Idoso",
                elderly.getId(),
                "Cuidador vinculado ao idoso: " + elderly.getName()
        );

        return elderly;
    }

    @Override
    public Elderly removeCaregiver(UUID elderlyId, UUID caregiverId) {
        Elderly elderly = findById(elderlyId);

        // Verifica se existe vínculo
        if (!elderlyRepository.hasCaregiver(elderlyId, caregiverId)) {
            throw new BusinessException("Este cuidador não está vinculado ao idoso.");
        }

        elderlyRepository.removeCaregiver(elderlyId, caregiverId);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "REMOVE_CAREGIVER",
                "Idoso",
                elderly.getId(),
                "Cuidador removido do idoso: " + elderly.getName()
        );

        return elderly;
    }

    @Override
    public FamilyMember addFamilyMember(UUID elderlyId, UUID userId, String relationship) {
        Elderly elderly = findById(elderlyId);

        // Verifica se o usuário existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + userId));

        // Verifica se já existe vínculo
        if (familyMemberRepository.existsByUserIdAndElderlyId(userId, elderlyId)) {
            throw new BusinessException("Este usuário já está vinculado como familiar deste idoso.");
        }

        // Converte a String para o enum Relationship
        Relationship relationshipEnum;
        try {
            relationshipEnum = Relationship.valueOf(relationship);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Tipo de relacionamento inválido. Valores permitidos: FILHO, NETO, SOBRINHO, OUTRO");
        }

        // Cria o vínculo
        FamilyMember familyMember = FamilyMember.builder()
                .id(UUID.randomUUID())
                .organization(elderly.getOrganization())
                .user(user)
                .elderly(elderly)
                .relationship(relationshipEnum)
                .build();

        FamilyMember created = familyMemberRepository.save(familyMember);

        auditService.recordEvent(
                elderly.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "ADD_FAMILY_MEMBER",
                "Familiar",
                created.getId(),
                "Familiar adicionado ao idoso " + elderly.getName() + ": " + user.getName()
        );

        return created;
    }

    @Override
    public void removeFamilyMember(UUID familyMemberId) {
        FamilyMember familyMember = familyMemberRepository.findById(familyMemberId)
                .orElseThrow(() -> new BusinessException("Vínculo familiar não encontrado com o id: " + familyMemberId));

        familyMemberRepository.delete(familyMember);

        auditService.recordEvent(
                familyMember.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "REMOVE_FAMILY_MEMBER",
                "Familiar",
                familyMember.getId(),
                "Familiar removido do idoso " + familyMember.getElderly().getName() + ": " + familyMember.getUser().getName()
        );
    }

    @Override
    public List<Caregiver> getCaregiversByElderlyId(UUID elderlyId) {
        return caregiverRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<FamilyMember> getFamilyMembersByElderlyId(UUID elderlyId) {
        return familyMemberRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<ElderlyResponse> findAllEnriched() {
        List<Elderly> elderlyList = findAll();
        return elderlyList.stream()
                .map(e -> elderlyMapper.toEnrichedElderlyResponse(
                        e,
                        getCaregiversByElderlyId(e.getId()),
                        getFamilyMembersByElderlyId(e.getId())
                ))
                .toList();
    }

    @Override
    public ElderlyResponse findEnrichedById(UUID id) {
        Elderly elderly = findById(id);
        return elderlyMapper.toEnrichedElderlyResponse(
                elderly,
                getCaregiversByElderlyId(id),
                getFamilyMembersByElderlyId(id)
        );
    }

    private void validateElderlyData(Elderly elderly) {
        if (elderly.getName() == null || elderly.getName().isBlank()) {
            throw new BusinessException("O nome do idoso é obrigatório");
        }
        if (elderly.getBirthDate() == null) {
            throw new BusinessException("A data de nascimento do idoso é obrigatória");
        }
        if (elderly.getOrganization() == null || elderly.getOrganization().getId() == null) {
            throw new BusinessException("A organização do idoso é obrigatória");
        }
    }
}

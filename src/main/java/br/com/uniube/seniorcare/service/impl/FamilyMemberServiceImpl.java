package br.com.uniube.seniorcare.service.impl;

import br.com.uniube.seniorcare.domain.entity.Elderly;
import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Relationship;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.ElderlyRepository;
import br.com.uniube.seniorcare.domain.repository.FamilyMemberRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import br.com.uniube.seniorcare.service.AuditService;
import br.com.uniube.seniorcare.service.FamilyMemberService;
import br.com.uniube.seniorcare.service.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FamilyMemberServiceImpl implements FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;
    private final ElderlyRepository elderlyRepository;
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public FamilyMemberServiceImpl(FamilyMemberRepository familyMemberRepository,
                                  UserRepository userRepository,
                                  ElderlyRepository elderlyRepository,
                                  AuditService auditService,
                                  SecurityUtils securityUtils) {
        this.familyMemberRepository = familyMemberRepository;
        this.userRepository = userRepository;
        this.elderlyRepository = elderlyRepository;
        this.auditService = auditService;
        this.securityUtils = securityUtils;
    }

    @Override
    public List<FamilyMember> findAll() {
        return familyMemberRepository.findAll();
    }

    @Override
    public FamilyMember findById(UUID id) {
        return familyMemberRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Vínculo familiar não encontrado com o id: " + id));
    }

    @Override
    public List<FamilyMember> findByElderly(UUID elderlyId) {
        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        return familyMemberRepository.findByElderlyId(elderlyId);
    }

    @Override
    public List<FamilyMember> findByUser(UUID userId) {
        // Verifica se o usuário existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + userId));

        // Precisamos criar este método no repositório
        return familyMemberRepository.findByUserId(userId);
    }

    @Override
    public FamilyMember createFamilyMember(FamilyMember familyMember) {
        validateFamilyMemberData(familyMember);

        // Verifica se o usuário existe
        User user = userRepository.findById(familyMember.getUser().getId())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + familyMember.getUser().getId()));

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(familyMember.getElderly().getId())
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + familyMember.getElderly().getId()));

        // Verifica se já existe vínculo
        if (familyMemberRepository.existsByUserIdAndElderlyId(user.getId(), elderly.getId())) {
            throw new BusinessException("Este usuário já está vinculado como familiar deste idoso.");
        }

        // Verifica se o usuário tem o papel FAMILY
        if (user.getRole() != Role.FAMILY) {
            throw new BusinessException("O usuário deve ter o papel FAMILY para ser registrado como familiar");
        }

        FamilyMember createdFamilyMember = familyMemberRepository.save(familyMember);

        auditService.recordEvent(
                familyMember.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "CREATE_FAMILY_MEMBER",
                "Familiar",
                createdFamilyMember.getId(),
                "Familiar adicionado ao idoso " + elderly.getName() + ": " + user.getName()
        );

        return createdFamilyMember;
    }

    @Override
    public FamilyMember addFamilyMemberToElderly(UUID userId, UUID elderlyId, Relationship relationship) {
        // Verifica se o usuário existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado com o id: " + userId));

        // Verifica se o idoso existe
        Elderly elderly = elderlyRepository.findById(elderlyId)
                .orElseThrow(() -> new BusinessException("Idoso não encontrado com o id: " + elderlyId));

        // Verifica se já existe vínculo
        if (familyMemberRepository.existsByUserIdAndElderlyId(userId, elderlyId)) {
            throw new BusinessException("Este usuário já está vinculado como familiar deste idoso.");
        }

        // Verifica se o usuário tem o papel FAMILY
        if (user.getRole() != Role.FAMILY) {
            throw new BusinessException("O usuário deve ter o papel FAMILY para ser registrado como familiar");
        }

        // Cria o vínculo
        FamilyMember familyMember = FamilyMember.builder()
                .id(UUID.randomUUID())
                .organization(elderly.getOrganization())
                .user(user)
                .elderly(elderly)
                .relationship(relationship)
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
    public FamilyMember updateRelationship(UUID id, Relationship relationship) {
        FamilyMember familyMember = findById(id);

        // Validação do enum
        if (relationship == null) {
            throw new BusinessException("O tipo de relacionamento não pode ser nulo");
        }

        familyMember.setRelationship(relationship);
        FamilyMember updated = familyMemberRepository.save(familyMember);

        auditService.recordEvent(
                updated.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "UPDATE_FAMILY_MEMBER",
                "Familiar",
                updated.getId(),
                "Relacionamento atualizado entre " + updated.getUser().getName() +
                " e idoso " + updated.getElderly().getName() + " para " + relationship
        );

        return updated;
    }

    @Override
    public void deleteFamilyMember(UUID id) {
        FamilyMember familyMember = findById(id);

        familyMemberRepository.delete(familyMember);

        auditService.recordEvent(
                familyMember.getOrganization().getId(),
                securityUtils.getCurrentUserId(),
                "DELETE_FAMILY_MEMBER",
                "Familiar",
                familyMember.getId(),
                "Familiar removido do idoso " + familyMember.getElderly().getName() + ": " + familyMember.getUser().getName()
        );
    }

    private void validateFamilyMemberData(FamilyMember familyMember) {
        if (familyMember.getUser() == null || familyMember.getUser().getId() == null) {
            throw new BusinessException("O usuário é obrigatório");
        }

        if (familyMember.getElderly() == null || familyMember.getElderly().getId() == null) {
            throw new BusinessException("O idoso é obrigatório");
        }

        if (familyMember.getOrganization() == null || familyMember.getOrganization().getId() == null) {
            throw new BusinessException("A organização é obrigatória");
        }

        if (familyMember.getRelationship() == null) {
            throw new BusinessException("O tipo de relacionamento é obrigatório");
        }
    }
}
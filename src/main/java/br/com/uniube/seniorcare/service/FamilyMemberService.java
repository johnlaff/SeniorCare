package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import br.com.uniube.seniorcare.domain.repository.FamilyMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class FamilyMemberService {

    private final FamilyMemberRepository familyMemberRepository;

    public FamilyMemberService(FamilyMemberRepository familyMemberRepository) {
        this.familyMemberRepository = familyMemberRepository;
    }

    public List<FamilyMember> findAll() {
        return familyMemberRepository.findAll();
    }

    public FamilyMember findById(UUID id) {
        return familyMemberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Family member not found with id: " + id));
    }

    public FamilyMember createFamilyMember(FamilyMember familyMember) {
        return familyMemberRepository.save(familyMember);
    }

    public FamilyMember updateFamilyMember(UUID id, FamilyMember updatedFamilyMember) {
        FamilyMember familyMember = findById(id);
        familyMember.setRelationship(updatedFamilyMember.getRelationship());
        // Atualize outros campos, se necessário
        return familyMemberRepository.save(familyMember);
    }

    public void deleteFamilyMember(UUID id) {
        FamilyMember familyMember = findById(id);
        familyMemberRepository.delete(familyMember);
    }
}

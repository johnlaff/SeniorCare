package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {
}

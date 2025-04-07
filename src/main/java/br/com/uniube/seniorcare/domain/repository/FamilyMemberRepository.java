package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.FamilyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, UUID> {

    /**
     * Verifica se existe um vínculo entre um usuário e um idoso
     *
     * @param userId ID do usuário
     * @param elderlyId ID do idoso
     * @return true se existir o vínculo, false caso contrário
     */
    boolean existsByUserIdAndElderlyId(UUID userId, UUID elderlyId);

    /**
     * Busca todos os membros da família vinculados a um idoso específico
     *
     * @param elderlyId ID do idoso
     * @return lista de membros da família
     */
    List<FamilyMember> findByElderlyId(UUID elderlyId);

    /**
     * Busca todos os vínculos de um usuário como membro da família
     *
     * @param userId ID do usuário
     * @return lista de vínculos de membro da família
     */
    List<FamilyMember> findByUserId(UUID userId);
}
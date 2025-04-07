package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Busca um usuário pelo seu email
     *
     * @param email email do usuário a ser buscado
     * @return Optional contendo o usuário, se encontrado
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica se existe um usuário com o email informado
     *
     * @param email email a ser verificado
     * @return true se existir um usuário com o email informado, false caso contrário
     */
    boolean existsByEmail(String email);
}
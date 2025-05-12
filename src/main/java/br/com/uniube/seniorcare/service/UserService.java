package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Serviço para gerenciamento de usuários.
 *
 * Regras de negócio:
 * 1. Validação de unicidade do email
 * 2. Validação de formato de email
 * 3. Validação de requisitos de senha
 * 4. Registro de eventos de auditoria
 */
public interface UserService {

    /**
     * Retorna todos os usuários.
     *
     * @return lista de usuários.
     */
    List<User> findAll();

    /**
     * Busca um usuário pelo seu ID, lançando exceção se não encontrado.
     *
     * @param id identificador do usuário.
     * @return usuário encontrado.
     */
    User findById(UUID id);

    /**
     * Busca um usuário pelo email.
     *
     * @param email email do usuário.
     * @return Optional contendo o usuário, se encontrado.
     */
    Optional<User> findByEmail(String email);

    /**
     * Cria um novo usuário, aplicando validações de negócio.
     *
     * @param user entidade que representa o novo usuário.
     * @return usuário criado.
     */
    User createUser(User user);

    /**
     * Atualiza um usuário existente.
     *
     * @param id identificador do usuário a ser atualizado.
     * @param updatedUser entidade com os dados atualizados.
     * @return usuário atualizado.
     */
    User updateUser(UUID id, User updatedUser);

    /**
     * Altera a senha de um usuário.
     *
     * @param id identificador do usuário.
     * @param currentPassword senha atual.
     * @param newPassword nova senha.
     * @return usuário atualizado.
     */
    User changePassword(UUID id, String currentPassword, String newPassword);

    /**
     * Exclui um usuário.
     *
     * @param id identificador do usuário a ser excluído.
     */
    void deleteUser(UUID id);
}
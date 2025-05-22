package br.com.uniube.seniorcare.service;

import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.web.dto.request.RegisterRequest;

/**
 * Serviço para autenticação e cadastro de usuários.
 * <p>
 * Regras de negócio:
 * 1. Cadastro de usuário exige nome, email, senha, organização e papel válidos
 * 2. Validação de unicidade do email
 * 3. Senha deve ser criptografada antes de persistir
 * 4. Não retorna dados sensíveis na resposta
 */
public interface AuthService {
    /**
     * Realiza o cadastro de um novo usuário no sistema.
     *
     * @param request DTO contendo nome, email, senha, organização e papel do usuário.
     * @throws BusinessException se o email já estiver cadastrado ou a organização não existir.
     */
    void register(RegisterRequest request);
}

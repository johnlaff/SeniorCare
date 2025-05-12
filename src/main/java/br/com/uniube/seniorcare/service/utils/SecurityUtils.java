package br.com.uniube.seniorcare.service.utils;

import br.com.uniube.seniorcare.domain.exception.BusinessException;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils {

    private final UserRepository userRepository;

    @Value("${app.security.development-mode:false}")
    private boolean developmentMode;

    // Default admin user ID for development
    private static final UUID DEV_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public SecurityUtils(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtém o ID do usuário atual autenticado
     *
     * @return UUID do usuário autenticado
     * @throws BusinessException se não houver usuário autenticado ou se não for encontrado
     */
    public UUID getCurrentUserId() {
        // If in development mode, return the default user ID
        if (developmentMode) {
            return DEV_USER_ID;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BusinessException("Usuário não autenticado");
        }

        // Handle UserDetails principal (from our JWT filter)
        if (authentication.getPrincipal() instanceof UserDetails) {
            String username = ((UserDetails) authentication.getPrincipal()).getUsername();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new BusinessException("Usuário autenticado não encontrado"))
                    .getId();
        }

        // Handle String principal (for backward compatibility)
        if (authentication.getPrincipal() instanceof String) {
            String username = (String) authentication.getPrincipal();
            return userRepository.findByEmail(username)
                    .orElseThrow(() -> new BusinessException("Usuário autenticado não encontrado"))
                    .getId();
        }

        throw new BusinessException("Tipo de autenticação não suportado");
    }

    /**
     * Verifica se o usuário atual tem papel de administrador
     *
     * @return true se o usuário for administrador, false caso contrário
     */
    public boolean isCurrentUserAdmin() {
        if (developmentMode) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

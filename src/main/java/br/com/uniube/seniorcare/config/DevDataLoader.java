package br.com.uniube.seniorcare.config;

import br.com.uniube.seniorcare.domain.entity.Organization;
import br.com.uniube.seniorcare.domain.entity.User;
import br.com.uniube.seniorcare.domain.enums.Role;
import br.com.uniube.seniorcare.domain.repository.OrganizationRepository;
import br.com.uniube.seniorcare.domain.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Component
@Slf4j
public class DevDataLoader {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.development-mode:true}")
    private boolean devMode;

    public DevDataLoader(OrganizationRepository organizationRepository,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void loadDevData() {
        if (!devMode) {
            log.info("Modo desenvolvimento desativado, carga inicial não será executada.");
            return;
        }

        log.info("Iniciando carga inicial de dados de desenvolvimento...");

        // Cria organização se não existir
        Organization org = organizationRepository.findAll().stream().findFirst().orElseGet(() -> {
            Organization newOrg = new Organization();
            newOrg.setName("DevOrg");
            newOrg.setDomain("devorg");
            Organization saved = organizationRepository.save(newOrg);
            log.info("Organização criada: {} (id={})", saved.getName(), saved.getId());
            return saved;
        });

        // Cria usuário admin se não existir
        if (userRepository.findByEmail("admin@dev.com").isEmpty()) {
            User admin = User.builder()
                    .name("Dev Admin")
                    .email("admin@dev.com")
                    .password(passwordEncoder.encode("admin123"))
                    .organization(org)
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Usuário admin criado: {} (email={})", admin.getName(), admin.getEmail());
        } else {
            log.info("Usuário admin já existe (email=admin@dev.com)");
        }

        log.info("Carga inicial de dados de desenvolvimento finalizada.");
    }
}

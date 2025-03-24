package br.com.uniube.seniorcare.domain.repository;

import br.com.uniube.seniorcare.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}

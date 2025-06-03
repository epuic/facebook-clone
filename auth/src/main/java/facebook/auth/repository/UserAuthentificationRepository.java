package facebook.auth.repository;

import facebook.auth.entity.UserAuthentification;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthentificationRepository extends AbstractRepository<UserAuthentification> {
    Optional<UserAuthentification> findByEmail(String email);
}

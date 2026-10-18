package claudodg.customerapi.reposotorie;

import claudodg.customerapi.entity.PasswordResetToken;
import claudodg.customerapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findByUserAndUsedFalse(User user);

    List<PasswordResetToken> findByUser(User user);

    boolean existsByUserAndCreatedAtAfter(User user, LocalDateTime since);
}

package claudodg.customerapi.reposotorie;

import claudodg.customerapi.entity.Score;
import claudodg.customerapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {

    List<Score> findByUserOrderByDateAsc(User user);
}

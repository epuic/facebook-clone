package facebook.server.repository;

import facebook.server.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends AbstractRepository<Vote> {
    Optional<Vote> findByUserIdAndContentId(Long userId, Long contentId);
}
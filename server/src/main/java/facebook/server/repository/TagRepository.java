package facebook.server.repository;

import facebook.server.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends AbstractRepository<Tag> {
    Tag findByName(String name);

    boolean existsByName(String name);
}

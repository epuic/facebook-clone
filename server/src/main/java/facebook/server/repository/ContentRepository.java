package facebook.server.repository;

import facebook.server.entity.Content;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends AbstractRepository<Content> {
    Page<Content> findAllByTypeContentIsTrue(Pageable pageable);
    List<Content> findByParentContentId(Long parentContentId);
}
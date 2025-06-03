package facebook.server.service;

import facebook.server.entity.Tag;
import facebook.server.entity.User;
import facebook.server.repository.ContentRepository;
import facebook.server.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TagService extends AbstractService<Tag, TagRepository> {

    @Autowired
    private UserService userService;
    @Autowired
    private ContentRepository contentRepository;

    @Override
    public Tag save(Tag entity) throws Exception {
        if(repository.existsByName(entity.getName())) {
            return null;
        }
        entity.setCreatedAt(LocalDateTime.now());
        return repository.save(entity);
    }

    public Tag findByNameOrCreate(String name) {
        Tag tag = repository.findByName(name);
        if (tag == null) {
            tag = new Tag();
            tag.setName(name);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setContents(new ArrayList<>());

            User user = userService.getUserFromJWT();
            tag.setOwner(user);

            tag = repository.save(tag);
        }
        return tag;
    }

    public List<Tag> getFromContentId(Long contentId) {
        return contentRepository.findById(contentId).get().getTags();
    }

}

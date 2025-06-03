package facebook.server.controller;

import facebook.server.entity.Tag;
import facebook.server.repository.TagRepository;
import facebook.server.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController extends AbstractController<Tag, TagService> {
    @Autowired
    private TagService tagService;

    @Override
    @Deprecated
    public ResponseEntity<Tag> add(Tag newEntry) throws Exception {
        return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Tag> delete(Long id) {
        return super.delete(id);
    }

    @Override
    public ResponseEntity<List<Tag>> getAll() {
        return super.getAll();
    }

    @GetMapping("from-content/{contentId}")
    public ResponseEntity<List<Tag>> getTagByContentId(@PathVariable Long contentId) {
        List<Tag> tags = tagService.getFromContentId(contentId);
        if (tags != null) {
            return new ResponseEntity<>(tags, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }
}

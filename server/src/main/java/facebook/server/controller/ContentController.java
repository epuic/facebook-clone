package facebook.server.controller;

import facebook.server.dto.ContentDTO;
import facebook.server.entity.Content;
import facebook.server.service.ContentService;
import facebook.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/content")
public class ContentController extends AbstractController<Content, ContentService> {
    @Autowired
    private ContentService contentService;
    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

    @Override
    @Deprecated
    @PostMapping("/admin")
    public ResponseEntity<Content> add(Content newEntry) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }
    @PostMapping
    public ResponseEntity<Content> add(
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("content") ContentDTO newEntry) {
        try{
            return new ResponseEntity<>(contentService.save(newEntry, photo), HttpStatus.OK);
        }
        catch (Exception e){
            logger.error("Error while saving content with file: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<Content> delete(Long id) {
        try {
            contentService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while deleting content: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<Content> update(Long id, Content updatedEntry) {
        try {
            return new ResponseEntity<>(contentService.update(id, updatedEntry), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while updating content: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/from/{cursor}")
    public ResponseEntity<List<Content>> getAll(@PathVariable int cursor) {
        try{
            List<Content> content = contentService.getAllLimited(cursor);
            return new ResponseEntity<>(content, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while getting all content: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Content>> getComments(@PathVariable Long id) {
        try {
            List<Content> comments = contentService.getCommentsForContent(id);
            return new ResponseEntity<>(comments, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error while getting comments: {}", e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}

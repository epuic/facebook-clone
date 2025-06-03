package facebook.server.controller;

import facebook.server.entity.Vote;
import facebook.server.service.VoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vote")
public class VoteController extends AbstractController<Vote, VoteService> {
    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    @Autowired
    VoteService voteService;

    @Override
    @PostMapping("/add")
    public ResponseEntity<Vote> add(Vote newEntry) {
        System.out.println(newEntry);
        try {
            return ResponseEntity.ok(voteService.save(newEntry));
        } catch (Exception e) {
            logger.error("Error adding vote: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Vote> delete(@PathVariable Long id) {
        try {
            voteService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting vote with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
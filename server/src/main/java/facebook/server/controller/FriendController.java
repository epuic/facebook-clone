package facebook.server.controller;

import facebook.server.entity.Friend;
import facebook.server.entity.User;
import facebook.server.service.FriendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friend")
public class FriendController extends AbstractController<Friend, FriendService> {

    private final Logger logger = LoggerFactory.getLogger(FriendController.class);

    @PostMapping("/add/{id}")
    public ResponseEntity<Friend> addFriend(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.createOrUpdateFriendship(id));
        }catch (Exception e) {
            logger.error("Error adding friend: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/mutual")
    public ResponseEntity<List<User>> getFriends() {
        try {
            List<User> friends = service.getFriendsMutual();
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            logger.error("Error getting friends: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<List<User>> getPendingFriends() {
        try {
            List<User> pendingFriends = service.getFriendsPending();
            return ResponseEntity.ok(pendingFriends);
        } catch (Exception e) {
            logger.error("Error getting pending friends: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/request")
    public ResponseEntity<List<User>> getFriendRequests() {
        try {
            List<User> friendRequests = service.getRequestFriends();
            return ResponseEntity.ok(friendRequests);
        } catch (Exception e) {
            logger.error("Error getting friend requests: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

    @DeleteMapping("/reject/{id}")
    public ResponseEntity rejectFriend(@PathVariable Long id) {
        try {
            service.rejectFriendship(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error rejecting friend: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }

}

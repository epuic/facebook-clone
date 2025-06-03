package facebook.server.controller;

import facebook.server.service.ServerService;
import facebook.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server")
public class ServerController {
    Logger logger = LoggerFactory.getLogger(ServerController.class);
    @Autowired
    UserService userService;
    @Autowired
    ServerService serverService;

    @PostMapping
    public ResponseEntity<String> receivePayload(@RequestBody String payload) {
        if(payload == null || payload.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            userService.processPayload(payload);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            if(e.getMessage().equals("User exists!")) {
                return ResponseEntity.badRequest().body("User exists!");
            }
        }
        return ResponseEntity.ok("Payload received!");
    }

    @PutMapping("user/ban/{id}")
    public ResponseEntity banPayload(@PathVariable Long id) {
        try {
            userService.banUser(id);
            serverService.sendBanPayload(id);
            logger.info("User banned: {}", id);
            return ResponseEntity.ok("User with ID " + id + " has been banned.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("user/unban/{id}")
    public ResponseEntity unbanPayload(@PathVariable Long id) {
        try {
            userService.unbanUser(id);
            serverService.sendUnbanPayload(id);
            logger.info("User unbanned: {}", id);
            return ResponseEntity.ok("User with ID " + id + " has been unbanned.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}


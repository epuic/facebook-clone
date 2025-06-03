package facebook.auth.controller;

import facebook.auth.dto.AuthDTO;
import facebook.auth.dto.RegisterDTO;
import facebook.auth.dto.ResetDTO;
import facebook.auth.dto.UserDTO;
import facebook.auth.entity.User;
import facebook.auth.service.UserAuthentificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    @Autowired
    UserAuthentificationService userService;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody AuthDTO authDTO) {
        if(authDTO == null || authDTO.getEmail() == null || authDTO.getPassword() == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(userService.login(authDTO));
    }


    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterDTO registerDTO) {
        if(registerDTO == null || registerDTO.getEmail() == null
                || registerDTO.getPassword() == null || registerDTO.getUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        User user = null;
        try {
            UserDTO userDTO = userService.register(registerDTO);
            if(userDTO.getStatusCode() == 400)
                return ResponseEntity.badRequest().body(userDTO);

            user = userDTO.getUser();
            userService.sendPayload(user);

            return ResponseEntity.ok(userDTO);
        }catch (Exception e){
            logger.error(e.getMessage());
            if(user != null)
                userService.getUserAuthentificationRepository()
                                .delete(userService.getUserAuthentificationRepository()
                                .findByEmail(user.getEmail()).get());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        if(email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        userService.sendPasswordResetEmail(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestBody ResetDTO resetDTO) {
        try {
            userService.resetPassword(resetDTO);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}


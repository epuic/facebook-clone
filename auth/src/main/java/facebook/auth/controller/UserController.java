package facebook.auth.controller;

import facebook.auth.entity.User;
import facebook.auth.entity.UserAuthentification;
import facebook.auth.service.EmailService;
import facebook.auth.service.SmsService;
import facebook.auth.service.UserAuthentificationService;
import facebook.auth.service.UserDetailsService;
import facebook.auth.utilities.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController extends AbstractController<UserAuthentification, UserAuthentificationService> {
    Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserAuthentificationService userAuthentificationService;
    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    AESUtil aesUtil;
    @Autowired
    EmailService emailService;
    @Autowired
    SmsService smsService;



    @PostMapping("/ban")
    public ResponseEntity banUser(@RequestBody String idString) {
        try {
            Long id = Long.valueOf(idString.trim());
            UserAuthentification user = userAuthentificationService.getUserAuthentificationRepository()
                    .findById(id).get();
            logger.info("User ID to ban: {}", id);
            userAuthentificationService.banUser(id);
            emailService.sendBanEmail(user.getEmail());
            smsService.sendSms("+18777804236", "You have been banned from Facebook application!");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Eroare la banUser:", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/unban")
    public ResponseEntity unbanUser(@RequestBody String idString) {
        try {
            Long id = Long.valueOf(idString.trim());
            UserAuthentification user = userAuthentificationService.getUserAuthentificationRepository()
                    .findById(id).get();
            logger.info("User ID to unban: {}", id);
            userAuthentificationService.unbanUser(id);
            emailService.sendUnbanEmail(user.getEmail());
            smsService.sendSms("+18777804236", "You have been un2banned from Facebook application!");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Eroare la unbanUser:", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

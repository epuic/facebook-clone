package facebook.server.controller;

import facebook.server.entity.User;
import facebook.server.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    UserService userService;
    @PostMapping("/photo")
    public ResponseEntity<String> handleFileUpload(HttpServletRequest req, @RequestParam("photo") MultipartFile photo) {
        if (photo.isEmpty()) return new ResponseEntity<>("Photo empty", HttpStatus.BAD_REQUEST);

        //Optional<String> token = JwtService.getToken(req);
        //if (token.isEmpty()) return new ResponseEntity<>("Missing token!", HttpStatus.BAD_REQUEST);
        //Integer userId = JwtService.staticExtractId(token.get());

        Long userId = 4L; // hardcoded for now, need to implement JWT
        Optional<User> optUser = userService.getUserRepository().findById(userId);
        if (optUser.isEmpty()) return new ResponseEntity<>("Missing user!", HttpStatus.BAD_REQUEST);
        User user = optUser.get();

        //the encoder puts "/" in the string, so we replace it with "." to avoid path problems
        String imageHashed = userService.getPasswordEncoder().encode(userId.toString() +
                        user.getUsername()).
                replace("/", ".");
        userService.getStorageS3Service().uploadFile(photo, imageHashed);

        user.setUrlPhoto(imageHashed);
        userService.getUserRepository().save(user);
        System.out.println(user);
        return new ResponseEntity<>("{ \"msg\" : \"Photo received\", \"url\": \"" + imageHashed + "\" }", HttpStatus.OK);
    }
    @GetMapping("/photo/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws Exception {
        byte[] imageBytes = userService.getStorageS3Service().getImage(imageName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(
                imageBytes,
                headers, HttpStatus.OK);
    }
}

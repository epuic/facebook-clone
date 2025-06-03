package facebook.server.controller;

import facebook.server.dto.UserDTO;
import facebook.server.entity.User;
import facebook.server.service.UserService;
import facebook.server.utilities.DTOBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/user")
public class UserController extends AbstractController<User, UserService> {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @PostMapping("/photo")
    public ResponseEntity uploadUserPhoto(@RequestPart("photo") MultipartFile photo) {
        try {
            userService.savePhoto(photo);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        try{
            return new ResponseEntity<>(userService.getUserFromJWT(), HttpStatus.OK);
        }catch (Exception e) {
            logger.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/photo-url/{userId}")
    public ResponseEntity<String> getUserPhotoUrl(@PathVariable Long userId) {
        Optional<User> user = userService.getUserRepository().findById(userId);
        if (user.isPresent()) {
            String urlPhoto = user.get().getUrlPhoto();
            // Fallback pentru valori greșite
            if (urlPhoto == null || urlPhoto.equalsIgnoreCase("url") || urlPhoto.equals("-")) {
                // URL de placeholder (poți schimba cu orice imagine vrei)
                return ResponseEntity.ok("https://ui-avatars.com/api/?name=User&background=random");
            }
            if (!(urlPhoto.startsWith("http://") || urlPhoto.startsWith("https://"))) {
                urlPhoto = "http://localhost:8081/user/photo/" + urlPhoto;
            }
            return ResponseEntity.ok(urlPhoto);
        }
        return ResponseEntity.notFound().build();
    }

    //TODO: user-ul normal n-are voie sa adauge sau sa stearga useri, update-ul numa daca este pe el insusi(din token)
}

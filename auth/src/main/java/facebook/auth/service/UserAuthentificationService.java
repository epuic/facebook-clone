package facebook.auth.service;

import facebook.auth.dto.AuthDTO;
import facebook.auth.dto.RegisterDTO;
import facebook.auth.dto.ResetDTO;
import facebook.auth.dto.UserDTO;
import facebook.auth.entity.User;
import facebook.auth.entity.UserAuthentification;
import facebook.auth.repository.UserAuthentificationRepository;
import facebook.auth.utilities.AESUtil;
import facebook.auth.utilities.DTOBuilder;
import facebook.auth.utilities.JWTUtils;
import facebook.auth.utilities.UserAuthentificationBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;

@Service
@Getter
public class UserAuthentificationService extends AbstractService<UserAuthentification, UserAuthentificationRepository> {
    @Autowired
    private UserAuthentificationRepository userAuthentificationRepository;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AESUtil aesUtil;
    @Autowired
    private EmailService emailService;


    public UserDTO register(RegisterDTO registerDTO) {
        DTOBuilder response = new DTOBuilder();

        try {
            if(registerDTO == null || registerDTO.getEmail().isEmpty() || registerDTO.getPassword().isEmpty()
                    || registerDTO.getUsername().isEmpty() || registerDTO.getPhoneNumber().isEmpty()) {
                return response.withStatusCode(400)
                        .withMessage("Invalid register request!").build();
            }

            UserAuthentification user = new UserAuthentificationBuilder()
                    .withUsername(registerDTO.getUsername())
                    .withEmail(registerDTO.getEmail())
                    .withPassword(passwordEncoder.encode(registerDTO.getPassword()))
                    .withPhoneNumber(registerDTO.getPhoneNumber())
                    .build();

            if(userAuthentificationRepository.findByEmail(user.getEmail()).isPresent()) {
                response.withStatusCode(400)
                        .withMessage("User already exists with this email!");

                return response.build();
            }

            UserAuthentification userResult = userAuthentificationRepository.save(user);
            if(userResult.getId() > 0) {
                response.withUser(userResult.toUser())
                        .withMessage("User saved succefully!")
                        .withStatusCode(200);

                return response.build();
            }
        }catch (Exception e) {
            System.out.println(e.getMessage());
            response.withStatusCode(500)
                    .withError(e.getMessage());
        }
        return response.build();
    }

    public UserDTO login(AuthDTO authDTO) {
        DTOBuilder response = new DTOBuilder();

        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(authDTO.getEmail(),
                                                                        authDTO.getPassword()));

            var user = userAuthentificationRepository.findByEmail(authDTO.getEmail())
                                                                .orElseThrow();
            if(user.isBanned()){
                response.withStatusCode(403)
                        .withMessage("User is banned!");
                return response.build();
            }
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            response.withStatusCode(200)
                    .withToken(jwt)
                    .withRefreshToken(refreshToken)
                    .withExpirationTime("24Hrs")
                    .withMessage("Successfully logged in")
                    .withUser(user.toUser());
        }catch (Exception e) {
            response.withStatusCode(500)
                    .withMessage(e.getMessage());
        }
        return response.build();
    }

    public UserDTO refreshToken(UserDTO refreshTokenRequest) { // probabil nu-l folosim
        DTOBuilder response = new DTOBuilder();
        try {
            String email = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            UserAuthentification user = userAuthentificationRepository
                    .findByEmail(email).orElseThrow();

            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), user)) {
                var jwt = jwtUtils.generateToken(user);
                response.withStatusCode(200)
                        .withToken(jwt)
                        .withRefreshToken(refreshTokenRequest.getToken())
                        .withExpirationTime("24Hrs")
                        .withMessage("Successfully refreshed token!")
                        .withStatusCode(200);
            }
        } catch (Exception e) {
            response.withStatusCode(500)
                    .withMessage(e.getMessage());

        }
        return response.build();
    }


    public String sendPayload(User user) throws Exception{
        if(user == null) {
            throw new Exception("User is null!");
        }
        RestTemplate restTemplate = new RestTemplate();
        String request = aesUtil.encrypt(user.toString());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:8081/server", request, String.class);

        if(response.getStatusCodeValue() == 400){
            throw new Exception(String.format("[%d] %s",
                    response.getStatusCodeValue(),
                    response.getBody()));
        }
        if (response.getStatusCodeValue() != 200) {
             throw new Exception(String.format("[%d] %s",
                                     response.getStatusCodeValue(),
                    response.getBody()));
}

        return response.getBody();
    }

    public boolean banUser(Long id){
        try {

            UserAuthentification user = userAuthentificationRepository.findById(id).orElseThrow();
            user.setBanned(true);
            userAuthentificationRepository.save(user);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public boolean unbanUser(Long id){
        try {
            UserAuthentification user = userAuthentificationRepository.findById(id).orElseThrow();
            user.setBanned(false);
            userAuthentificationRepository.save(user);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public void sendPasswordResetEmail(String email) {
        String generatedCode = String.valueOf((int) (Math.random() * 1000000));
        UserAuthentification user = userAuthentificationRepository.findByEmail(email).orElseThrow(); //for production
        user.setResetCode(generatedCode);
        user.setResetExpiration(LocalDateTime.now().plusMinutes(15));

        userAuthentificationRepository.save(user);
        emailService.sendVerificationCode(user.getEmail(), generatedCode);
    }

    public void resetPassword(ResetDTO resetDTO) {
        if(resetDTO == null || resetDTO.getEmail().isEmpty() ||
                resetDTO.getNewPassword().isEmpty() || resetDTO.getResetCode().isEmpty()) {
            throw new RuntimeException("Invalid reset request");
        }
        UserAuthentification user = userAuthentificationRepository.findByEmail(resetDTO.getEmail()).orElseThrow();

        if(user.getResetExpiration() == null || user.getResetCode() == null) {
            throw new RuntimeException("No reset code generated");
        }
        if(user.getResetExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset code expired");
        }
        if(!user.getResetCode().equals(resetDTO.getResetCode())) {
            throw new RuntimeException("Invalid reset code");
        }
        user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
        user.setResetCode(null);
        user.setResetExpiration(null);
        userAuthentificationRepository.save(user);
    }
}


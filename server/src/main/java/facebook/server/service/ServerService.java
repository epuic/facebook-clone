package facebook.server.service;

import facebook.server.entity.User;
import facebook.server.utilities.AESUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ServerService {
    Logger logger = LoggerFactory.getLogger(ServerService.class);

    @Autowired
    UserService userService;
    @Autowired
    AESUtil aesUtil;


    @Transactional
    public void sendBanPayload(Long id) throws Exception {
        User user = userService.getUserFromJWT();

        if (user.getRole().equals("USER")) {
            logger.error("Unauthorized access attempt by user with ID: {}", user.getId());
            throw new Exception("Unauthorized access attempt by user with ID: " + user.getId());
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/user/ban";

        try {
            HttpEntity<String> request = new HttpEntity<>(aesUtil.encrypt(String.valueOf(id)));
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            userService.unbanUser(id);
            logger.error("An error occurred while sending the ban payload", e);
        }
    }

    @Transactional
    public void sendUnbanPayload(Long id) throws Exception {
        User user = userService.getUserFromJWT();

        if (user.getRole().equals("USER")) {
            logger.error("Unauthorized access attempt by user with ID: {}", user.getId());
            throw new Exception("Unauthorized access attempt by user with ID: " + user.getId());
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8080/user/unban";

        try {
            HttpEntity<String> request = new HttpEntity<>(aesUtil.encrypt(String.valueOf(id)));
            restTemplate.postForObject(url, request, String.class);
        } catch (Exception e) {
            userService.banUser(id);
            logger.error("An error occurred while sending the unban payload", e);
        }
    }
}

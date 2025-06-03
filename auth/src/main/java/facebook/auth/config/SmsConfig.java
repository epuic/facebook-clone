package facebook.auth.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SmsConfig { // Twilio services
    private final static Logger logger = LoggerFactory.getLogger(SmsConfig.class);

    @Value("${twilio.account-sid}")
    private String accountSid;
    @Value("${twilio.auth-token}")
    private String authToken;
    @Value("${twilio.phone-number}")
    private String fromNumber;

    @PostConstruct
    private void initTwilio() {
        Twilio.init(accountSid, authToken);
        logger.info("Twilio initialized with account SID {} and from phone number {}", accountSid, fromNumber);
    }


}

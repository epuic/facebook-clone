package facebook.auth.service;

import facebook.auth.config.SmsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    private Logger logger = LoggerFactory.getLogger(SmsService.class);
    @Autowired
    private SmsConfig smsConfig;

    public void sendSms(String phoneNumber, String message) {
        try {
            com.twilio.rest.api.v2010.account.Message messageResponse = com.twilio.rest.api.v2010.account.Message.creator(
                    new com.twilio.type.PhoneNumber(phoneNumber),
                    new com.twilio.type.PhoneNumber(smsConfig.getFromNumber()),
                    message
            ).create();

            logger.info("SMS sent successfully to {}: {}", phoneNumber, messageResponse.getSid());
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
        }
    }


}

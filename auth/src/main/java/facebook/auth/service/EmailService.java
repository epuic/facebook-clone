package facebook.auth.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendBanEmail(String to) {
        try {
            logger.info("Sending ban email to " + to);
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setFrom("security@facebook-clone-support.com");
            helper.setSubject("Your Facebook Account Has Been Restricted");

            String htmlContent =
                    "<div style='font-family: Helvetica, Arial, sans-serif; color: #1c1e21; max-width: 600px; margin: 0 auto;'>" +
                            "    <div style='background-color: #4267B2; padding: 15px; text-align: center;'>" +
                            "        <img src='cid:facebook2' alt='Facebook' height='80'>" +
                            "    </div>" +
                            "    <div style='padding: 20px; background-color: #f0f2f5; border: 1px solid #dddfe2; border-radius: 3px;'>" +
                            "        <h2 style='color: #4267B2; margin-top: 0;'>Account Restriction Notice</h2>" +
                            "        <p>Dear User,</p>" +
                            "        <p>We've detected unusual activity on your Facebook account that violates our Community Standards.</p>" +
                            "        <p>As a result, your account has been temporarily restricted.</p>" +
                            "        <p>If you believe this decision was made in error, please contact our support team.</p>" +
                            "        <div style='margin: 25px 0;'>" +
                            "            <a href='#' style='background-color: #4267B2; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Review Account Status</a>" +
                            "        </div>" +
                            "        <p style='margin-top: 30px; color: #65676b; font-size: 12px;'>Facebook Support Team</p>" +
                            "    </div>" +
                            "</div>";

            helper.setText(htmlContent, true);
            helper.addInline("facebook2", new ClassPathResource("facebook2.png"));

            emailSender.send(mimeMessage);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }
    public void sendUnbanEmail(String to) {
        try {
            logger.info("Sending unban email to " + to);
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setFrom("security@facebook-clone-support.com");
            helper.setSubject("Your Facebook Account Has Been Restored");

            String htmlContent =
                    "<div style='font-family: Helvetica, Arial, sans-serif; color: #1c1e21; max-width: 600px; margin: 0 auto;'>" +
                            "    <div style='background-color: #4267B2; padding: 15px; text-align: center;'>" +
                            "        <img src='cid:facebook2' alt='Facebook' height='80'>" +
                            "    </div>" +
                            "    <div style='padding: 20px; background-color: #f0f2f5; border: 1px solid #dddfe2; border-radius: 3px;'>" +
                            "        <h2 style='color: #4267B2; margin-top: 0;'>Account Restoration Notice</h2>" +
                            "        <p>Dear User,</p>" +
                            "        <p>We're pleased to inform you that the restrictions on your Facebook account have been lifted.</p>" +
                            "        <p>Your account has been fully restored and you can now continue using all Facebook features.</p>" +
                            "        <p>Thank you for your patience during this process.</p>" +
                            "        <div style='margin: 25px 0;'>" +
                            "            <a href='#' style='background-color: #4267B2; color: white; padding: 10px 15px; text-decoration: none; border-radius: 4px; font-weight: bold;'>Go to Facebook</a>" +
                            "        </div>" +
                            "        <p style='margin-top: 30px; color: #65676b; font-size: 12px;'>Facebook Support Team</p>" +
                            "    </div>" +
                            "</div>";

            helper.setText(htmlContent, true);
            helper.addInline("facebook2", new ClassPathResource("facebook2.png"));

            emailSender.send(mimeMessage);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendVerificationCode(String to, String code) {
        try {
            logger.info("Sending verification code to " + to);
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setFrom("security@facebook-clone-support.com");
            helper.setSubject("Your Facebook Verification Code");

            String htmlContent =
                    "<div style='font-family: Helvetica, Arial, sans-serif; color: #1c1e21; max-width: 600px; margin: 0 auto;'>" +
                            "    <div style='background-color: #4267B2; padding: 15px; text-align: center;'>" +
                            "        <img src='cid:facebook2' alt='Facebook' height='80'>" +
                            "    </div>" +
                            "    <div style='padding: 20px; background-color: #f0f2f5; border: 1px solid #dddfe2; border-radius: 3px;'>" +
                            "        <h2 style='color: #4267B2; margin-top: 0;'>Verification Code</h2>" +
                            "        <p>Dear User,</p>" +
                            "        <p>You recently requested a verification code for your Facebook account. Use the code below to complete the process:</p>" +
                            "        <div style='margin: 25px 0; text-align: center;'>" +
                            "            <p style='font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #1877f2; padding: 10px; background-color: #ffffff; border: 1px solid #dddfe2; border-radius: 4px; display: inline-block;'>" + code + "</p>" +
                            "        </div>" +
                            "        <p>This code will expire in 15 minutes. If you didn't request this code, please ignore this email.</p>" +
                            "        <p style='margin-top: 30px; color: #65676b; font-size: 12px;'>Facebook Security Team</p>" +
                            "    </div>" +
                            "</div>";

            helper.setText(htmlContent, true);
            helper.addInline("facebook2", new ClassPathResource("facebook2.png"));

            emailSender.send(mimeMessage);
        }
        catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

}

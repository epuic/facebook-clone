package facebook.server.utilities;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AESUtil {

    private static final String ALGORITHM = "AES";
    private SecretKeySpec secretKey;

    @Value("${aes.secret}")
    private String secret;

    @PostConstruct
    private void init() {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalStateException("AES Secret Key is missing! Set 'aes.secret' in application.properties");
        }

        byte[] decodedKey = Base64.getDecoder().decode(secret);
        if (decodedKey.length != 16 && decodedKey.length != 24 && decodedKey.length != 32) {
            throw new IllegalStateException("Invalid AES key length: " + decodedKey.length + " bytes. Must be 16, 24, or 32 bytes.");
        }

        secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    public String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public String decrypt(String encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}

package facebook.auth.utilities;

import facebook.auth.entity.UserAuthentification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserAuthentificationBuilder {
    private final UserAuthentification user;

    public UserAuthentificationBuilder() {
        user = new UserAuthentification();
        user.setName("Default User");
        user.setEmail("default@example.com");
        user.setPassword("securepassword");
        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now().toString());
        user.setPhoneNumber("0000000000");

    }

    public UserAuthentificationBuilder withUsername(String username) {
        user.setName(username);
        return this;
    }

    public UserAuthentificationBuilder withEmail(String email) {
        user.setEmail(email);
        return this;
    }

    public UserAuthentificationBuilder withPassword(String password) {
        user.setPassword(password);
        return this;
    }

    public UserAuthentificationBuilder withRole(String role) {
        user.setRole(role);
        return this;
    }

    public UserAuthentificationBuilder withPhoneNumber(String phoneNumber) {
        user.setPhoneNumber(phoneNumber);
        return this;
    }

    public UserAuthentification build() {
        return user;
    }
}
